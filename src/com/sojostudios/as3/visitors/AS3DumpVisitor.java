package com.sojostudios.as3.visitors;

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import japa.parser.ast.visitor.VoidVisitor;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class dumps the AST for the CompilationUnit to a String using
 * the AS3 syntax.
 * 
 * @author Kurtis Kopf
 *
 */
public class AS3DumpVisitor implements VoidVisitor<Object>
{
	private Logger logger = Logger.getLogger(getClass());
	private IndentedPrinter printer = new IndentedPrinter();
	
	public AS3DumpVisitor()
	{
	}

	@Override
	public String toString()
	{
		return printer.toString();
	}

	/**
	 * Print modifiers for a token based on flags.
	 * @param modifiers
	 */
	private void printModifiers(int modifiers, boolean field)
	{
		if (ModifierSet.isPrivate(modifiers))
		{
			printer.print("private ");
		}
		if (ModifierSet.isProtected(modifiers))
		{
			printer.print("protected ");
		}
		if (ModifierSet.isPublic(modifiers))
		{
			printer.print("public ");
		}
		if (ModifierSet.isAbstract(modifiers))
		{
			printer.print("abstract ");
		}
		if (ModifierSet.isStatic(modifiers))
		{
			printer.print("static ");
		}
		if (ModifierSet.isFinal(modifiers))
		{
			if (field)
			{
				logger.warn("no final modifier for fields, changing to const");
				printer.print("const ");
			}
			else
			{
				printer.print("final ");
			}
		}
		if (ModifierSet.isNative(modifiers))
		{
			logger.warn("Ignoring modifier 'native'");
			// printer.print("native ");
		}
		if (ModifierSet.isStrictfp(modifiers))
		{
			logger.warn("Ignoring modifier 'strictfp'");
			// printer.print("strictfp ");
		}
		if (ModifierSet.isSynchronized(modifiers))
		{
			logger.warn("Ignoring modifier 'synchronized'");
			// printer.print("synchronized ");
		}
		if (ModifierSet.isTransient(modifiers))
		{
			logger.warn("Ignoring modifier 'transient'");
			// printer.print("transient ");
		}
		if (ModifierSet.isVolatile(modifiers))
		{
			logger.warn("Ignoring modifier 'volatile'");
			// printer.print("volatile ");
		}
	}
	
	private void printMembers(List<BodyDeclaration> members, Object arg)
	{
		for (BodyDeclaration member : members)
		{
			printer.printLn();
			member.accept(this, arg);
			printer.printLn();
		}
	}

	private void printMemberAnnotations(List<AnnotationExpr> annotations, Object arg)
	{
		if (annotations != null)
		{
			for (AnnotationExpr a : annotations)
			{
				a.accept(this, arg);
				printer.printLn();
			}
		}
	}

	private void printAnnotations(List<AnnotationExpr> annotations, Object arg)
	{
		if (annotations != null)
		{
			for (AnnotationExpr a : annotations)
			{
				a.accept(this, arg);
				printer.print(" ");
			}
		}
	}

	private void printTypeArgs(String clazz, List<Type> args, Object arg)
	{
		if (args != null)
		{
			boolean comment = true;
			// only vectors have typeargs in AS3
			if (clazz != null && clazz.equals("Vector"))
			{
				comment = false;
			}
			// TODO: comments fail with embedded generics 
			// ie: HashMap<String,Map<String,String>> => HashMap/*<String,Map/*<String,String>*/>*/ => parse error
			// remove if(!comment) when this is fixed
			if (!comment)
			{
				printer.print((comment?"/*":".") + "<");
				for (Iterator<Type> i = args.iterator(); i.hasNext();)
				{
					Type t = i.next();
					t.accept(this, arg);
					if (i.hasNext())
					{
						printer.print(", ");
					}
				}
				printer.print(">"+(comment?"*/":""));
			}
		}
	}

