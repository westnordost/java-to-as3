package com.sojostudios.as3;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sojostudios.as3.visitors.AS3DumpVisitor;
import com.sojostudios.as3.visitors.AS3MutationVisitor;

/**
 * This class will take java source files as input and generate AS3 source files
 * as output.
 * 
 * There are a decent set of default mutation options, plus the ability to construct
 * your own custom mutations.
 * 
 * Options: 
 * 
 * - includeDefaultMutations: boolean, default is true.
 * -- use to include the default options, such as converting Lists to Vectors, etc.
 * 
 * - forceSprite: boolean, default is false.
 * -- use to force the target compilation class to extend the Sprite class, useful
 *    when generating a class to target for a SWF. Only works if the class doesn't
 *    already extend anything.
 *    
 * - forceMovieClip: boolean, default is false.
 * -- similar to the forceSprite option, except a MovieClip has no type enforcement
 * 
 * - packageToPackage: map of package => package conversions, allows RegEx matching.
 * 
 * - classesToClasses: map of Class => Class conversions, allows RegEx matching. Does 
 *   not perform any additional mutations to the class, only replaces references.
 *   
 * - importsToImports: map of import => import conversions, allows RegEx matching.
 * 
 * - importsToIgnore: list of import definitions to ignore, allows RegEx matching.
 * 
 * - forcedImports: list of imports to include by force.
 * 
 * - classesToArrays: list of Class reference types to be converted to AS3 Arrays.
 *   These classes will have their corresponding calls mutated to match AS3 format.
 *   If the underlying structure is template-typed, a meta tag will be generated
 *   to inform the Flex compiler of the type. Java native arrays are automatically
 *   converted to AS3 Arrays.
 *   Java:
 *   <pre>
 *   int[] x = new int[1];
 *   </pre>
 *   AS3:
 *   <pre>
 *   [ArrayElementType("int")] 
 *   var x:Array = new Array(1);
 *   </pre>
 *   
 * - classesToVectors: list of Class reference types to be converted to AS3 Typed Vectors.
 *   These classes must be template-typed. These will have their calls mutated to match AS3
 *   format. A common example would be List.
 *   Java:
 *   <pre>
 *   List<String> x = new ArrayList<String>();
 *   x.add("asdf");
 *   x.size();
 *   x.get(0);
 *   x.remove(0); 
 *   </pre>
 *   AS3:
 *   <pre>
 *   var x:Vector.<String> = new Vector.<String>();
 *   x.push("asdf");
 *   x.length;
 *   x[0];
 *   x.splice(0, 1);
 *   </pre>
 *   
 * - classesToDictionaries: list of Class reference types to be converted to AS3 Dictionaries.
 *   These classes do not have to be template-typed, since AS3 does not enforce any typing
 *   on Dictionaries. A common example would be a Map.
 *   Java:
 *   <pre>
 *   Map<String,String> x = new HashMap<String,String>();
 *   x.put("a", "b");
 *   x.size();
 *   x.get("a");
 *   x.remove("a");
 *   </pre>
 *   AS3:
 *   <pre>
 *   var x:Dictionary = new Dictionary();
 *   x["a"] = "b";
 *   x.length;
 *   x["a"];
 *   delete x["a"];
 *   </pre>
 * 
 * @author Kurtis Kopf
 *
 */
public class JavaToAS3Compiler
{
	private final Logger logger = Logger.getLogger(getClass());

	private Map<File,File> files = new HashMap<File,File>();
	private boolean includeDefaultMutations = true;
	
	private Map<String,String> packageToPackage = new HashMap<String,String>();
	private Map<String,String> classesToClasses = new HashMap<String,String>();
	private Map<String,String> importsToImports = new HashMap<String,String>();
	private List<String> importsToIgnore = new ArrayList<String>();
	private List<String> forcedImports = new ArrayList<String>();
	private List<String> classesToArrays = new ArrayList<String>();
	private List<String> classesToDictionaries = new ArrayList<String>();
	private List<String> classesToVectors = new ArrayList<String>();
	private List<String> classesExtendArray = new ArrayList<String>();
	private List<String> classesExtendDictionary = new ArrayList<String>();
	private List<String> classesExtendVector = new ArrayList<String>();
	private boolean forceSprite = false;
	private boolean forceMovieClip = false;
	
