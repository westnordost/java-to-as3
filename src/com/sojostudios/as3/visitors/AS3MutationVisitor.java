package com.sojostudios.as3.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

/**
 * This class mutates the AST to remove or exchange elements from Java
 * and replace them with the AS3 versions.
 * 
 * The big mutations here relate to Arrays, Vectors, and Dictionaries.
 * 
 * @author Kurtis Kopf
 *
 */
public class AS3MutationVisitor extends ModifierVisitorAdapter<Object>
{
	public static final String ARRAY_MUTATION_FLAG = "Array";
	public static final String DICTIONARY_MUTATION_FLAG = "Dictionary";
	public static final String VECTOR_MUTATION_FLAG = "Vector";
	
	private Logger logger = Logger.getLogger(getClass());
	
	private Map<String,String> packageToPackage = new HashMap<String,String>();
	private Map<String,String> classesToClasses = new HashMap<String,String>();
	private Map<String,String> importsToImports = new HashMap<String,String>();
	private List<String> importsToIgnore = new ArrayList<String>();
	private List<String> forcedImports = new ArrayList<String>();
	private List<String> classesToArrays = new ArrayList<String>();
	private List<String> classesToDictionaries = new ArrayList<String>();
	private List<String> classesToVectors = new ArrayList<String>();
	private boolean forceSprite = false;
	private boolean forceMovieClip = false;
	
	private MutationVariableScope varScope = new MutationVariableScope();
	
	/**
	 * Constructor.
	 * 
	 * @param includeDefaults true to include the default mutations.
	 */
	public AS3MutationVisitor()
	{
	}
	
