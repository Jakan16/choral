package org.choral.compiler.dependencygraph.dnodes;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DRoot extends DNode {

	private final List< DNode > nodes;

	protected DRoot() {
		super( "root" );
		nodes = Collections.emptyList();
	}

	public static final DRoot emptyRoot = new DRoot();
	public static DRoot emptyRoot(){
		return emptyRoot;
	}

	public DRoot( List< DNode > nodes ) {
		super( "root" );
		this.nodes = new ArrayList<>( nodes );
	}

	public DRoot merge( DRoot other ){
		if( this.nodes.size() == 0 ){
			if( other.getNodes().size() == 0 ) {
				return emptyRoot();
			}else {
				other.getNodes().addAll( this.nodes );
				return other;
			}
		}
		this.nodes.addAll( other.getNodes() );
		return this;
	}

	@Override
	public DRoot merge( DNode other ){
		if( other instanceof DRoot ){
			return merge( (DRoot) other );
		}

		if( this == emptyRoot() ){
			return new DRoot( Collections.singletonList( other ) );
		}
		this.nodes.add( other );
		return this;
	}

	public DRoot mergeFlip( DNode other ){
		if( other instanceof DRoot ){
			return ((DRoot) other).merge( this );
		}

		if( this == emptyRoot() ){
			return new DRoot( Collections.singletonList( other ) );
		}
		this.nodes.add( 0, other );
		return this;
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
