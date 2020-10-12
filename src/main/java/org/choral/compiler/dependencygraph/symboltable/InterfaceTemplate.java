package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Interface;
import org.choral.ast.body.MethodDefinition;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;
import org.choral.compiler.dependencygraph.role.FixedRole;
import org.choral.compiler.dependencygraph.role.Role;

import java.util.List;

/**
 * Template for an interface
 */
public class InterfaceTemplate extends Template {
	private final Interface interfaceNode;
	private final List< Role > roles;

	InterfaceTemplate( Interface interfaceNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, interfaceNode.typeParameters() );
		this.interfaceNode = interfaceNode;
		this.roles = Mapper.map( interfaceNode.worldParameters(),
				w -> new FixedRole( w.toWorldArgument().name().identifier() ) );
	}

	@Override
	public List< DType > prepareSuperType() {
		return typeExpressionsToDTypes( interfaceNode.extendsInterfaces() );
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
	public List< Role > worldParameters() {
		return roles;
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