	/**
	 * Include the default mutations.
	 */
	public void includeDefaults()
	{
		forcedImports.add("flash.utils.Dictionary");
		
		importsToIgnore.add("java.*");
		importsToImports.put("java\\.util\\..*Map", "flash.utils.Dictionary");
		
		classesToClasses.put("Exception", "Error");
		classesToClasses.put("java.lang.Exception", "Error");
		classesToClasses.put("Integer", "Number");
		classesToClasses.put("java.lang.Integer", "Number");
		classesToClasses.put("Double", "Number");
		classesToClasses.put("java.lang.Double", "Number");
		classesToClasses.put("Float", "Number");
		classesToClasses.put("java.lang.Float", "Number");
		classesToClasses.put("Long", "Number");
		classesToClasses.put("java.lang.Long", "Number");
		classesToClasses.put("Short", "Number");
		classesToClasses.put("java.lang.Short", "Number");
		classesToClasses.put("Character", "String");
		classesToClasses.put("java.lang.Character", "String");
		
		// list taken from here: http://docs.oracle.com/javase/6/docs/api/java/util/Collection.html
		classesToVectors.add("Collection");
		classesToVectors.add("java.util.Collection");
		
		// interfaces
		classesToVectors.add("BeanContext");
		classesToVectors.add("java.beans.beancontext.BeanContext");
		classesToVectors.add("BeanContextServices");
		classesToVectors.add("java.beans.beancontext.BeanContextServices");
		classesToVectors.add("BlockingDeque");
		classesToVectors.add("java.util.concurrent.BlockingDeque");
		classesToVectors.add("BlockingQueue");
		classesToVectors.add("java.util.concurrent.BlockingQueue");
		classesToVectors.add("Deque");
		classesToVectors.add("java.util.Deque");
		classesToVectors.add("List");
		classesToVectors.add("java.util.List");
		classesToVectors.add("NavigableSet");
		classesToVectors.add("java.util.NavigableSet");
		classesToVectors.add("Queue");
		classesToVectors.add("java.util.Queue");
		classesToVectors.add("Set");
		classesToVectors.add("java.util.Set");
		classesToVectors.add("SortedSet");
		classesToVectors.add("java.util.SortedSet");
		
		// implementing classes
		classesToVectors.add("AbstractCollection");
		classesToVectors.add("java.util.AbstractCollection");
		classesToVectors.add("AbstractList");
		classesToVectors.add("java.util.AbstractList");
		classesToVectors.add("AbstractQueue");
		classesToVectors.add("java.util.AbstractQueue");
		classesToVectors.add("AbstractSequentialList");
		classesToVectors.add("java.util.AbstractSequentialList");
		classesToVectors.add("AbstractSet");
		classesToVectors.add("java.util.AbstractSet");
		classesToVectors.add("ArrayBlockingQueue");
		classesToVectors.add("java.util.concurrent.ArrayBlockingQueue");
		classesToVectors.add("ArrayDeque");
		classesToVectors.add("java.util.ArrayDeque");
		classesToVectors.add("ArrayList");
		classesToVectors.add("java.util.ArrayList");
		classesToVectors.add("AttributeList");
		classesToVectors.add("javax.management.AttributeList");
		classesToVectors.add("BeanContextServicesSupport");
		classesToVectors.add("java.beans.beancontext.BeanContextServicesSupport");
		classesToVectors.add("BeanContextSupport");
		classesToVectors.add("java.beans.beancontext.BeanContextSupport");
		classesToVectors.add("ConcurrentLinkedQueue");
		classesToVectors.add("java.util.concurrent.ConcurrentLinkedQueue");
		classesToVectors.add("ConcurrentSkipListSet");
		classesToVectors.add("java.util.concurrent.ConcurrentSkipListSet");
		classesToVectors.add("CopyOnWriteArrayList");
		classesToVectors.add("java.util.concurrent.CopyOnWriteArrayList");
		classesToVectors.add("CopyOnWriteArraySet");
		classesToVectors.add("java.util.concurrent.CopyOnWriteArraySet");
		classesToVectors.add("DelayQueue");
		classesToVectors.add("java.util.concurrent.DelayQueue");
		classesToVectors.add("EnumSet");
		classesToVectors.add("java.util.EnumSet");
		classesToVectors.add("HashSet");
		classesToVectors.add("java.util.HashSet");
		classesToVectors.add("JobStateReasons");
		classesToVectors.add("javax.print.attribute.standard.JobStateReasons");
		classesToVectors.add("LinkedBlockingDeque");
		classesToVectors.add("java.util.concurrent.LinkedBlockingDeque");
		classesToVectors.add("LinkedBlockingQueue");
		classesToVectors.add("java.util.concurrent.LinkedBlockingQueue");
		classesToVectors.add("LinkedHashSet");
		classesToVectors.add("java.util.LinkedHashSet");
		classesToVectors.add("LinkedList");
		classesToVectors.add("java.util.LinkedList");
		classesToVectors.add("PriorityBlockingQueue");
		classesToVectors.add("java.util.concurrent.PriorityBlockingQueue");
		classesToVectors.add("PriorityQueue");
		classesToVectors.add("java.util.PriorityQueue");
		classesToVectors.add("RoleList");
		classesToVectors.add("javax.management.relation.RoleList");
		classesToVectors.add("RoleUnresolvedList");
		classesToVectors.add("javax.management.relation.RoleUnresolvedList");
		classesToVectors.add("Stack");
		classesToVectors.add("java.util.Stack");
		classesToVectors.add("SynchronousQueue");
		classesToVectors.add("java.util.concurrent.SynchronousQueue");
		classesToVectors.add("TreeSet");
		classesToVectors.add("java.util.TreeSet");
		classesToVectors.add("Vector");
		classesToVectors.add("java.util.Vector");
		
		
		// list taken from http://docs.oracle.com/javase/6/docs/api/java/util/Map.html
		classesToDictionaries.add("Map");
		classesToDictionaries.add("java.util.Map");
		
		// interfaces
		classesToDictionaries.add("Bindings");
		classesToDictionaries.add("javax.script.Bindings");
		classesToDictionaries.add("ConcurrentMap");
		classesToDictionaries.add("java.util.concurrent.ConcurrentMap");
		classesToDictionaries.add("ConcurrentNavigableMap");
		classesToDictionaries.add("java.util.concurrent.ConcurrentNavigableMap");
		classesToDictionaries.add("LogicalMessageContext");
		classesToDictionaries.add("javax.xml.ws.handler.LogicalMessageContext");
		classesToDictionaries.add("MessageContext");
		classesToDictionaries.add("javax.xml.ws.handler.MessageContext");
		classesToDictionaries.add("NavigableMap");
		classesToDictionaries.add("java.util.NavigableMap");
		classesToDictionaries.add("SOAPMessageContext");
		classesToDictionaries.add("javax.xml.ws.handler.soap.SOAPMessageContext");
		classesToDictionaries.add("SortedMap");
		classesToDictionaries.add("java.util.SortedMap");
		
		// classes
		classesToDictionaries.add("AbstractMap");
		classesToDictionaries.add("java.util.AbstractMap");
		classesToDictionaries.add("Attributes");
		classesToDictionaries.add("java.util.jar.Attributes");
		classesToDictionaries.add("AuthProvider");
		classesToDictionaries.add("java.security.AuthProvider");
		classesToDictionaries.add("ConcurrentHashMap");
		classesToDictionaries.add("java.util.concurrent.ConcurrentHashMap");
		classesToDictionaries.add("ConcurrentSkipListMap");
		classesToDictionaries.add("java.util.concurrent.ConcurrentSkipListMap");
		classesToDictionaries.add("EnumMap");
		classesToDictionaries.add("java.util.EnumMap");
		classesToDictionaries.add("HashMap");
		classesToDictionaries.add("java.util.HashMap");
		classesToDictionaries.add("Hashtable");
		classesToDictionaries.add("java.util.Hashtable");
		classesToDictionaries.add("IdentityHashMap");
		classesToDictionaries.add("java.util.IdentityHashMap");
		classesToDictionaries.add("LinkedHashMap");
		classesToDictionaries.add("java.util.LinkedHashMap");
		classesToDictionaries.add("PrinterStateReasons");
		classesToDictionaries.add("javax.print.attribute.standard.PrinterStateReasons");
		classesToDictionaries.add("Properties");
		classesToDictionaries.add("java.util.Properties");
		classesToDictionaries.add("Provider");
		classesToDictionaries.add("java.security.Provider");
		classesToDictionaries.add("RenderingHints");
		classesToDictionaries.add("java.awt.RenderingHints");
		classesToDictionaries.add("SimpleBindings");
		classesToDictionaries.add("javax.script.SimpleBindings");
		classesToDictionaries.add("TabularDataSupport");
		classesToDictionaries.add("javax.management.openmbean.TabularDataSupport");
		classesToDictionaries.add("TreeMap");
		classesToDictionaries.add("java.util.TreeMap");
		classesToDictionaries.add("UIDefaults");
		classesToDictionaries.add("javax.swing.UIDefaults");
		classesToDictionaries.add("WeakHashMap");
		classesToDictionaries.add("java.util.WeakHashMap");
	}

