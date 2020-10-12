package org.choral.compiler.dependencygraph.dnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DRoot extends DNode {

	private DRoot() {
		super( Collections.emptyList(), "root" );
	}

	public static final DRoot emptyRoot = new DRoot();
	public static DRoot emptyRoot(){
		return emptyRoot;
	}

	public DRoot( List< DNode > dependencies ) {
		super( dependencies, "root" );
	}

	public DRoot merge( DRoot other ){
		var nodes = new ArrayList<>( getDependencies() );
		nodes.addAll( other.getDependencies() );
		return new DRoot( nodes );
	}

	@Override
	public DRoot merge( DNode other ){
		if( other instanceof DRoot ){
			return merge( (DRoot) other );
		}

		var nodes = new ArrayList<>( getDependencies() );
		nodes.add( other );
		return new DRoot( nodes );
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
