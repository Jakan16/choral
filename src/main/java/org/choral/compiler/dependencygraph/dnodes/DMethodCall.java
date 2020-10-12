package org.choral.compiler.dependencygraph.dnodes;

import org.choral.compiler.dependencygraph.role.Role;

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

	public List< Role > getRoles() {
		return returnType.getRoles();
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getName() );

		if( getRoles() != null ){
			sb
			.append( "@(" )
			.append( getRoles().stream().map( Object::toString )
					.collect( Collectors.joining(", ")) )
			.append( ") " );
		}
		sb.append( "r:" ).append( returnType.toString() ).append( " p:(" )
		.append( parameters.stream().map( DType::toString )
				.collect( Collectors.joining( ", " ) ) )
		.append( ")" );

		return sb.toString();
	}

	@Override
	public DType getType() {
		return returnType;
	}
}
