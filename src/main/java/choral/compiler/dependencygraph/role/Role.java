package choral.compiler.dependencygraph.role;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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

	public abstract void coalesceIfUnfixed( Role coalesceTo );

	public abstract void coalesceIfPreferred( Role coalesceTo );

	public abstract void coalesceHierarchical( Role role, RoleHierarchy roleHierarchy );

	/**
	 * Weather the role have been fixed to an actual role, or is still to be fixed.
	 * @return true if an actual role is assigned to this instance.
	 */
	public abstract boolean isFixed();

	public abstract void setPossibleRoles( List< Role > role );

	public abstract List< Role > getPossibleRoles();

	public abstract Set< Role > getPreferredRoles();

	public abstract void setPreferredRoles( Set< Role > roles );

	public abstract void setUnion( Set< Role > roles );

	public abstract void setLeftUnion( Set< Role > roles );

	public abstract void setRightUnion( Set< Role > roles );

	public abstract Set< Role > getUnion();

	public abstract boolean isPreferredAUnion();

	public abstract void setPreferredAUnion( boolean isUnion );

	@Override
	public boolean equals( Object o ) {
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;
		Role role = (Role) o;
		return getCanonicalRole() == role.getCanonicalRole();
	}

	@Override
	public int hashCode() {
		var canonicalRole = getCanonicalRole();
		if( this == canonicalRole ){
			return super.hashCode();
		}
		return canonicalRole.hashCode();
	}

	/**
	 * Next time the role is coalesced it won't, the role will instead be at the top of the roleHierarchy
	 * @param roleHierarchy the hierarchy to put the role on top of
	 */
	public abstract void hierarchyAlert( RoleHierarchy roleHierarchy );
}