	/**
	 * Manipulate imports, need to do it from up one level.
	 */
	@Override
	public Node visit(CompilationUnit n, Object arg)
	{
		if (forceSprite)
		{
			forcedImports.add("flash.display.Sprite");
		}
		if (forceMovieClip)
		{
			forcedImports.add("flash.display.MovieClip");
		}
		
		List<ImportDeclaration> addMe = new ArrayList<ImportDeclaration>();
		if (forcedImports.size() > 0)
		{
			for (String forcedImport : forcedImports)
			{
				addMe.add(new ImportDeclaration(new NameExpr(forcedImport), false, forcedImport.contains("*")));
			}
		}
		
		if (n.getImports() != null)
		{
			List<ImportDeclaration> removeMe = new ArrayList<ImportDeclaration>();
			for (ImportDeclaration i : n.getImports())
			{
				String imp = i.getName().toString();
				for (String ignore : importsToIgnore)
				{
					if (imp.matches(ignore))
					{
						logger.info("removing import " + imp);
						removeMe.add(i);
					}
				}
				for (String impFrom : importsToImports.keySet())
				{
					if (imp.matches(impFrom))
					{
						String newImport = importsToImports.get(impFrom);
						logger.info("modifying import from " + imp + " to " + newImport);
						addMe.add(new ImportDeclaration(new NameExpr(newImport), false, newImport.contains("*")));
					}
				}
			}
			n.getImports().removeAll(removeMe);
		}
		
		// make sure only one of each new/modified import gets added.
		Map<String,Boolean> used = new HashMap<String,Boolean>();
		for(ImportDeclaration imp : addMe)
		{
			String impName = imp.getName().toString();
			if (used.get(impName) == null)
			{
				used.put(impName, true);
				n.getImports().add(imp);
				logger.info("adding import " + impName);
			}
		}
		
		return super.visit(n, arg);
	}

	/**
	 * Direct class name manipulation for things like Exception -> Error.
	 */
	@Override
	public Node visit(ClassOrInterfaceType n, Object arg)
	{
		//logger.warn("class or interface type reference " + n.getName());
		
		for(String incoming : classesToClasses.keySet())
		{
			if (n.getName().matches(incoming))
			{
				String newName = classesToClasses.get(incoming);
				logger.info("changing class reference from " + n.getName() + " to " + newName);
				n.setName(newName);
			}
		}
		
		for(String incoming : classesToArrays)
		{
			if (n.getName().matches(incoming))
			{
				logger.info("changing class reference from " + n.getName() + " to Array");
				n.setName("Array");
			}
		}
		
		for(String incoming : classesToDictionaries)
		{
			if (n.getName().matches(incoming))
			{
				logger.info("changing class reference from " + n.getName() + " to Dictionary");
				n.setName("Dictionary");
			}
		}
		
		for(String incoming : classesToVectors)
		{
			if (n.getName().matches(incoming))
			{
				logger.info("changing class reference from " + n.getName() + " to Vector");
				n.setName("Vector");
			}
		}
		
		return super.visit(n, arg);
	}

