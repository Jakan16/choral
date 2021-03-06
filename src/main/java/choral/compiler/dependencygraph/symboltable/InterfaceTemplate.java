package choral.compiler.dependencygraph.symboltable;

import choral.ast.ImportDeclaration;
import choral.ast.body.Interface;
import choral.ast.body.MethodDefinition;
import choral.compiler.dependencygraph.Mapper;
import choral.compiler.dependencygraph.dnodes.DType;
import choral.compiler.dependencygraph.dnodes.DVariable;
import choral.compiler.dependencygraph.role.FixedRole;
import choral.compiler.dependencygraph.role.Role;

import java.util.Collections;
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
		if( interfaceNode.extendsInterfaces().size() > 0 ) {
			return typeExpressionsToDTypes( interfaceNode.extendsInterfaces() );
		}

		if( interfaceNode.worldParameters().size() == 1 ){ // multi roled objects does not extend Object
			// If nothing else is extended, Object is implicit
			Template objectTem = getHoldingPackage().getRoot().getPackage( PackageHandler.langPath )
					.getTemplate( "Object" );
			return Collections.singletonList(
					new DType( objectTem,
							Collections.singletonList( worldParameters().get( 0 ) ),
							Collections.emptyList() ) );
		}

		return Collections.emptyList();
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