	private String arrayClass = null;
	private String vectorClass = null;
	private String dictionaryClass = null;
	
	/**
	 * Constructor.
	 */
	public JavaToAS3Compiler()
	{
		
	}
	
	/**
	 * Compile all specified files in the File map.
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	public void compileAll() throws ParseException, IOException
	{
		if (files != null && files.size() > 0)
		{
			for (File input : files.keySet())
			{
				File output = files.get(input);
				recursiveCompileFile(input, output);
			}
		}
	}

	/**
	 *  Compile a Java input file to an AS3 file. If the input file denotes a directory, all .java files
	 *  in the directory will be compiled recursively
	 *
	 * @param input Input file
	 * @param output Output file
	 * @throws ParseException
	 * @throws IOException
	 */
	private void recursiveCompileFile(File input, File output) throws ParseException, IOException
	{
		if (input.isDirectory())
		{
			for (File file : input.listFiles())
			{

				// when recursing down the directory tree, only try to parse .java files
				if (file.isDirectory())
				{
					File fileOutput = new File(output, file.getName());
					recursiveCompileFile(file, fileOutput);
				}
				else if (file.getName().endsWith(".java"))
				{
					// create any intermediate dirs
					if (output != null)
					{
						output.mkdirs();
					}
					compileFile(file, output);
				}
			}
		}
		else
		{
			compileFile(input, output);
		}
	}
	
	/**
	 * Compile a Java input file to an AS3 file.
	 * 
	 * @param inputFile
	 * @param outputDir
	 * @throws ParseException
	 * @throws IOException
	 */
	public void compileFile(File inputFile, File outputDir) throws ParseException, IOException
	{
		logger.info("Parsing "+inputFile.getPath() + "...");
		
		FileInputStream in = new FileInputStream(inputFile);
		CompilationUnit cu = null;
		cu = JavaParser.parse(in);
		in.close();
		
		AS3MutationVisitor as3Mut = new AS3MutationVisitor();
		if (includeDefaultMutations)
		{
			as3Mut.includeDefaults();
		}
		mergeMutationOptions(as3Mut);
		as3Mut.visit(cu, null);
		
		AS3DumpVisitor as3 = new AS3DumpVisitor();
		as3.visit(cu, null);
		String output = as3.toString();
		//logger.debug("Compilation output:\n" + output);
		
		File outputFile = null;
		// outputDir/File is optional
		if (outputDir == null || outputDir.isDirectory())
		{
			String name = inputFile.getName().replace(".java", ".as");
			outputFile = new File(outputDir, name);
		}
		else
		{
			outputFile = outputDir;
		}
	
		outputFile.createNewFile();
		FileOutputStream out = new FileOutputStream(outputFile);
		out.write(output.getBytes());
		out.close();
	}
	
	/**
	 * Compile a Java String to an AS3 String.
	 * 
	 * @param inputJava
	 * @return
	 * @throws ParseException
	 */
	public String compileString(String inputJava) throws ParseException
	{
		CompilationUnit cu = null;
		cu = JavaParser.parse(new ByteArrayInputStream(inputJava.getBytes()));
		
		AS3MutationVisitor as3Mut = new AS3MutationVisitor();
		if (includeDefaultMutations)
		{
			as3Mut.includeDefaults();
		}
		mergeMutationOptions(as3Mut);
		as3Mut.visit(cu, null);
		
		AS3DumpVisitor as3 = new AS3DumpVisitor();
		as3.visit(cu, null);
		String output = as3.toString();
		//logger.debug("Compilation output:\n" + output);
		return output;
	}
	
