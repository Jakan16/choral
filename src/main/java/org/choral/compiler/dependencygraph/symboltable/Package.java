package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Class;
import org.choral.ast.body.Interface;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Package {

	private final HashMap<String, Package> packages = new HashMap<>();
	private final HashMap<String, Template > templates = new HashMap<>();
	private final Package root;
	private final PackageHandler packageHandler;
	private final String fullName;

	public Package( Package root, PackageHandler packageHandler, String fullName ) {
		this.root = root;
		this.packageHandler = packageHandler;
		this.fullName = fullName;
	}

	public Package( PackageHandler packageHandler ) {
		this.root = this;
		this.packageHandler = packageHandler;
		fullName = "";
	}

	public Package getPackage( String path ){
		String[] pathParts = path.split( "\\.", 2 );
		Package child = packages.computeIfAbsent( pathParts[0], k -> {
			if( fullName.equals( "" ) ){
				return new Package( root, packageHandler, pathParts[0] );
			}
			return new Package( root, packageHandler, fullName + "." + pathParts[0] );
		} );
		if( pathParts.length > 1 ){
			return child.getPackage( pathParts[1] );
		}
		return child;
	}

	public void addClass( Class classNode, List< ImportDeclaration > importDeclarations ){
		templates.put( classNode.name().identifier(),
				new ClassTemplate( classNode, importDeclarations, this ) );
	}

	public void addInterface( Interface interfaceNode, List< ImportDeclaration > importDeclarations ) {
		templates.put( interfaceNode.name().identifier(),
				new InterfaceTemplate( interfaceNode, importDeclarations, this ) );
	}

	public Template getTemplate( String name ){
		return templates.get( name );
	}

	public Collection< Template > getTemplates(){
		return templates.values();
	}

	public Package getRoot() {
		return root;
	}

	public PackageHandler getPackageHandler() {
		return packageHandler;
	}
}
