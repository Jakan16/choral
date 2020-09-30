package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.FormalMethodParameter;
import org.choral.ast.body.Interface;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.List;
import java.util.stream.Collectors;

public class InterfaceTemplate extends Template {
	private final Interface interfaceNode;

	InterfaceTemplate( Interface interfaceNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, Mapper.map( interfaceNode.typeParameters(), t -> t.name().identifier() ) );
		this.interfaceNode = interfaceNode;
	}

	@Override
	public String getName() {
		return interfaceNode.name().identifier();
	}

	@Override
	public List< VariableDNode > getFields() {
		throw new UnsupportedOperationException();
	}

	@Override
	public VariableDNode getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< MethodSignature > getMethodSigs() {
		return interfaceNode.methods().stream()
				.map( MethodDefinition::signature )
				.map( s -> new MethodSignature(
						s.name().identifier(),
						s.parameters().stream().map( FormalMethodParameter::type )
								.map( this::typeExpressionToNode ).collect(	Collectors.toList() ),
						typeExpressionToNode( s.returnType() ) ) )
				.collect( Collectors.toList() );
	}

	@Override
	public List< FormalWorldParameter > worldParameters() {
		return interfaceNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters() {
		return interfaceNode.typeParameters();
	}
}
