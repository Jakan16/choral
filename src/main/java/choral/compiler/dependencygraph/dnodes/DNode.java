package choral.compiler.dependencygraph.dnodes;

import choral.ast.Position;

import java.util.Collections;

/**
 * Base class for dependency graph nodes
 */
public abstract class DNode {

	private final String name;
	private Position position;

	public DNode( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition( Position position ) {
		this.position = position;
	}

	public DRoot merge( DNode other ){
		if( other instanceof DRoot ){
			return (( DRoot ) other).mergeFlip( this );
		}else{
			return new DRoot( Collections.singletonList( this ) ).merge( other );
		}
	}

	/**
	 * The type to the extent it is known from the source code
	 * @return the known non-inferred type
	 */
	public abstract DType getType();

	public abstract < R > R accept( DNodeVisitorInterface< R > v );

	@Override
	public String toString() {
		if( getPosition() != null ){
			return getPosition().sourceFile() + ":" + position.line() + ":" + position.column();
		}
		return "";
	}
}