	/**
	 * Look for variables that were modified to special types (Array, Dictionary) and
	 * modify method calls for them.
	 */
	@Override
	public Node visit(MethodCallExpr n, Object arg)
	{
		if (n.getScope() != null && n.getScope() instanceof NameExpr)
		{
			NameExpr callObj = (NameExpr)n.getScope();
			//logger.warn("method call scope is " + callObj.getName() + "." + n.getName());
			
			// check scope for mutations
			VarMutation mut = varScope.getVar(callObj.getName());
			if (mut != null)
			{
				//logger.warn("found mutation");
				String method = n.getName();
				// string mutations (.equals() to == [BinaryExpr])
				if (mut.type.getName().equals("String") && method.equals("equals"))
				{
					logger.info("found a string equals method reference");
					Expression right = n.getArgs().get(0);
					BinaryExpr expr = new BinaryExpr(n.getScope(), right, BinaryExpr.Operator.equals);
					return expr;
				}
				// array and dictionary mutations
				if (mut.hasFlag(ARRAY_MUTATION_FLAG) || mut.hasFlag(DICTIONARY_MUTATION_FLAG) || mut.hasFlag(VECTOR_MUTATION_FLAG))
				{
					logger.info("found mutation for " + method + " at current scope");
					// put(a, b) -> [a]=b;
					// add(a) -> push(a);
					// get(a) -> [a];
					// size() -> length;
					
					if (method.equals("put") && n.getArgs().size() > 1)
					{
						// replace with AssignmentExpr with ArrayAccessExpr and B expression
						Expression arg1 = n.getArgs().get(0);
						Expression right = n.getArgs().get(1);
						ArrayAccessExpr left = new ArrayAccessExpr(n.getScope(), arg1);
						AssignExpr expr = new AssignExpr(left, right, Operator.assign);
						return expr;
					}
					else if (method.equals("add"))
					{
						// replace method name with "push"
						n.setName("push");
						return n;
					}
					else if (method.equals("get") && n.getArgs().size() > 0)
					{
						// replace with ArrayAccessExpr
						Expression arg1 = n.getArgs().get(0);
						ArrayAccessExpr expr = new ArrayAccessExpr(n.getScope(), arg1);
						return expr;
					}
					else if (method.equals("remove") && n.getArgs().size() > 0)
					{
						if (mut.hasFlag(ARRAY_MUTATION_FLAG) || mut.hasFlag(VECTOR_MUTATION_FLAG))
						{
							// convert to splice(arg0, 1)
							n.setName("splice");
							n.getArgs().add(new IntegerLiteralExpr("1"));
						}
						else if (mut.hasFlag(DICTIONARY_MUTATION_FLAG))
						{
							// convert to delete method, dump visitor will have to convert to delete dict[key]
							n.setName("!delete");
							Expression arg1 = n.getArgs().get(0);
							ArrayAccessExpr expr = new ArrayAccessExpr(n.getScope(), arg1);
							n.getArgs().clear();
							n.getArgs().add(expr);
						}
					}
					else if (method.equals("size"))
					{
						// replace with a FieldAccessExpr
						FieldAccessExpr expr = new FieldAccessExpr(n.getScope(), "length");
						return expr;
					}
					else
					{
						logger.warn("Unhandled method " + n + " on a mutated variable");
					}
				}
			}
			else if (n.getName().equals("equals"))
			{
				logger.warn("Potentially unhandled 'equals' method call, this might not be what you want to do.");
			}
		}
		return super.visit(n, arg);
	}
	
	/**
	 * Replace any packages as defined.
	 */
	@Override
	public Node visit(PackageDeclaration n, Object arg)
	{
		String fullPkg = n.getName().toString();
		if (packageToPackage.size() > 0)
		{
			for(String key : packageToPackage.keySet())
			{
				if (fullPkg.matches(key))
				{
					NameExpr nm = new NameExpr(packageToPackage.get(key));
					n.setName(nm);
				}
			}
		}
		return super.visit(n, arg);
	}

