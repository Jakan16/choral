package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.CompilationUnit;
import org.choral.ast.body.Class;

import java.util.Collection;

/**
 * Holds all known packages and classes
 */
public class PackageHandler {

	private final Package root;

	public final static String langPath = "java.lang";
	private final static String BOOLEAN = "Boolean";
	private final static String BYTE = "Byte";
	private final static String CHARACTER = "Character";
	private final static String SHORT = "Short";
	private final static String INTEGER = "Integer";
	private final static String LONG = "Long";
	private final static String FLOAT = "Float";
	private final static String DOUBLE = "Double";

	private final static String PRIM_BOOLEAN = "boolean";
	private final static String PRIM_BYTE = "byte";
	private final static String PRIM_CHARACTER = "char";
	private final static String PRIM_SHORT = "short";
	private final static String PRIM_INTEGER = "int";
	private final static String PRIM_LONG = "long";
	private final static String PRIM_FLOAT = "float";
	private final static String PRIM_DOUBLE = "double";

	private final Template booleanPrimitive;
	private final Template bytePrimitive;
	private final Template charPrimitive;
	private final Template shortPrimitive;
	private final Template intPrimitive;
	private final Template longPrimitive;
	private final Template floatPrimitive;
	private final Template doublePrimitive;

	/**
	 * Holds all known packages and classes
	 * @param cus The source units
	 * @param headerUnits The header units
	 */
	public PackageHandler(
			Collection< CompilationUnit > cus,
			Collection< CompilationUnit > headerUnits
	) {
		this.root = new Package( this );
		for( CompilationUnit cu : headerUnits ) {
			Package pack = root.getPackage( cu.packageDeclaration() );
			cu.classes().forEach( c -> pack.addClass( c, cu.imports() ) );
			cu.interfaces().forEach( i -> pack.addInterface( i, cu.imports() ) );
		}

		for( CompilationUnit cu : cus ) {
			Package pack = root.getPackage( cu.packageDeclaration() );
			cu.classes().forEach( c -> pack.addClass( c, cu.imports() ) );
			cu.interfaces().forEach( i -> pack.addInterface( i, cu.imports() ) );
		}

		Package langPackage = getRoot().getPackage( langPath );
		booleanPrimitive = new PrimitiveTemplate( langPackage.getTemplate( BOOLEAN ) );
		bytePrimitive = new PrimitiveTemplate( langPackage.getTemplate( BYTE ) );
		charPrimitive = new PrimitiveTemplate( langPackage.getTemplate( CHARACTER ) );
		shortPrimitive = new PrimitiveTemplate( langPackage.getTemplate( SHORT ) );
		intPrimitive = new PrimitiveTemplate( langPackage.getTemplate( INTEGER ) );
		longPrimitive = new PrimitiveTemplate( langPackage.getTemplate( LONG ) );
		floatPrimitive = new PrimitiveTemplate( langPackage.getTemplate( FLOAT ) );
		doublePrimitive = new PrimitiveTemplate( langPackage.getTemplate( DOUBLE ) );
	}

	/**
	 * The root {@link Package}. This package holds all other packages
	 * @return The {@link Package} holding all other Packages
	 */
	public Package getRoot() {
		return root;
	}

	/**
	 * Getter for a template by the given name
	 * @param packagePath The path where the Template is stored
	 * @param templateName The name of the Template
	 * @return A Template for the given identifier
	 */
	public Template getTemplate( String packagePath, String templateName ){
		return this.root.getPackage( packagePath ).getTemplate( templateName );
	}

	/**
	 * Getter for Template by its primitive name
	 * @param identifier Name of the primitive
	 * @return A Template for the boxed type
	 */
	public Template getPrimitive( String identifier ){
		return switch( identifier ) {
			case PRIM_BOOLEAN -> booleanPrimitive;
			case PRIM_BYTE -> bytePrimitive;
			case PRIM_CHARACTER -> charPrimitive;
			case PRIM_SHORT -> shortPrimitive;
			case PRIM_INTEGER -> intPrimitive;
			case PRIM_LONG -> longPrimitive;
			case PRIM_FLOAT -> floatPrimitive;
			case PRIM_DOUBLE -> doublePrimitive;
			default -> Template.unknownTemplate();
		};
	}
}
