package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

public class ClassInstantiationDNode extends DNode {

	private final TypeDNode type;

	public ClassInstantiationDNode( List< DNode > dependencies, String name, TypeDNode type ) {
		super( dependencies, name );
		this.type = type;
	}

	@Override
	public TypeDNode getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
