package choral.compiler.dependencygraph.dnodes;

public class DMethod extends DNode {

	private final DNode content;

	public DMethod( String name, DNode content ) {
		super( name );
		this.content = content;
	}

	public DNode getContent() {
		return content;
	}

	@Override
	public DType getType() {
		return null;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + super.toString();
	}
}
