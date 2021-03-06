package choral.compiler.dependencygraph.dnodes;

/**
 * Node for super and this keyword.
 * Does not have roles or a type,
 * and is not needed for the final dependency graph, but is used to manage context.
 */
public class DThis extends DNode {

	public DThis() {
		super( "this or super" );
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName() + " " + super.toString();
	}
}
