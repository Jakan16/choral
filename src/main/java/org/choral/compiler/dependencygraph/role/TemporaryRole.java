package org.choral.compiler.dependencygraph.role;

public class TemporaryRole extends Role {

	private Role parent;

	@Override
	public void coalesce( Role coalesceTo ){
		assert parent == null;
		if( this == coalesceTo.getCanonicalRole() ){
			// coalescing a role to itself does nothing
			return;
		}
		parent = coalesceTo;
	}

	@Override
	public Role getCanonicalRole() {
		if( parent == null ){
			return this;
		}
		Role fixedRole = parent.getCanonicalRole();
		// Shortcut to the root, for future calls
		parent = fixedRole;
		return fixedRole;
	}

	@Override
	public String getName() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			throw new IllegalStateException( "Temporary not coalesced to fixed role has no name" );
		}
		return canonical.getName();
	}

	@Override
	public boolean isFixed() {
		if( parent == null ){
			return false;
		}
		return getCanonicalRole().isFixed();
	}

	@Override
	public String toString() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			return String.valueOf( getNumber() );
		}
		return canonical.toString();
	}
}
