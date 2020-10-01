package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Class;
import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.dnodes.TypeDNode;

import java.util.Collections;
import java.util.List;

public class ClassTemplate extends Template {
	private final Class classNode;

	ClassTemplate( Class classNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super(importDeclarations, holdingPackage, classNode.typeParameters() );
		this.classNode = classNode;
	}

	@Override
	public List< TypeDNode > prepareSuperType(){
		if( classNode.extendsClass() != null ){
			return Collections.singletonList( typeExpressionToNode( classNode.extendsClass() ) );
		}

		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return this.classNode.name().identifier();
	}

	@Override
	public List< FormalWorldParameter > worldParameters(){
		return classNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters(){
		return classNode.typeParameters();
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return classNode.methods();
	}

	@Override
	public List< Field > fields() {
		return classNode.fields();
	}
}
