package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

/**
 * Node for generic expression
 */
public class DExpression extends DNode{
	private List<String> roles;

	public DExpression( List< DNode > dependencies ) {
		super( dependencies, "ExpressionDNode" );
	}

	public DExpression( List< DNode > dependencies, String name ) {
		super( dependencies, name );
	}

	@Override
	public DType getType() {
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
