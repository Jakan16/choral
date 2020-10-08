package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node for class instantiation
 */
public class DClassInstantiation extends DNode {

	private final DType type;
	private final List< DType > parameters;

	public DClassInstantiation(
			List< DNode > dependencies,
			String name,
			DType type,
			List< DType > parameters
	) {
		super( dependencies, name );
		this.type = type;
		this.parameters = parameters;
	}

	@Override
	public DType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString() + " (" + this.parameters.stream().map( DType::toString )
				.collect( Collectors.joining( ", " ) ) + ")";
	}
}
