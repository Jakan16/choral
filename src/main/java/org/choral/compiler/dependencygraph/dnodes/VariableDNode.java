package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;
import java.util.List;

public class VariableDNode extends DNode {

	private final TypeDNode type;

	public VariableDNode( String name, TypeDNode type ) {
		super( Collections.emptyList(), name );
		this.type = type;
	}

	public List< String > getRoles() {
		return type.getRoles();
	}

	@Override
	public TypeDNode getType() {
		return type;
	}

	@Override
	public String toString() {
		return "VariableNode " + getName() + " " + type.toString();
	}
}
