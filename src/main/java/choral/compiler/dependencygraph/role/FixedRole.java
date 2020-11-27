package choral.compiler.dependencygraph.role;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FixedRole extends Role {

	private final String name;

	public FixedRole( String name ) {
		super();
		this.name = name;
	}

	/**
	 * The display name of this role
	 * @return The display name of this role
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void coalesce( Role coalesceTo ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void coalesceIfUnfixed( Role coalesceTo ) {
		// no-op
	}

	@Override
	public void coalesceIfPreferred( Role coalesceTo ) {
		// no-op
	}

	@Override
	public boolean isFixed() {
		return true;
	}

	@Override
	public void setPossibleRoles( List< Role > role ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< Role > getPossibleRoles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set< Role > getPreferredRoles() {
		return Collections.singleton( this );
	}

	@Override
	public void setPreferredRoles( Set<Role> roles ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPreferredAUnion() {
		return false;
	}

	@Override
	public void setPreferredAUnion( boolean isUnion ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Role getCanonicalRole() {
		return this;
	}

	@Override
	public String toString() {
		return getNumber() + ":" + getName();
	}
}
