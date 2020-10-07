package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;
import java.util.List;

public class DVariable extends DNode {

	private final DType type;

	public DVariable( String name, DType type ) {
		super( Collections.emptyList(), name );
		this.type = type;
	}

	public List< String > getRoles() {
		return type.getRoles();
	}

	@Override
	public DType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "VariableNode " + getName() + " " + type.toString();
	}
}
