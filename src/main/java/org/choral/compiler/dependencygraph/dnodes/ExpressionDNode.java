package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

public class ExpressionDNode extends DNode{
	private List<String> roles;

	public ExpressionDNode( List< DNode > dependencies ) {
		super( dependencies, "ExpressionDNode" );
	}

	public ExpressionDNode( List< DNode > dependencies, String name ) {
		super( dependencies, name );
	}

	@Override
	public TypeDNode getType() {
		return getDependencies().get( getDependencies().size()-1 ).getType();
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRole( List<String> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		if( getRoles() != null ){
			return getName() + "@(" + String.join( ", ", roles ) + ")";
		}
		return getName();
	}
}