	private void mergeMutationOptions(AS3MutationVisitor as3Mut)
	{
		as3Mut.setForceSprite(forceSprite);
		as3Mut.setForceMovieClip(forceMovieClip);
		
		if (packageToPackage != null && packageToPackage.size() > 0)
		{
			as3Mut.getPackageToPackage().putAll(packageToPackage);
		}
		if (classesToClasses != null && classesToClasses.size() > 0)
		{
			as3Mut.getClassesToClasses().putAll(classesToClasses);
		}
		if (importsToImports != null && importsToImports.size() > 0)
		{
			as3Mut.getImportsToImports().putAll(importsToImports);
		}
		if (importsToIgnore != null && importsToIgnore.size() > 0)
		{
			as3Mut.getImportsToIgnore().addAll(importsToIgnore);
		}
		if (forcedImports != null && forcedImports.size() > 0)
		{
			as3Mut.getForcedImports().addAll(forcedImports);
		}
		if (classesToArrays != null && classesToArrays.size() > 0)
		{
			as3Mut.getClassesToArrays().addAll(classesToArrays);
		}
		if (classesToDictionaries != null && classesToDictionaries.size() > 0)
		{
			as3Mut.getClassesToDictionaries().addAll(classesToDictionaries);
		}
		if (classesToVectors != null && classesToVectors.size() > 0)
		{
			as3Mut.getClassesToVectors().addAll(classesToVectors);
		}
		if (classesExtendArray != null && classesExtendArray.size() > 0)
		{
			as3Mut.getClassesExtendArray().addAll(classesExtendArray);
		}
		if (classesExtendDictionary != null && classesExtendDictionary.size() > 0)
		{
			as3Mut.getClassesExtendDictionary().addAll(classesExtendDictionary);
		}
		if (classesExtendVector != null && classesExtendVector.size() > 0)
		{
			as3Mut.getClassesExtendVector().addAll(classesExtendVector);
		}
		if (arrayClass != null && !arrayClass.isEmpty())
		{
			as3Mut.setArrayClass(arrayClass);
		}
		if (vectorClass != null && !vectorClass.isEmpty())
		{
			as3Mut.setVectorClass(vectorClass);
		}
		if (dictionaryClass != null && !dictionaryClass.isEmpty())
		{
			as3Mut.setDictionaryClass(dictionaryClass);
		}
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			System.out.println("Usage: java JavaToAS3Compiler <input file or directory> [<output file or directory>]");
			return;
		}

		JavaToAS3Compiler me = new JavaToAS3Compiler();
		File inFile = new File(args[0]);
		File outFile = null;
		if (args.length == 2)
		{
			outFile = new File(args[1]);
		}
		me.getFiles().put(inFile, outFile);
		
