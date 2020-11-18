package choral.compiler.dependencygraph.role;

import java.util.List;

public abstract class Role {

	public static final String UNBOUND_ROLE = "UNBOUNDROLE";

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

	/**
	 * Returns true if the two roles share the same name.
	 * If the roles are from the same world, they are effectively the same role.
	 * @param role1 The first role to compare.
	 * @param role2 Thew second role to compare.
	 * @return True if the roles have the same display name
	 */
	public static boolean commonDisplayName( Role role1, Role role2 ){
		return role1.getName().equals( role2.getName() );
	}

	public abstract void coalesce( Role coalesceTo );

	/**
	 * Weather the role have been fixed to an actual role, or is still to be fixed.
	 * @return true if an actual role is assigned to this instance.
	 */
	public abstract boolean isFixed();

	public abstract void setPossibleRoles( List<Role> role );

	public abstract List<Role> getPossibleRoles();
}