	/**
	 * Convert Arrays and Dictionaries for variable declarations
	 */
	@Override
	public Node visit(VariableDeclarationExpr n, Object arg)
	{
		// appropriate methods with name accessors
		boolean modified = false;
		if (n.getType() instanceof ReferenceType)
		{
			ReferenceType rt = (ReferenceType)n.getType();
			
			if (rt.getType() instanceof ClassOrInterfaceType)
			{
				ClassOrInterfaceType ct = (ClassOrInterfaceType)rt.getType();
				//logger.warn("got variable declaration " + ct.getName() + " " + ct.getTypeArgs() + " " + rt.getArrayCount());
				String name = ct.getName();
				// Array conversions
				for(String classToArray : classesToArrays)
				{
					if (name.matches(classToArray))
					{
						modified = true;
						varDeclToArray(n, rt, ct);
					}
				}
				// Dictionary conversions
				for(String classToDict : classesToDictionaries)
				{
					if (name.matches(classToDict))
					{
						modified = true;
						varDeclToDictionary(n, rt, ct);
					}
				}
				// Vector conversions
				for(String classToVect : classesToVectors)
				{
					if (name.matches(classToVect))
					{
						modified = true;
						varDeclToVector(n, rt, ct);
					}
				}
				// register variable type even for unmodified vars
				if (!modified)
				{
					for(VariableDeclarator varDec : n.getVars())
					{
						// register mutation at current scope
						//logger.info("registering scope for variable " + varDec.getId().getName());
						VarMutation mut = varScope.getVarCurScopeOnly(varDec.getId().getName());
						if (mut == null)
						{
							varScope.addVar(varDec.getId().getName(), ct, null);
						}
					}
				}
			}
		}
		if (modified)
		{
			return n;
		}
		else
		{
			return super.visit(n, arg);
		}
	}
	
	/**
	 * Convert a variable declaration to an Array declaration.
	 * 
	 * @param n
	 * @param rt
	 * @param ct
	 */
	private void varDeclToArray(VariableDeclarationExpr n, ReferenceType rt, ClassOrInterfaceType ct)
	{
		logger.info("Converting variable declaration " + ct + " to Array declaration with typing info");
		// take the first typearg if it exists
		String newName = "Array";
		if (ct.getTypeArgs() != null && ct.getTypeArgs().size() > 0)
		{
			String typeArg = ct.getTypeArgs().get(0).toString();
			logger.info("taking TypeArg " + typeArg + " as new Array class type");
			newName = typeArg;
			ct.setTypeArgs(null); // wipe out TypeArgs
		}
		ct.setName(newName);
		rt.setArrayCount(1);
		
		// change initializer (should only be one, but who the hell knows)
		for(VariableDeclarator varDec : n.getVars())
		{
			// register mutation at current scope
			logger.info("registering Array mutation for variable " + varDec.getId().getName());
			VarMutation mut = varScope.getVarCurScopeOnly(varDec.getId().getName());
			if (mut != null)
			{
				if (!mut.hasFlag(ARRAY_MUTATION_FLAG))
				{	
					mut.mutationFlags.add(ARRAY_MUTATION_FLAG);
				}
			}
			else
			{
				List<String> flags = new ArrayList<String>();
				flags.add(ARRAY_MUTATION_FLAG);
				varScope.addVar(varDec.getId().getName(), ct, flags);
			}
			
			Expression init = varDec.getInit();
			if (init != null)
			{
				//logger.warn("initialization expression "  + init.getClass());
				// just destroy this sucker, replace with ArrayCreationExpression
				varDec.setInit(new ArrayCreationExpr(rt,1,null));
			}
		}
	}
	
	/**
	 * Convert a variable declaration to a Vector.
	 * This is the least complex of the conversions.
	 * @param n
	 * @param rt
	 * @param ct
	 */
	private void varDeclToVector(VariableDeclarationExpr n, ReferenceType rt, ClassOrInterfaceType ct)
	{
		logger.info("Converting variable declaration " + ct + " to Array declaration with typing info");
		
		ct.setName("Vector");
		
		for(VariableDeclarator varDec : n.getVars())
		{
			// register mutation at current scope
			logger.info("registering Vector mutation for variable " + varDec.getId().getName());
			VarMutation mut = varScope.getVarCurScopeOnly(varDec.getId().getName());
			if (mut != null)
			{
				if (!mut.hasFlag(VECTOR_MUTATION_FLAG))
				{	
					mut.mutationFlags.add(VECTOR_MUTATION_FLAG);
				}
			}
			else
			{
				List<String> flags = new ArrayList<String>();
				flags.add(VECTOR_MUTATION_FLAG);
				varScope.addVar(varDec.getId().getName(), ct, flags);
			}
			
			Expression init = varDec.getInit();
			if (init != null)
			{
				//logger.warn("initialization expression "  + init.getClass());
				if (init instanceof ObjectCreationExpr)
				{
					ObjectCreationExpr oce = (ObjectCreationExpr) init;
					oce.setType(ct);
					oce.setTypeArgs(null);
				}
			}
		}
	}
	
