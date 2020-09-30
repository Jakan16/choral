package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;
import java.util.List;

public class VariableDNode extends DNode {

	private final List< String > roles;
	private final TypeDNode type;

	public VariableDNode( String name, List< String > roles, TypeDNode type ) {
		super( Collections.emptyList(), name );
		this.roles = roles;
		this.type = type;
	}

	public List< String > getRoles() {
		return roles;
	}

	@Override
	public TypeDNode getType() {
		return type;
	}

	@Override
	public String toString() {
		return "VariableNode " + getName() + "@(" + String.join( ", ", roles ) + ") " + type.toString();
	}
}
