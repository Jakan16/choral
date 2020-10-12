package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Node for literal values
 */
public class DLiteral extends DNode{

	private final DType type;

	public DLiteral( DType type ) {
		super( Collections.emptyList(), type.getName() );
		this.type = type;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		if( !getType().getRoles().isEmpty() ){
			return getName() +
					"@(" + getType().getRoles().stream().map( Object::toString )
					.collect( Collectors.joining(", ")) + ")" +
					super.toString();
		}
		return getName() + " " + super.toString();
	}
}
