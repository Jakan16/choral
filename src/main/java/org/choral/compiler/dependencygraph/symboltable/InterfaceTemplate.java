package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Interface;
import org.choral.ast.body.MethodDefinition;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.List;

/**
 * Template for an interface
 */
public class InterfaceTemplate extends Template {
	private final Interface interfaceNode;

	InterfaceTemplate( Interface interfaceNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, interfaceNode.typeParameters() );
		this.interfaceNode = interfaceNode;
	}

	@Override
	public List< DType > prepareSuperType() {
		return typeExpressionsToNodes( interfaceNode.extendsInterfaces() );
	}

	@Override
	public String getName() {
		return interfaceNode.name().identifier();
	}

	@Override
	public List< DVariable > getFields() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DVariable getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< String > worldParameters() {
		return Mapper.map( interfaceNode.worldParameters(), w -> w.toWorldArgument().name().identifier() );
	}

	@Override
	public List< String > typeParameters() {
		return Mapper.map( interfaceNode.typeParameters(), t -> t.name().identifier() );
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return interfaceNode.methods();
	}

}
