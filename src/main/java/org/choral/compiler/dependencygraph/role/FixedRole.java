package org.choral.compiler.dependencygraph.role;

import java.util.List;

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
	public Role getCanonicalRole() {
		return this;
	}

	@Override
	public String toString() {
		return getNumber() + ":" + getName();
	}
}
