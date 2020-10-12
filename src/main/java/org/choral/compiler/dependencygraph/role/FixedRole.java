package org.choral.compiler.dependencygraph.role;

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
	public Role getCanonicalRole() {
		return this;
	}

	@Override
	public String toString() {
		return getNumber() + ":" + getName();
	}
}
