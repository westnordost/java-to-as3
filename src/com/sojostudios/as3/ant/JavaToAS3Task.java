package com.sojostudios.as3.ant;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sojostudios.as3.JavaToAS3Compiler;
import com.sojostudios.as3.ant.types.SourceTarget;

/**
 * Task to use the JavaToAS3Compiler.
 * 
 * Ant Usage:
 * <pre>
 *     <javaToAs3 includeDefaultMutations="true">
 *       <sourceTarget src="MyClass.java" dst="MyClass.as" forceMovieClip="true" />
 *       <sourceTarget src="MyHelperClass.java" dst="MyHelperClass.as" />
 *     </javaToAs3>
 * </pre>
 * 
 * Property loglevel can also be set to OFF|ALL|DEBUG|INFO|WARN|ERROR|FATAL to
 * configure Log4j for the com.sojostudios.as3 package.
 * 
 * @see JavaToAS3Compiler for more details on options
 * 
 * @author Kurtis Kopf
 *
 */
public class JavaToAS3Task extends Task
{
	private List<SourceTarget> targets = new ArrayList<SourceTarget>();
	
	private String logLevel = null;
	
	private boolean includeDefaultMutations = true;
	private boolean forceMovieClip = false;
	private boolean forceSprite = false;
	
	private String packageToPackage = null;
	private String classesToClasses = null;
	private String importsToImports = null;
	private String importsToIgnore = null;
	private String forcedImports = null;
	private String classesToArrays = null;
	private String classesToDictionaries = null;
	private String classesToVectors = null;
	private String classesExtendArray = null;
	private String classesExtendDictionary = null;
	private String classesExtendVector = null;

