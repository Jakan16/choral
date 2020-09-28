package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;
import java.util.List;

public class LiteralDNode extends DNode{

	private final String literalType;
	private List< String > roles;

	public LiteralDNode( String type ) {
		super( Collections.emptyList(), type );
		this.literalType = type;
	}

	public String getLiteralType() {
		return literalType;
	}

	public List< String > getRoles() {
		return roles;
	}

	public void setRole( List< String > roles ) {
		this.roles = roles;
	}

	@Override
	public TypeDNode getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if( getRoles() != null ){
			return getName() + "@(" + String.join( ", ", getRoles() ) + ")" + super.toString();
		}
		return getName() + " " + super.toString();
	}
}
