package choral.compiler.dependencygraph.symboltable;

import choral.ast.ImportDeclaration;
import choral.ast.body.Class;
import choral.ast.body.Interface;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Package {

	private final HashMap<String, Package> packages = new HashMap<>();
	private final HashMap<String, Template > templates = new HashMap<>();
	private final Package root;
	private final PackageHandler packageHandler;

	public Package( Package root, PackageHandler packageHandler ) {
		this.root = root;
		this.packageHandler = packageHandler;
	}

	public Package( PackageHandler packageHandler ) {
		this.root = this;
		this.packageHandler = packageHandler;
	}

	/**
	 * Get package by path rooted in this package
	 * @param path The path rooted in this package
	 * @return The package of the path
	 */
	public Package getPackage( String path ){
		// split root of path and rest
		String[] pathParts = path.split( "\\.", 2 );
		Package child = this.packages.computeIfAbsent( pathParts[0],
				k -> new Package( this.root, this.packageHandler ) );
		// if more of the path remains, recursively visit it.
		return pathParts.length == 1 ? child : child.getPackage( pathParts[ 1 ] );
	}

	/**
	 * Add class definition to package
	 * @param classNode The class to add
	 * @param importDeclarations The imports the class has access to
	 */
	public void addClass( Class classNode, List< ImportDeclaration > importDeclarations ){
		templates.put( classNode.name().identifier(),
				new ClassTemplate( classNode, importDeclarations, this ) );
	}

	/**
	 * Add interface definition to package
	 * @param interfaceNode The interface to add
	 * @param importDeclarations The imports the interface has access to
	 */
	public void addInterface( Interface interfaceNode, List< ImportDeclaration > importDeclarations ) {
		templates.put( interfaceNode.name().identifier(),
				new InterfaceTemplate( interfaceNode, importDeclarations, this ) );
	}

	/**
	 * Get Template from this package
	 * @param name The name of the Template
	 * @return The template by the given name
	 */
	public Template getTemplate( String name ){
		return templates.get( name );
	}

	/**
	 * Get all Templates from this package
	 * @return A list of Templates
	 */
	public Collection< Template > getTemplates(){
		return templates.values();
	}

	/**
	 * The root package of this package
	 * @return The root package
	 */
	public Package getRoot() {
		return root;
	}

	/**
	 * The package handler of this package
	 * @return The package handler
	 */
	public PackageHandler getPackageHandler() {
		return packageHandler;
	}
}