		me.compileAll();
	}

	/**
	 * @return the files
	 */
	public Map<File, File> getFiles()
	{
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(Map<File, File> files)
	{
		this.files = files;
	}

	/**
	 * @return the includeDefaultMutations
	 */
	public boolean isIncludeDefaultMutations()
	{
		return includeDefaultMutations;
	}

	/**
	 * @param includeDefaultMutations the includeDefaultMutations to set
	 */
	public void setIncludeDefaultMutations(boolean includeDefaultMutations)
	{
		this.includeDefaultMutations = includeDefaultMutations;
	}

	/**
	 * @return the packageToPackage
	 */
	public Map<String, String> getPackageToPackage()
	{
		return packageToPackage;
	}

	/**
	 * @param packageToPackage the packageToPackage to set
	 */
	public void setPackageToPackage(Map<String, String> packageToPackage)
	{
		this.packageToPackage = packageToPackage;
	}

	/**
	 * @return the classesToClasses
	 */
	public Map<String, String> getClassesToClasses()
	{
		return classesToClasses;
	}

	/**
	 * @param classesToClasses the classesToClasses to set
	 */
	public void setClassesToClasses(Map<String, String> classesToClasses)
	{
		this.classesToClasses = classesToClasses;
	}

	/**
	 * @return the importsToImports
	 */
	public Map<String, String> getImportsToImports()
	{
		return importsToImports;
	}

	/**
	 * @param importsToImports the importsToImports to set
	 */
	public void setImportsToImports(Map<String, String> importsToImports)
	{
		this.importsToImports = importsToImports;
	}

	/**
	 * @return the importsToIgnore
	 */
	public List<String> getImportsToIgnore()
	{
		return importsToIgnore;
	}

	/**
	 * @param importsToIgnore the importsToIgnore to set
	 */
	public void setImportsToIgnore(List<String> importsToIgnore)
	{
		this.importsToIgnore = importsToIgnore;
	}

	/**
	 * @return the forcedImports
	 */
	public List<String> getForcedImports()
	{
		return forcedImports;
	}

	/**
	 * @param forcedImports the forcedImports to set
	 */
	public void setForcedImports(List<String> forcedImports)
	{
		this.forcedImports = forcedImports;
	}

	/**
	 * @return the classesToArrays
	 */
	public List<String> getClassesToArrays()
	{
		return classesToArrays;
	}

	/**
	 * @param classesToArrays the classesToArrays to set
	 */
	public void setClassesToArrays(List<String> classesToArrays)
	{
		this.classesToArrays = classesToArrays;
	}

	/**
	 * @return the classesToDictionaries
	 */
	public List<String> getClassesToDictionaries()
	{
		return classesToDictionaries;
	}

	/**
	 * @param classesToDictionaries the classesToDictionaries to set
	 */
	public void setClassesToDictionaries(List<String> classesToDictionaries)
	{
		this.classesToDictionaries = classesToDictionaries;
	}

	/**
	 * @return the classesToVectors
	 */
	public List<String> getClassesToVectors()
	{
		return classesToVectors;
	}

	/**
	 * @param classesToVectors the classesToVectors to set
	 */
	public void setClassesToVectors(List<String> classesToVectors) 
	{
		this.classesToVectors = classesToVectors;
	}

	/**
	 * @return the classesExtendArray
	 */
	public List<String> getClassesExtendArray() 
	{
		return classesExtendArray;
	}

	/**
	 * @param classesExtendArray the classesExtendArray to set
	 */
	public void setClassesExtendArray(List<String> classesExtendArray) 
	{
		this.classesExtendArray = classesExtendArray;
	}

	/**
	 * @return the classesExtendDictionaries
	 */
	public List<String> getClassesExtendDictionary() 
	{
		return classesExtendDictionary;
	}

	/**
	 * @param classesExtendDictionaries the classesExtendDictionaries to set
	 */
	public void setClassesExtendDictionary(List<String> classesExtendDictionary) 
	{
		this.classesExtendDictionary = classesExtendDictionary;
	}

	/**
	 * @return the classesExtendVectors
	 */
	public List<String> getClassesExtendVector() 
	{
		return classesExtendVector;
	}

	/**
	 * @param classesExtendVectors the classesExtendVectors to set
	 */
	public void setClassesExtendVector(List<String> classesExtendVector) 
	{
		this.classesExtendVector = classesExtendVector;
	}

	/**
	 * @return the forceSprite
	 */
	public boolean isForceSprite()
	{
		return forceSprite;
	}

	/**
	 * @param forceSprite the forceSprite to set
	 */
	public void setForceSprite(boolean forceSprite)
	{
		this.forceSprite = forceSprite;
	}

	/**
	 * @return the forceMovieClip
	 */
	public boolean isForceMovieClip()
	{
		return forceMovieClip;
	}

	/**
	 * @param forceMovieClip the forceMovieClip to set
	 */
	public void setForceMovieClip(boolean forceMovieClip)
	{
		this.forceMovieClip = forceMovieClip;
	}

	/**
	 * @return the arrayClass
	 */
	public String getArrayClass()
	{
		return arrayClass;
	}

	/**
	 * @param arrayClass the arrayClass to set
	 */
	public void setArrayClass(String arrayClass)
	{
		this.arrayClass = arrayClass;
	}

	/**
	 * @return the vectorClass
	 */
	public String getVectorClass()
	{
		return vectorClass;
	}

	/**
	 * @param vectorClass the vectorClass to set
	 */
	public void setVectorClass(String vectorClass)
	{
		this.vectorClass = vectorClass;
	}

	/**
	 * @return the dictionaryClass
	 */
	public String getDictionaryClass()
	{
		return dictionaryClass;
	}

	/**
	 * @param dictionaryClass the dictionaryClass to set
	 */
	public void setDictionaryClass(String dictionaryClass)
	{
		this.dictionaryClass = dictionaryClass;
	}
}