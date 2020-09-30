package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

public class MethodCallDNode extends DNode {

	private List<String> roles;
	private final TypeDNode returnType;
	private final List< TypeDNode > parameters;

	public MethodCallDNode( List< DNode > dependencies, TypeDNode returnType, List< TypeDNode > parameters ) {
		super( dependencies, "MethodCallDNode" );
		this.returnType = returnType;
		this.parameters = parameters;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRole( List<String> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getName() );

		if( getRoles() != null ){
			sb.append( "@(" ).append( String.join( ", ", getRoles() ) ).append( ") " );
		}
		sb.append( returnType.toString() ).append( " (" )
		.append( parameters.stream().map( TypeDNode::toString ).collect( Collectors.joining( ", " ) ) )
		.append( ")" );

		return sb.toString();
	}

	@Override
	public TypeDNode getType() {
		return returnType;
	}
}