	@Override
	public void execute() throws BuildException
	{
		checkLogLevel();
		
		JavaToAS3Compiler me = new JavaToAS3Compiler();
		
		for(SourceTarget target : targets)
		{
			resetCompileOptions(me);
			File inputFile = new File(target.getSrc());
			File outputFile = new File(target.getDst());
			propOptionsFromSource(me, target);
			try
			{
				me.compileFile(inputFile, outputFile);
			}
			catch(ParseException pe)
			{
				throw new BuildException("Parse Exception: " + pe.getMessage(), pe);
				//this.handleErrorFlush("Parse Exception: " + pe.getMessage());
				//pe.printStackTrace();
			}
			catch(IOException ioe)
			{
				throw new BuildException("IOException: " + ioe.getMessage());
				//this.handleErrorFlush("IOException: " + ioe.getMessage());
				//ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Sometimes difficult to initialize Log4j from ANT, so we can
	 * override it here if the property is set.
	 */
	private void checkLogLevel()
	{
		if (logLevel != null)
		{
			Level lvl = Level.INFO;
			Logger logger = Logger.getLogger("com.sojostudios.as3");
			if (logLevel.equalsIgnoreCase("none") || logLevel.equalsIgnoreCase("off"))
			{
				lvl = Level.OFF;
			}
			if (logLevel.equalsIgnoreCase("all"))
			{
				lvl = Level.ALL;
			}
			if (logLevel.equalsIgnoreCase("debug"))
			{
				lvl = Level.DEBUG;
			}
			if (logLevel.equalsIgnoreCase("info"))
			{
				lvl = Level.INFO;
			}
			if (logLevel.equalsIgnoreCase("warn"))
			{
				lvl = Level.WARN;
			}
			if (logLevel.equalsIgnoreCase("error"))
			{
				lvl = Level.ERROR;
			}
			if (logLevel.equalsIgnoreCase("fatal"))
			{
				lvl = Level.FATAL;
			}
			logger.setLevel(lvl);
		}
	}
	
	private void resetCompileOptions(JavaToAS3Compiler me)
	{
		me.setIncludeDefaultMutations(includeDefaultMutations);
		me.setForceSprite(forceSprite);
		me.setForceMovieClip(forceMovieClip);
		me.setPackageToPackage(generateMap(packageToPackage));
		me.setClassesToClasses(generateMap(classesToClasses));
		me.setImportsToImports(generateMap(importsToImports));
		me.setImportsToIgnore(generateList(importsToIgnore));
		me.setForcedImports(generateList(forcedImports));
		me.setClassesToArrays(generateList(classesToArrays));
		me.setClassesToDictionaries(generateList(classesToDictionaries));
		me.setClassesToVectors(generateList(classesToVectors));
		me.setClassesExtendArray(generateList(classesExtendArray));
		me.setClassesExtendDictionary(generateList(classesExtendDictionary));
		me.setClassesExtendVector(generateList(classesExtendVector));
	}
	
	private void propOptionsFromSource(JavaToAS3Compiler me, SourceTarget target)
	{
		Map<String,String> ops = target.getAttributes();
		if (ops.containsKey("includedefaultmutations"))
		{
			me.setIncludeDefaultMutations(Boolean.parseBoolean(ops.get("includedefaultmutations")));
		}
		if (ops.containsKey("forcesprite"))
		{
			me.setForceSprite(Boolean.parseBoolean(ops.get("forcesprite")));
		}
		if (ops.containsKey("forcemovieclip"))
		{
			me.setForceMovieClip(Boolean.parseBoolean(ops.get("forcemovieclip")));
		}
		me.getPackageToPackage().putAll(generateMap(ops.get("packagetopackage")));
		me.getClassesToClasses().putAll(generateMap(ops.get("classestoclasses")));
		me.getImportsToImports().putAll(generateMap(ops.get("importstoimports")));
		me.getImportsToIgnore().addAll(generateList(ops.get("importstoignore")));
		me.getForcedImports().addAll(generateList(ops.get("forcedimports")));
		me.getClassesToArrays().addAll(generateList(ops.get("classestoarrays")));
		me.getClassesToDictionaries().addAll(generateList(ops.get("classestodictionaries")));
		me.getClassesToVectors().addAll(generateList(ops.get("classestovectors")));
		me.getClassesExtendArray().addAll(generateList(ops.get("classesextendarray")));
		me.getClassesExtendDictionary().addAll(generateList(ops.get("classesextenddictionary")));
		me.getClassesExtendVector().addAll(generateList(ops.get("classesextendvector")));
	}
	
	private Map<String,String> generateMap(String input)
	{
		Map<String,String> map = new HashMap<String,String>();
		if (input != null && !input.isEmpty())
		{
			String[] entries = input.split(",");
			for(String entry : entries)
			{
				String[] mapping = entry.split(":");
				map.put(mapping[0], mapping[1]);
			}
		}
		return map;
	}
	
	private List<String> generateList(String input)
	{
		List<String> list = new ArrayList<String>();
		if (input != null && !input.isEmpty())
		{
			String [] entries = input.split(",");
			for(String entry : entries)
			{
				list.add(entry);
			}
		}
		return list;
	}
	
	/**
	 * Hopefully ANT picks this up.
	 * 
	 * @param target
	 */
	public void addConfiguredSourceTarget(SourceTarget target)
	{
		targets.add(target);
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
	 * @return the packageToPackage
	 */
	public String getPackageToPackage()
	{
		return packageToPackage;
	}

	/**
	 * @param packageToPackage the packageToPackage to set
	 */
	public void setPackageToPackage(String packageToPackage)
	{
		this.packageToPackage = packageToPackage;
	}

	/**
	 * @return the classesToClasses
	 */
	public String getClassesToClasses()
	{
		return classesToClasses;
	}

	/**
	 * @param classesToClasses the classesToClasses to set
	 */
	public void setClassesToClasses(String classesToClasses)
	{
		this.classesToClasses = classesToClasses;
	}

	/**
	 * @return the importsToImports
	 */
	public String getImportsToImports()
	{
		return importsToImports;
	}

	/**
	 * @param importsToImports the importsToImports to set
	 */
	public void setImportsToImports(String importsToImports)
	{
		this.importsToImports = importsToImports;
	}

	/**
	 * @return the importsToIgnore
	 */
	public String getImportsToIgnore()
	{
		return importsToIgnore;
	}

	/**
	 * @param importsToIgnore the importsToIgnore to set
	 */
	public void setImportsToIgnore(String importsToIgnore)
	{
		this.importsToIgnore = importsToIgnore;
	}

	/**
	 * @return the forcedImports
	 */
	public String getForcedImports()
	{
		return forcedImports;
	}

	/**
	 * @param forcedImports the forcedImports to set
	 */
	public void setForcedImports(String forcedImports)
	{
		this.forcedImports = forcedImports;
	}

	/**
	 * @return the classesToArrays
	 */
	public String getClassesToArrays()
	{
		return classesToArrays;
	}

	/**
	 * @param classesToArrays the classesToArrays to set
	 */
	public void setClassesToArrays(String classesToArrays)
	{
		this.classesToArrays = classesToArrays;
	}

	/**
	 * @return the classesToDictionaries
	 */
	public String getClassesToDictionaries()
	{
		return classesToDictionaries;
	}

	/**
	 * @param classesToDictionaries the classesToDictionaries to set
	 */
	public void setClassesToDictionaries(String classesToDictionaries)
	{
		this.classesToDictionaries = classesToDictionaries;
	}

	/**
	 * @return the classesToVectors
	 */
	public String getClassesToVectors()
	{
		return classesToVectors;
	}

	/**
	 * @param classesToVectors the classesToVectors to set
	 */
	public void setClassesToVectors(String classesToVectors)
	{
		this.classesToVectors = classesToVectors;
	}

	/**
	 * @return the logLevel
	 */
	public String getLogLevel()
	{
		return logLevel;
	}

	/**
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(String logLevel)
	{
		this.logLevel = logLevel;
	}
	
	/**
	 * @return the classesExtendArray
	 */
	public String getClassesExtendArray() 
	{
		return classesExtendArray;
	}

	/**
	 * @param classesExtendArray the classesExtendArray to set
	 */
	public void setClassesExtendArray(String classesExtendArray) 
	{
		this.classesExtendArray = classesExtendArray;
	}

	/**
	 * @return the classesExtendDictionary
	 */
	public String getClassesExtendDictionary() 
	{
		return classesExtendDictionary;
	}

	/**
	 * @param classesExtendDictionary the classesExtendDictionary to set
	 */
	public void setClassesExtendDictionary(String classesExtendDictionary) 
	{
		this.classesExtendDictionary = classesExtendDictionary;
	}

	/**
	 * @return the classesExtendVector
	 */
	public String getClassesExtendVector() 
	{
		return classesExtendVector;
	}

	/**
	 * @param classesExtendVector the classesExtendVector to set
	 */
	public void setClassesExtendVector(String classesExtendVector)
	{
		this.classesExtendVector = classesExtendVector;
	}
}
