package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Class;
import org.choral.ast.body.ConstructorDefinition;
import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;

import java.util.Collections;
import java.util.List;

/**
 * Template for a class
 */
public class ClassTemplate extends Template {
	private final Class classNode;

	ClassTemplate( Class classNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, classNode.typeParameters() );
		this.classNode = classNode;
	}

	@Override
	public List< DType > prepareSuperType(){
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
	public List< String > worldParameters(){
		return Mapper.map( classNode.worldParameters(),
				w -> w.toWorldArgument().name().identifier() );
	}

	@Override
	public List< String > typeParameters(){
		return Mapper.map( classNode.typeParameters(), t -> t.name().identifier() );
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return classNode.methods();
	}

	@Override
	public List< ConstructorDefinition > constructorDefinitions() {
		return classNode.constructors();
	}

	@Override
	public List< Field > fields() {
		return classNode.fields();
	}
}
