package org.choral.compiler.dependencygraph.role;

public abstract class Role {

	private final int number;
	private static int nonce = 0;

	public Role() {
		this.number = roleNonce();
	}

	/**
	 * Returns an unique number for each invocation, used to differentiate role instances.
	 * @return An unique number
	 */
	static int roleNonce(){
		return nonce++;
	}

	/**
	 * The unique number for this role instance
	 * @return A number uniquely representing this role
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * The common representation for a set of roles.
	 * The set can have at most one {@link FixedRole},
	 * and if it does, the returned instance will be the {@link FixedRole}.
	 * @return The current representation of this role
	 */
	public abstract Role getCanonicalRole();

	public abstract String getName();
}
