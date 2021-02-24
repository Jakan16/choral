package binaryoperatortree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BinaryOperatorTreeGenerator {

	private static final String[] roleNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};

	public static String[] getRoleNames( int numRoles ){
		assert numRoles > 0;
		String[] roleSlice = new String[numRoles];
		System.arraycopy( roleNames, 0, roleSlice, 0, numRoles );

		return roleSlice;
	}

	public static BinaryOperatorTreeNode generateTree( int size, int numRoles ){
		return generateTree( size, numRoles, new Random() );
	}

	public static BinaryOperatorTreeNode generateTree( int size, int numRoles, long seed ){
		return generateTree( size, numRoles, new Random( seed ) );
	}

	public static BinaryOperatorTreeNode generateTree( int size, int numRoles, Random random ){
		BinaryOperatorTreeNode root = new BinaryOperatorTreeNode();
		List<BinaryOperatorTreeNode> leaves = new ArrayList<>();
		leaves.add( root );

		while( leaves.size() < size ){
			BinaryOperatorTreeNode node = leaves.remove( random.nextInt( leaves.size() ) );
			var nodeLeft = new BinaryOperatorTreeNode();
			var nodeRight = new BinaryOperatorTreeNode();
			node.setChildren( nodeLeft, nodeRight );
			leaves.add( nodeLeft );
			leaves.add( nodeRight );
		}

		for( BinaryOperatorTreeNode node: leaves ){
			node.setRole( roleNames[ random.nextInt( numRoles ) ] );
		}

		return root;
	}

}
