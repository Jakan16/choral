package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A node for method calls
 */
public class DMethodCall extends DNode {

	private final DType returnType;
	private final List< DType > parameters;

	public DMethodCall(
			String name,
			List< DNode > dependencies,
			DType returnType,
			List< DType > parameters
	) {
		super( dependencies, name );
		this.returnType = returnType;
		this.parameters = parameters;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {

		return getName() +
				" r:" + returnType.toString()
				+ " p:("
				+ parameters.stream().map( DType::toString )
					.collect( Collectors.joining( ", " ) )
				+ ")";
	}

	@Override
	public DType getType() {
		return returnType;
	}
}
