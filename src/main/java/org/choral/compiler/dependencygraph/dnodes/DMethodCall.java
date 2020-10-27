package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A node for method calls
 */
public class DMethodCall extends DNode {

	private final DType returnType;
	private final List< DType > parameters;
	private final List< DNode > arguments;

	public DMethodCall(
			String name,
			List< DNode > arguments,
			DType returnType,
			List< DType > parameters
	) {
		super( name );
		this.arguments = arguments;
		this.returnType = returnType;
		this.parameters = parameters;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	public DType getReturnType() {
		return returnType;
	}

	public List< DType > getParameters() {
		return parameters;
	}

	public List< DNode > getArguments() {
		return arguments;
	}

	@Override
	public String toString() {

		return getName() +
				" r:" + returnType.toString()
				+ " p:("
				+ parameters.stream().map( DType::toString )
					.collect( Collectors.joining( ", " ) )
				+ ") " + super.toString();
	}

	@Override
	public DType getType() {
		return returnType;
	}
}
