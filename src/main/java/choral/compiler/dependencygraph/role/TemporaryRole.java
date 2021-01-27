package choral.compiler.dependencygraph.role;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TemporaryRole extends Role {

	private Role parent;
	private Role parentIfPreferred;
	private List< Role > possibleRoles;
	private Set< Role > preferredRoles = Collections.emptySet();
	private Set< Role > union = Collections.emptySet();
	private Set< Role > leftUnion = Collections.emptySet();
	private Set< Role > rightUnion = Collections.emptySet();
	private boolean isPreferredUnion = false;
	private RoleHierarchy roleHierarchy;
	private boolean hierarchyAlert = false;

	@Override
	public void coalesce( Role coalesceTo ){
		assert parent == null;
		var role = coalesceTo.getCanonicalRole();
		if( this == role ){
			// coalescing a role to itself does nothing
			return;
		}
		if( hierarchyAlert ){
			parentIfPreferred = coalesceTo;
			roleHierarchy.setDominantRole( coalesceTo );
			hierarchyAlert = false;
			return;
		}
		parent = role;
	}

	@Override
	public void coalesceIfUnfixed( Role coalesceTo ) {
		coalesce( coalesceTo );
	}

	@Override
	public void coalesceIfPreferred( Role coalesceTo ) {
		if( this.isPreferredUnion ){
			// any role may be used, if preferred is made as an union
			// however the set may affect the parent, which this role is coalesced to.
			coalesce( coalesceTo );
		}else{
			assert parent == null;
			parentIfPreferred = coalesceTo;
		}
	}

	@Override
	public void coalesceHierarchical( Role role, RoleHierarchy roleHierarchy ) {
		this.roleHierarchy = roleHierarchy;
		this.parentIfPreferred = role;
	}

	@Override
	public Role getCanonicalRole() {
		if( parent != null ) {
			Role fixedRole = parent.getCanonicalRole();
			// Shortcut to the root, for future calls
			parent = fixedRole;
			return fixedRole;
		}

		if( parentIfPreferred == null ){
			return this;
		}

		var maybeParent = parentIfPreferred.getCanonicalRole();
		assert maybeParent.isFixed();

		if( roleHierarchy != null ){
			//if( preferredRoles.contains( maybeParent ) || union.isEmpty() ){
			//if( union.isEmpty() || this.isPreferredUnion ){
			if( union.isEmpty() ){
				coalesce( maybeParent );
			}else {
				var leftHighest = roleHierarchy.getHighest( leftUnion );
				var rightHighest = roleHierarchy.getHighest( rightUnion );
				if( leftHighest == rightHighest ){
					coalesce( leftHighest );
				}else{
					coalesce( maybeParent );
				}
			}
		}else {
			if( preferredRoles.contains( maybeParent ) ){
				coalesce( maybeParent );
			}else {
				coalesce( preferredRoles.iterator().next() );
			}
		}
		return getCanonicalRole();
	}

	@Override
	public String getName() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			if( this.possibleRoles != null && this.possibleRoles.size() > 0 ){
				return this.possibleRoles.get( 0 ).getName();
			}
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
	public void setPossibleRoles( List< Role > role ) {
		this.possibleRoles = role;
	}

	@Override
	public List< Role > getPossibleRoles() {
		return this.possibleRoles;
	}

	@Override
	public Set< Role > getPreferredRoles() {
		return this.preferredRoles;
	}

	@Override
	public void setPreferredRoles( Set<Role> roles ) {
		this.preferredRoles = roles;
	}

	@Override
	public void setUnion( Set< Role > roles ) {
		this.union = roles;
	}

	@Override
	public void setLeftUnion( Set< Role > roles ) {
		this.leftUnion = roles;
	}

	@Override
	public void setRightUnion( Set< Role > roles ) {
		this.rightUnion = roles;
	}

	@Override
	public Set< Role > getUnion() {
		return this.union;
	}

	@Override
	public boolean isPreferredAUnion() {
		return this.isPreferredUnion;
	}

	@Override
	public void setPreferredAUnion( boolean isUnion ) {
		this.isPreferredUnion = isUnion;
	}

	@Override
	public void hierarchyAlert( RoleHierarchy roleHierarchy ) {
		this.roleHierarchy = roleHierarchy;
		this.hierarchyAlert = true;
	}

	@Override
	public String toString() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			return String.valueOf( getNumber() );
		}
		return getNumber() + "->" + canonical.toString();
	}
}
