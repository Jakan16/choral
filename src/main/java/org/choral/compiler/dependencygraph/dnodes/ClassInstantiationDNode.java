package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

public class ClassInstantiationDNode extends DNode {

	private final TypeDNode type;
	private final List< TypeDNode > parameters;

	public ClassInstantiationDNode(
			List< DNode > dependencies,
			String name,
			TypeDNode type,
			List< TypeDNode > parameters
	) {
		super( dependencies, name );
		this.type = type;
		this.parameters = parameters;
	}

	@Override
	public TypeDNode getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString() + " (" + this.parameters.stream().map( TypeDNode::toString )
				.collect( Collectors.joining( ", " ) ) + ")";
	}
}