	/**
	 * Convert a variable declaration to a Dictionary declaration.
	 * 
	 * @param n
	 * @param rt
	 * @param ct
	 */
	private void varDeclToDictionary(VariableDeclarationExpr n, ReferenceType rt, ClassOrInterfaceType ct)
	{
		logger.info("Converting variable declaration " + ct + " to Dictionary declaration without typing");
		// take the first typearg if it exists
		String newName = "Dictionary";
		//ct.setTypeArgs(null);
		ct.setName(newName);
		
		// change initializer (should only be one, but who the hell knows)
		for(VariableDeclarator varDec : n.getVars())
		{
			// register mutation at current scope
			logger.info("registering Dictionary mutation for variable " + varDec.getId().getName());
			VarMutation mut = varScope.getVarCurScopeOnly(varDec.getId().getName());
			if (mut != null)
			{
				if (!mut.hasFlag(DICTIONARY_MUTATION_FLAG))
				{	
					mut.mutationFlags.add(DICTIONARY_MUTATION_FLAG);
				}
			}
			else
			{
				List<String> flags = new ArrayList<String>();
				flags.add(DICTIONARY_MUTATION_FLAG);
				varScope.addVar(varDec.getId().getName(), ct, flags);
			}
			
			Expression init = varDec.getInit();
			if (init != null)
			{
				//logger.warn("initialization expression "  + init.getClass());
				if (init instanceof ObjectCreationExpr)
				{
					ObjectCreationExpr oce = (ObjectCreationExpr) init;
					oce.setType(new ClassOrInterfaceType(newName));
					oce.setTypeArgs(null);
				}
			}
		}
	}

	/**
	 * Just track variable scopes here.
	 */
	@Override
	public Node visit(BlockStmt n, Object arg)
	{
		varScope.pushScopeStack();
		Node result = super.visit(n, arg);
		varScope.popScopeStack();
		return result;
	}

	/**
	 * Track variable scopes, also force Sprite extension if enabled.
	 */
	@Override
	public Node visit(ClassOrInterfaceDeclaration n, Object arg)
	{
		varScope.pushScopeStack();
		
		if (n.getExtends() == null && (forceSprite || forceMovieClip))
		{
			List<ClassOrInterfaceType> ext = new ArrayList<ClassOrInterfaceType>();
			ClassOrInterfaceType sprite = new ClassOrInterfaceType( forceSprite ? "Sprite" : "MovieClip");
			ext.add(sprite);
			n.setExtends(ext);
		}
		
		Node result = super.visit(n, arg);
		varScope.popScopeStack();
		return result;
	}

	/**
	 * Track parameter variable scope and mutations.
	 */
	@Override
	public Node visit(Parameter n, Object arg)
	{
		//logger.error("found parameter " + n.getId().getName() + " of type " + n.getType().getClass());
		if (n.getType() instanceof ReferenceType)
		{
			ReferenceType rt = (ReferenceType)n.getType();
			if (rt.getType() instanceof ClassOrInterfaceType)
			{
				ClassOrInterfaceType ct = (ClassOrInterfaceType)rt.getType();
				String type = ct.getName();
				List<String> flags = new ArrayList<String>();
				for(String classToArray : classesToArrays)
				{
					if (type.matches(classToArray))
					{
						flags.add(ARRAY_MUTATION_FLAG);
					}
				}
				// Dictionary conversions
				for(String classToDict : classesToDictionaries)
				{
					if (type.matches(classToDict))
					{
						flags.add(DICTIONARY_MUTATION_FLAG);
					}
				}
				// Vector conversions
				for(String classToVect : classesToVectors)
				{
					if (type.matches(classToVect))
					{
						flags.add(VECTOR_MUTATION_FLAG);
					}
				}
				varScope.addVar(n.getId().getName(), ct, flags);
			}
		}
		return super.visit(n, arg);
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
}