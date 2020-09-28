package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Class;
import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;

import java.util.List;

public class ClassTemplate extends Template {
	private final Class classNode;

	ClassTemplate( Class classNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super(importDeclarations, holdingPackage );
		this.classNode = classNode;
	}

	@Override
	public String getName() {
		return classNode.name().identifier();
	}

	@Override
	public Field getField( String identifier ){
		for( Field field : classNode.fields() ) {
			if( field.name().identifier().equals( identifier ) ) {
				return field;
			}
		}
		return null;
	}

	@Override
	public List< ? extends MethodDefinition > getMethodDefs(){
		return classNode.methods();
	}

	@Override
	public List< FormalWorldParameter > worldParameters(){
		return classNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters(){
		return classNode.typeParameters();
	}
}