	private void printTypeParameters(String clazz, List<TypeParameter> args, Object arg)
	{
		if (args != null)
		{
			// TODO: this also fails with embedded generics
			// uncomment after fixed
			
			//printer.print("/*<");
			//for (Iterator<TypeParameter> i = args.iterator(); i.hasNext();)
			//{
			//	TypeParameter t = i.next();
			//	t.accept(this, arg);
			//	if (i.hasNext())
			//	{
			//		printer.print(", ");
			//	}
			//}
			//printer.print(">*/");
		}
	}

	private void printArguments(List<Expression> args, Object arg)
	{
		printer.print("(");
		if (args != null)
		{
			for (Iterator<Expression> i = args.iterator(); i.hasNext();)
			{
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.print(")");
	}

	private void printJavadoc(JavadocComment javadoc, Object arg)
	{
		if (javadoc != null)
		{
			javadoc.accept(this, arg);
		}
	}
	
	private void printNumeric(String value)
	{
		// remove any type-hinted suffixes (F,D,L, etc)
		printer.print(value.replaceAll("[dflDFL]", ""));
		//printer.print(value);
	}

	@Override
	public void visit(CompilationUnit n, Object arg)
	{
		if (n.getPackage() != null)
		{
			n.getPackage().accept(this, arg);
		}
		else
		{
			printer.printLn("package");
		}
		printer.printLn("{");
		printer.indent();
		
		if (n.getImports() != null)
		{
			for (ImportDeclaration i : n.getImports())
			{
				i.accept(this, arg);
			}
			printer.printLn();
		}
		if (n.getTypes() != null)
		{
			for (Iterator<TypeDeclaration> i = n.getTypes().iterator(); i.hasNext();)
			{
				i.next().accept(this, arg);
				printer.printLn();
				if (i.hasNext())
				{
					printer.printLn();
				}
			}
		}
		printer.unindent();
		printer.printLn("}");
	}

	@Override
	public void visit(PackageDeclaration n, Object arg)
	{
		//printAnnotations(n.getAnnotations(), arg);
		printer.print("package ");
		n.getName().accept(this, arg);
		//printer.printLn(";");
		printer.printLn();
	}

	@Override
	public void visit(ImportDeclaration n, Object arg)
	{
		printer.print("import ");
		if (n.isStatic())
		{
			printer.print("static ");
		}
		
		n.getName().accept(this, arg);
		if (n.isAsterisk())
		{
			logger.warn("starred imports are scary");
			printer.print(".*");
		}
		printer.printLn(";");
	}

	@Override
	public void visit(TypeParameter n, Object arg)
	{
		printer.print(n.getName());
		if (n.getTypeBound() != null)
		{
			printer.print(" extends ");
			for (Iterator<ClassOrInterfaceType> i = n.getTypeBound().iterator(); i.hasNext();)
			{
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(" & ");
				}
			}
		}
	}

	@Override
	public void visit(LineComment n, Object arg1)
	{
		printer.print("//");
		printer.printLn(n.getContent());
	}

	@Override
	public void visit(BlockComment n, Object arg1)
	{
		printer.print("/*");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, Object arg)
	{
		printJavadoc(n.getJavaDoc(), arg);
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers(), false);

		if (n.isInterface())
		{
			printer.print("interface ");
		}
		else
		{
			printer.print("class ");
		}

		printer.print(n.getName());

		printTypeParameters(n.getName(), n.getTypeParameters(), arg);

		if (n.getExtends() != null)
		{
			printer.print(" extends ");
			for (Iterator<ClassOrInterfaceType> i = n.getExtends().iterator(); i.hasNext();)
			{
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}

		if (n.getImplements() != null)
		{
			printer.print(" implements ");
			for (Iterator<ClassOrInterfaceType> i = n.getImplements()
					.iterator(); i.hasNext();)
			{
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.printLn();
		printer.printLn("{");
		printer.indent();
		if (n.getMembers() != null)
		{
			printMembers(n.getMembers(), arg);
		}
		printer.unindent();
		printer.print("}");
	}

	@Override
	public void visit(EnumDeclaration arg0, Object arg1)
	{
		logger.error("Enums are not support in AS3, your code will probably break now.");
	}

	@Override
	public void visit(EmptyTypeDeclaration n, Object arg)
	{
		printJavadoc(n.getJavaDoc(), arg);
		printer.print(";");
	}

	@Override
	public void visit(EnumConstantDeclaration arg0, Object arg1)
	{
		logger.error("EnumConstants are not support in AS3, your code will probably break now.");
	}

	@Override
	public void visit(AnnotationDeclaration arg0, Object arg1)
	{
		logger.error("Annotations are not support in AS3, your code will probably break now.");
	}

	@Override
	public void visit(AnnotationMemberDeclaration arg0, Object arg1)
	{
		logger.error("Annotation Members are not support in AS3, your code will probably break now.");
	}

	@Override
	public void visit(FieldDeclaration n, Object arg)
	{
		// private var myVar:MyType = x;
		printJavadoc(n.getJavaDoc(), arg);
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers(), true);
		
		if (ModifierSet.isFinal(n.getModifiers()))
		{
			// ignore var
		}
		else
		{
			printer.print("var ");
		}
		
		for (Iterator<VariableDeclarator> i = n.getVariables().iterator(); i.hasNext();)
		{
			VariableDeclarator var = i.next();
			
			//var.getId().accept(this, arg);
			
			printer.print(var.getId().getName());
			printer.print(":");
			if (var.getId().getArrayCount() > 0)
			{
				printer.print("Array");
			}
			else
			{
				n.getType().accept(this, arg);
			}
			
			if (var.getInit() != null)
			{
				printer.print(" = ");
				var.getInit().accept(this, arg);
			}
			
			//var.accept(this, arg);
			if (i.hasNext())
			{
				printer.print(", ");
			}
		}
		printer.print(";");
	}

	@Override
	public void visit(VariableDeclarator n, Object arg)
	{
		n.getId().accept(this, arg);
		if (n.getInit() != null)
		{
			printer.print(" = ");
			n.getInit().accept(this, arg);
		}
	}

	@Override
	public void visit(VariableDeclaratorId n, Object arg)
	{
		printer.print(n.getName());
	}

	@Override
	public void visit(ConstructorDeclaration n, Object arg)
	{
		printJavadoc(n.getJavaDoc(), arg);
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers(), false);
		printer.print("function ");

		printTypeParameters(n.getName(), n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null)
		{
			printer.print(" ");
		}
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null)
		{
			for (Iterator<Parameter> i = n.getParameters().iterator(); i
					.hasNext();)
			{
				Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.print(")");

		// no throws declaration in AS3
		if (n.getThrows() != null)
		{
			printer.print("/* throws ");
			for (Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();)
			{
				NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
			printer.print("*/");
		}
		printer.printLn();
		n.getBlock().accept(this, arg);
	}

	@Override
	public void visit(MethodDeclaration n, Object arg)
	{
		printJavadoc(n.getJavaDoc(), arg);
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers(), false);

		printTypeParameters(n.getName(), n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null)
		{
			printer.print(" ");
		}

		
		printer.print("function ");
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null)
		{
			for (Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext();)
			{
				Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.print("):");
		
		n.getType().accept(this, arg);

		/*for (int i = 0; i < n.getArrayCount(); i++)
		{
			printer.print("[]");
		}*/

		if (n.getThrows() != null)
		{
			printer.print(" /*throws ");
			for (Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();)
			{
				NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
			printer.print("*/");
		}
		if (n.getBody() == null)
		{
			printer.print(";");
		}
		else
		{
			printer.printLn();
			n.getBody().accept(this, arg);
		}
	}

	@Override
	public void visit(Parameter n, Object arg)
	{
		printAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers(), true);

        if (n.isVarArgs()) {
            printer.print("...");
        }
        printer.print(" ");
        n.getId().accept(this, arg);
        printer.print(":");
        n.getType().accept(this, arg);
	}

	@Override
	public void visit(EmptyMemberDeclaration n, Object arg)
	{
		printJavadoc(n.getJavaDoc(), arg);
		printer.print(";");
	}

	@Override
	public void visit(InitializerDeclaration n, Object arg)
	{
		// not even sure if you can do this in AS3
		printJavadoc(n.getJavaDoc(), arg);
		if (n.isStatic())
		{
			printer.print("static ");
		}
		n.getBlock().accept(this, arg);
	}

	@Override
	public void visit(JavadocComment n, Object arg)
	{
		printer.print("/**");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

	@Override
	public void visit(ClassOrInterfaceType n, Object arg)
	{
		if (n.getScope() != null)
		{
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printer.print(n.getName());
		printTypeArgs(n.getName(), n.getTypeArgs(), arg);
	}

	@Override
	public void visit(PrimitiveType n, Object arg)
	{
		switch (n.getType())
		{
			case Boolean:
				printer.print("Boolean");
				break;
			case Byte:
				logger.warn("Replacing 'byte' with 'uint' for primitive type.");
				printer.print("uint");
				break;
			case Char:
				// no char type in AS3
				logger.warn("Replacing 'char' with 'String' for primitive type.");
				printer.print("String");
				break;
			case Double:
				logger.warn("Replacing 'double' with 'Number' for primitive type.");
				printer.print("Number");
				break;
			case Float:
				logger.warn("Replacing 'float' with 'Number' for primitive type.");
				printer.print("Number");
				break;
			case Int:
				printer.print("int");
				break;
			case Long:
				logger.warn("Replacing 'long' with 'Number' for primitive type.");
				printer.print("Number");
				break;
			case Short:
				logger.warn("Replacing 'short' with 'int' for primitive type.");
				printer.print("int");
				break;
		}
	}

	@Override
	public void visit(ReferenceType n, Object arg)
	{
		if (n.getArrayCount() > 0)
		{
			printer.print("Array");
		}
		else
		{
			n.getType().accept(this, arg);
		}
	}

	@Override
	public void visit(VoidType n, Object arg)
	{
		printer.print("void");
	}

	@Override
	public void visit(WildcardType n, Object arg)
	{
		printer.print("*");
		/*printer.print("?");
		if (n.getExtends() != null)
		{
			printer.print(" extends ");
			n.getExtends().accept(this, arg);
		}
		if (n.getSuper() != null)
		{
			printer.print(" super ");
			n.getSuper().accept(this, arg);
		}*/
	}

	@Override
	public void visit(ArrayAccessExpr n, Object arg)
	{
		n.getName().accept(this, arg);
		printer.print("[");
		n.getIndex().accept(this, arg);
		printer.print("]");
	}

	@Override
	public void visit(ArrayCreationExpr n, Object arg)
	{
		printer.print("new Array(");
		if (n.getInitializer() != null)
		{
			n.getInitializer().accept(this, arg);
		}
		printer.print(")");
		/*n.getType().accept(this, arg);

		if (n.getDimensions() != null)
		{
			for (Expression dim : n.getDimensions())
			{
				printer.print("[");
				dim.accept(this, arg);
				printer.print("]");
			}
			for (int i = 0; i < n.getArrayCount(); i++)
			{
				printer.print("[]");
			}
		}
		else
		{
			for (int i = 0; i < n.getArrayCount(); i++)
			{
				printer.print("[]");
			}
			printer.print(" ");
			n.getInitializer().accept(this, arg);
		}*/
	}

	@Override
	public void visit(ArrayInitializerExpr n, Object arg)
	{
		printer.print("[");
		if (n.getValues() != null)
		{
			printer.print(" ");
			for (Iterator<Expression> i = n.getValues().iterator(); i.hasNext();)
			{
				Expression expr = i.next();
				expr.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
			printer.print(" ");
		}
		printer.print("]");
	}

	@Override
	public void visit(AssignExpr n, Object arg)
	{
		n.getTarget().accept(this, arg);
		printer.print(" ");
		switch (n.getOperator())
		{
			case assign:
				printer.print("=");
				break;
			case and:
				printer.print("&=");
				break;
			case or:
				printer.print("|=");
				break;
			case xor:
				printer.print("^=");
				break;
			case plus:
				printer.print("+=");
				break;
			case minus:
				printer.print("-=");
				break;
			case rem:
				printer.print("%=");
				break;
			case slash:
				printer.print("/=");
				break;
			case star:
				printer.print("*=");
				break;
			case lShift:
				printer.print("<<=");
				break;
			case rSignedShift:
				printer.print(">>=");
				break;
			case rUnsignedShift:
				printer.print(">>>=");
				break;
		}
		printer.print(" ");
		n.getValue().accept(this, arg);
	}

	@Override
	public void visit(BinaryExpr n, Object arg)
	{
		n.getLeft().accept(this, arg);
		printer.print(" ");
		switch (n.getOperator())
		{
			case or:
				printer.print("||");
				break;
			case and:
				printer.print("&&");
				break;
			case binOr:
				printer.print("|");
				break;
			case binAnd:
				printer.print("&");
				break;
			case xor:
				printer.print("^");
				break;
			case equals:
				printer.print("==");
				break;
			case notEquals:
				printer.print("!=");
				break;
			case less:
				printer.print("<");
				break;
			case greater:
				printer.print(">");
				break;
			case lessEquals:
				printer.print("<=");
				break;
			case greaterEquals:
				printer.print(">=");
				break;
			case lShift:
				printer.print("<<");
				break;
			case rSignedShift:
				printer.print(">>");
				break;
			case rUnsignedShift:
				printer.print(">>>");
				break;
			case plus:
				printer.print("+");
				break;
			case minus:
				printer.print("-");
				break;
			case times:
				printer.print("*");
				break;
			case divide:
				printer.print("/");
				break;
			case remainder:
				printer.print("%");
				break;
		}
		printer.print(" ");
		n.getRight().accept(this, arg);
	}

	@Override
	public void visit(CastExpr n, Object arg)
	{
		n.getType().accept(this, arg);
		printer.print("(");
		n.getExpr().accept(this, arg);
		printer.print(") ");
	}

	@Override
	public void visit(ClassExpr n, Object arg)
	{
		printer.print("Class(");
		n.getType().accept(this, arg);
		printer.print(")");
		// printer.print(".class");
	}

	@Override
	public void visit(ConditionalExpr n, Object arg)
	{
		n.getCondition().accept(this, arg);
		printer.print(" ? ");
		n.getThenExpr().accept(this, arg);
		printer.print(" : ");
		n.getElseExpr().accept(this, arg);
	}

	@Override
	public void visit(EnclosedExpr n, Object arg)
	{
		printer.print("(");
		n.getInner().accept(this, arg);
		printer.print(")");
	}

	@Override
	public void visit(FieldAccessExpr n, Object arg)
	{
		n.getScope().accept(this, arg);
		printer.print(".");
		printer.print(n.getField());
	}

	@Override
	public void visit(InstanceOfExpr n, Object arg)
	{
		n.getExpr().accept(this, arg);
		printer.print(" is ");
		n.getType().accept(this, arg);
	}

	@Override
	public void visit(StringLiteralExpr n, Object arg)
	{
		printer.print("\"");
		printer.print(n.getValue());
		printer.print("\"");
	}

	@Override
	public void visit(IntegerLiteralExpr n, Object arg1)
	{
		printer.print(n.getValue());
	}

	@Override
	public void visit(LongLiteralExpr n, Object arg1)
	{
		// remove any non-numeric type-hinting suffixes (D,L,F, etc)
		printNumeric(n.getValue());
		//printer.print(n.getValue());
	}

	@Override
	public void visit(IntegerLiteralMinValueExpr n, Object arg1)
	{
		printer.print(n.getValue());
	}

	@Override
	public void visit(LongLiteralMinValueExpr n, Object arg1)
	{
		printer.print(n.getValue());
	}

	@Override
	public void visit(CharLiteralExpr n, Object arg)
	{
		// no character literals in AS3, only strings.
		printer.print("\"");
		printer.print(n.getValue());
		printer.print("\"");
	}

	@Override
	public void visit(DoubleLiteralExpr n, Object arg1)
	{
		// remove any non-numeric type-hinting suffixes (D,L,F, etc)
		printNumeric(n.getValue());
		//logger.error("Double Value: " + n.getValue());
		//printer.print(n.getValue());
	}

	@Override
	public void visit(BooleanLiteralExpr n, Object arg1)
	{
		printer.print(n.getValue() ? "true" : "false");
	}

	@Override
	public void visit(NullLiteralExpr arg0, Object arg1)
	{
		printer.print("null");
	}

	@Override
	public void visit(MethodCallExpr n, Object arg)
	{
		// this is a special hack since there's no 'delete' keyword in java
		// for removing an element from a Map/Dictionary
		// ![a-z] is an invalid method name in Java
		if (n.getName().equals("!delete") && n.getArgs().size() > 0)
		{
			printer.print("delete ");
			n.getArgs().get(0).accept(this, arg);
			return;
		}
		
		if (n.getScope() != null)
		{
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printTypeArgs(n.getName(), n.getTypeArgs(), arg);
		printer.print(n.getName());
		printArguments(n.getArgs(), arg);
	}

	@Override
	public void visit(NameExpr n, Object arg1)
	{
		printer.print(n.getName());
	}

	@Override
	public void visit(ObjectCreationExpr n, Object arg)
	{
		if (n.getScope() != null)
		{
			n.getScope().accept(this, arg);
			printer.print(".");
		}

		printer.print("new ");
		
		n.getType().accept(this, arg);
		printTypeArgs(n.getType().getName(), n.getTypeArgs(), arg);

		printArguments(n.getArgs(), arg);

		if (n.getAnonymousClassBody() != null)
		{
			printer.printLn(" {");
			printer.indent();
			printMembers(n.getAnonymousClassBody(), arg);
			printer.unindent();
			printer.print("}");
		}
	}

	@Override
	public void visit(QualifiedNameExpr n, Object arg)
	{
		n.getQualifier().accept(this, arg);
		printer.print(".");
		printer.print(n.getName());
	}

	@Override
	public void visit(ThisExpr n, Object arg)
	{
		if (n.getClassExpr() != null)
		{
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("this");
	}

	@Override
	public void visit(SuperExpr n, Object arg)
	{
		if (n.getClassExpr() != null)
		{
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("super");
	}

	@Override
	public void visit(UnaryExpr n, Object arg)
	{
		switch (n.getOperator())
		{
			case positive:
				printer.print("+");
				break;
			case negative:
				printer.print("-");
				break;
			case inverse:
				printer.print("~");
				break;
			case not:
				printer.print("!");
				break;
			case preIncrement:
				printer.print("++");
				break;
			case preDecrement:
				printer.print("--");
				break;
			default:
				//logger.warn("out of context unary operator " + n.getOperator());
				break;
		}

		n.getExpr().accept(this, arg);

		switch (n.getOperator())
		{
			case posIncrement:
				printer.print("++");
				break;
			case posDecrement:
				printer.print("--");
				break;
			default:
				//logger.warn("out of context unary operator " + n.getOperator());
				break;
		}
	}

	@Override
	public void visit(VariableDeclarationExpr n, Object arg)
	{
		// if we're declaring an array, we can meta-type it for compile time checking.
		if (n.getType() instanceof ReferenceType)
		{
			ReferenceType rt = (ReferenceType)n.getType();
			if (rt.getArrayCount() > 0)
			{
				printer.printLn("[ArrayElementType(\"" + rt.getType() + "\")]");
			}
		}
		
		printAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers(), true);
		
		printer.print("var ");

		//n.getType().accept(this, arg);
		//printer.print(" ");

		for (Iterator<VariableDeclarator> i = n.getVars().iterator(); i.hasNext();)
		{
			VariableDeclarator v = i.next();
			
			//v.accept(this, arg);
			
			v.getId().accept(this, arg);
			printer.print(":");
			n.getType().accept(this, arg);
			
			if (v.getInit() != null)
			{
				printer.print(" = ");
				v.getInit().accept(this, arg);
			}
			
			if (i.hasNext())
			{
				printer.print(", ");
			}
		}
	}

	@Override
	public void visit(MarkerAnnotationExpr n, Object arg)
	{
		printer.print("[");
		n.getName().accept(this, arg);
		printer.print("]");
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n, Object arg)
	{
		printer.print("[");
		n.getName().accept(this, arg);
		printer.print("(");
		n.getMemberValue().accept(this, arg);
		printer.print(")]");
	}

	@Override
	public void visit(NormalAnnotationExpr n, Object arg)
	{
		printer.print("[");
		n.getName().accept(this, arg);
		printer.print("(");
		if (n.getPairs() != null)
		{
			for (Iterator<MemberValuePair> i = n.getPairs().iterator(); i.hasNext();)
			{
				MemberValuePair m = i.next();
				m.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.print(")]");
	}

	@Override
	public void visit(MemberValuePair n, Object arg)
	{
		printer.print(n.getName());
		printer.print(" = ");
		n.getValue().accept(this, arg);
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Object arg)
	{
		if (n.isThis())
		{
			printTypeArgs("this", n.getTypeArgs(), arg);
			printer.print("this");
		}
		else
		{
			if (n.getExpr() != null)
			{
				n.getExpr().accept(this, arg);
				printer.print(".");
			}
			printTypeArgs("super", n.getTypeArgs(), arg);
			printer.print("super");
		}
		printArguments(n.getArgs(), arg);
		printer.print(";");
	}

	@Override
	public void visit(TypeDeclarationStmt n, Object arg)
	{
		n.getTypeDeclaration().accept(this, arg);
	}

	@Override
	public void visit(AssertStmt n, Object arg)
	{
		logger.error("Assert statements are unsupported in AS3.");
		/*printer.print("assert ");
		n.getCheck().accept(this, arg);
		if (n.getMessage() != null)
		{
			printer.print(" : ");
			n.getMessage().accept(this, arg);
		}
		printer.print(";");*/
	}

	@Override
	public void visit(BlockStmt n, Object arg)
	{
		printer.printLn("{");
		if (n.getStmts() != null)
		{
			printer.indent();
			for (Statement s : n.getStmts())
			{
				s.accept(this, arg);
				printer.printLn();
			}
			printer.unindent();
		}
		printer.print("}");
	}

	@Override
	public void visit(LabeledStmt n, Object arg)
	{
		logger.warn("Why are you using labels?!?!");
		printer.print(n.getLabel());
        printer.print(": ");
        n.getStmt().accept(this, arg);
	}

	@Override
	public void visit(EmptyStmt arg0, Object arg1)
	{
		printer.print(";");
	}

	@Override
	public void visit(ExpressionStmt n, Object arg)
	{
		n.getExpression().accept(this, arg);
		printer.print(";");
	}

	@Override
	public void visit(SwitchStmt n, Object arg)
	{
		printer.print("switch(");
		n.getSelector().accept(this, arg);
		printer.printLn(") {");
		if (n.getEntries() != null)
		{
			printer.indent();
			for (SwitchEntryStmt e : n.getEntries())
			{
				e.accept(this, arg);
			}
			printer.unindent();
		}
		printer.print("}");
	}

	@Override
	public void visit(SwitchEntryStmt n, Object arg)
	{
		if (n.getLabel() != null)
		{
			printer.print("case ");
			n.getLabel().accept(this, arg);
			printer.print(":");
		}
		else
		{
			printer.print("default:");
		}
		printer.printLn();
		printer.indent();
		if (n.getStmts() != null)
		{
			for (Statement s : n.getStmts())
			{
				s.accept(this, arg);
				printer.printLn();
			}
		}
		printer.unindent();
	}

	@Override
	public void visit(BreakStmt n, Object arg)
	{
		printer.print("break");
		if (n.getId() != null)
		{
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}

	@Override
	public void visit(ReturnStmt n, Object arg)
	{
		printer.print("return");
		if (n.getExpr() != null)
		{
			printer.print(" ");
			n.getExpr().accept(this, arg);
		}
		printer.print(";");
	}

	@Override
	public void visit(IfStmt n, Object arg)
	{
		printer.print("if (");
		n.getCondition().accept(this, arg);
		printer.printLn(") ");
		n.getThenStmt().accept(this, arg);
		if (n.getElseStmt() != null)
		{
			printer.printLn(" else ");
			n.getElseStmt().accept(this, arg);
		}
	}

	@Override
	public void visit(WhileStmt n, Object arg)
	{
		printer.print("while (");
		n.getCondition().accept(this, arg);
		printer.printLn(") ");
		n.getBody().accept(this, arg);
	}

	@Override
	public void visit(ContinueStmt n, Object arg)
	{
		printer.print("continue");
		if (n.getId() != null)
		{
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}

	@Override
	public void visit(DoStmt n, Object arg)
	{
		printer.printLn("do ");
		n.getBody().accept(this, arg);
		printer.print(" while (");
		n.getCondition().accept(this, arg);
		printer.print(");");
	}

	@Override
	public void visit(ForeachStmt n, Object arg)
	{
		logger.error("Detected ForEach style for loop... this probably won't convert correctly. Use standard FOR statements.");
		printer.print("for (");
		n.getVariable().accept(this, arg);
		printer.print(" in ");
		n.getIterable().accept(this, arg);
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	@Override
	public void visit(ForStmt n, Object arg)
	{
		printer.print("for (");
		if (n.getInit() != null)
		{
			for (Iterator<Expression> i = n.getInit().iterator(); i.hasNext();)
			{
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.print("; ");
		if (n.getCompare() != null)
		{
			n.getCompare().accept(this, arg);
		}
		printer.print("; ");
		if (n.getUpdate() != null)
		{
			for (Iterator<Expression> i = n.getUpdate().iterator(); i.hasNext();)
			{
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext())
				{
					printer.print(", ");
				}
			}
		}
		printer.printLn(") ");
		n.getBody().accept(this, arg);
	}

	@Override
	public void visit(ThrowStmt n, Object arg)
	{
		printer.print("throw ");
		n.getExpr().accept(this, arg);
		printer.print(";");
	}

	@Override
	public void visit(SynchronizedStmt n, Object arg)
	{
		logger.error("Synchronized blocks do not work in AS3");
		printer.print("/*synchronized (");
		n.getExpr().accept(this, arg);
		printer.print(")*/ ");
		n.getBlock().accept(this, arg);
	}

	@Override
	public void visit(TryStmt n, Object arg)
	{
		printer.print("try ");
		n.getTryBlock().accept(this, arg);
		if (n.getCatchs() != null)
		{
			for (CatchClause c : n.getCatchs())
			{
				c.accept(this, arg);
			}
		}
		if (n.getFinallyBlock() != null)
		{
			printer.print(" finally ");
			n.getFinallyBlock().accept(this, arg);
		}
	}

	@Override
	public void visit(CatchClause n, Object arg)
	{
		printer.print(" catch (");
		n.getExcept().accept(this, arg);
		printer.printLn(") ");
		n.getCatchBlock().accept(this, arg);
	}

}
