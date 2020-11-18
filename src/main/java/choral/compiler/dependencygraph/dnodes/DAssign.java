package choral.compiler.dependencygraph.dnodes;

public class DAssign extends DNode {

	private final DNode target;
	private final DNode value;

	public DAssign( DNode target, DNode value ) {
		super( "Assign" );
		this.target = target;
		this.value = value;
	}

	@Override
	public DType getType() {
		return target.getType();
	}

	public DNode getTarget() {
		return target;
	}

	public DNode getValue() {
		return value;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + getType() + " " + super.toString();
	}
}
