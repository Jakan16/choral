package org.choral.compiler.dependencygraph.dnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DRoot extends DNode {

	private final List< DNode > nodes;

	private DRoot() {
		super( "root" );
		nodes = Collections.emptyList();
	}

	public static final DRoot emptyRoot = new DRoot();
	public static DRoot emptyRoot(){
		return emptyRoot;
	}

	public DRoot( List< DNode > nodes ) {
		super( "root" );
		this.nodes = nodes;
	}

	public DRoot merge( DRoot other ){
		var nodes = new ArrayList<>( getNodes() );
		nodes.addAll( other.getNodes() );
		return new DRoot( nodes );
	}

	@Override
	public DRoot merge( DNode other ){
		if( other instanceof DRoot ){
			return merge( (DRoot) other );
		}

		var nodes = new ArrayList<>( getNodes() );
		nodes.add( other );
		return new DRoot( nodes );
	}

	public List< DNode > getNodes() {
		return nodes;
	}

	@Override
	public DType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}
}
