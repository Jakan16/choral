package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.CompilationUnit;
import org.choral.ast.body.Class;

import java.util.Collection;

public class PackageHandler {

	private final Package root;

	public PackageHandler(
			Collection< CompilationUnit > cus,
			Collection< CompilationUnit > headerUnits
	) {
		this.root = new Package();
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
	}

	public Package getRoot() {
		return root;
	}

	public Template getTemplate( String packagePath, String className ){
		return this.root.getPackage( packagePath ).getTemplate( className );
	}

}
