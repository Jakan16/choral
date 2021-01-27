package choral.compiler.dependencygraph.role;

import java.util.*;

public class RoleHierarchy {

	private List< Role > hierarchy;
	private final Node root;
	private Node cursor;
	private final Deque< Node > parentStack = new LinkedList<>();
	private Role dominantRole;

	public RoleHierarchy() {
		cursor = root = new Node();
	}

	public Role getHighest( Collection< ? extends Role > roles ){
		if( hierarchy == null ){
			buildHierarchy();
		}
		for( Role role: hierarchy ){
			if( roles.contains( role ) ){
				return role;
			}
		}
		throw new IllegalStateException( "The roles does not contain a role from the hierarchy" );
	}

	public void setRole( Role role ){
		cursor.role = role;
	}

	public void setLeftRole( Role role ){
		cursor.getLeft().role = role;
	}

	public void setRightRole( Role role ){
		cursor.getRight().role = role;
	}

	public void goLeft(){
		parentStack.push( cursor );
		cursor = cursor.getLeft();
	}

	public void goRight(){
		parentStack.push( cursor );
		cursor = cursor.getRight();
	}

	public void goUp(){
		cursor = parentStack.pop();
	}

	public void setDominantRole( Role dominantRole ) {
		this.dominantRole = dominantRole;
	}

	private void buildHierarchy(){
		compactTree();
		hierarchy = new ArrayList<>();
		assert dominantRole != null;
		hierarchy.add( dominantRole.getCanonicalRole() );

		Set< Role > used = new HashSet<>();
		used.add( dominantRole.getCanonicalRole() );

		Map< Role, Count > roleCountMap = new HashMap<>();
		var roleSet = root.role.getUnion();
		roleSet.stream()
				.filter( r -> !used.contains( r ) )
				.forEach( r -> roleCountMap.put( r, new Count() ) );

		while( roleCountMap.size() > 0 ){
			visit( root, used, roleCountMap );
			Optional< Role > opNext = roleSet.stream()
					.filter( r -> !used.contains( r ) )
					.max( ( o1, o2 ) -> Float.compare( roleCountMap.get( o1 ).score(), roleCountMap.get( o2 ).score() ) );
			assert opNext.isPresent();
			hierarchy.add( opNext.get() );
			used.add( opNext.get() );
			roleCountMap.remove( opNext.get() );
			roleCountMap.values().forEach( Count::reset );
		}
	}

	private void visit( Node node, Set< Role > used, Map< Role, Count > roleCountMap ){
		if( node.left == null ){ // leaf
			return;
		}

		if( !Collections.disjoint( node.role.getUnion(), used ) ){
			visit( node.left, used, roleCountMap );
			visit( node.right, used, roleCountMap );
			return;
		}

		if( node.role.getUnion().size() < node.size ){
			count( node, roleCountMap );
		}
	}

	private void count( Node node, Map< Role, Count > roleCountMap ){
		if( node.left == null ){ // leaf
			roleCountMap.get( node.role ).addLeafCount();
		}else{
			node.role.getUnion().stream()
					.map( roleCountMap::get )
					.forEach( Count::addInternalCount );
			count( node.left, roleCountMap );
			count( node.right, roleCountMap );
		}
	}

	private void compactTree(){
		compactTree( root );
	}

	private int compactTree( Node node ){
		if( node.left == null ){
			node.size = 1;
			return 1; // leaf
		}

		if( node.role.getUnion().size() == 1 ){
			// subtree only have single role, replace with leaf
			node.left = null;
			node.right = null;
			node.role = node.role.getUnion().iterator().next();
			node.size = 1;
			return 1;
		}

		if( node.left.role.getUnion().isEmpty() ){
			// left is unbound and can be removed
			node.role = node.right.role;
			node.left = node.right.left;
			node.right = node.right.right;
			node.size = compactTree( node );
			return node.size;
		}

		if( node.right.role.getUnion().isEmpty() ){
			// right is unbound and can be removed
			node.role = node.left.role;
			node.right = node.left.right;
			node.size = compactTree( node );
			return node.size;
		}

		node.size = compactTree( node.left ) + compactTree( node.right );
		return node.size;
	}

	@Override
	public String toString() {
		if( hierarchy == null ){
			return "hierarchy is not initialized";
		}
		return hierarchy.toString();
	}

	private static class Node{
		Role role;
		Node left;
		Node right;
		int size;

		Node getLeft(){
			if( left == null ){
				left = new Node();
			}

			return left;
		}

		Node getRight(){
			if( right == null ){
				right = new Node();
			}

			return right;
		}
	}

	private static class Count{
		int leafCount;
		int internalNodeCount;

		void addLeafCount(){
			leafCount++;
		}

		void addInternalCount(){
			internalNodeCount++;
		}

		float score(){
			return leafCount/(float) internalNodeCount;
		}

		void reset(){
			leafCount = 0;
			internalNodeCount = 0;
		}
	}
}
