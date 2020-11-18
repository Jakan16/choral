package choral.compiler.dependencygraph.dnodes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node for class instantiation
 */
public class DClassInstantiation extends DNode {

	private final DType type;
	private final List< DType > parameters;
	private final List< DNode > arguments;

	public DClassInstantiation(
			List< DNode > arguments,
			String name,
			DType type,
			List< DType > parameters
	) {
		super( name );
		this.arguments = arguments;
		this.type = type;
		this.parameters = parameters;
	}

	@Override
	public DType getType() {
		return type;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	public List< DType > getParameters() {
		return parameters;
	}

	public List< DNode > getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return type.toString() + " (" + this.parameters.stream().map( DType::toString )
				.collect( Collectors.joining( ", " ) ) + ") " + super.toString();
	}
}
