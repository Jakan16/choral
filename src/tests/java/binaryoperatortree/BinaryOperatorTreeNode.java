package binaryoperatortree;

public class BinaryOperatorTreeNode {

	String role;
	BinaryOperatorTreeNode left;
	BinaryOperatorTreeNode right;

	public void setChildren(BinaryOperatorTreeNode left, BinaryOperatorTreeNode right){
		this.left = left;
		this.right = right;
	}

	public String getRole() {
		return role;
	}

	public void setRole( String role ) {
		this.role = role;
	}

	@Override
	public String toString() {
		if( role != null ){ // leaf
			return "1@" + role;
		}
		return "(" + left.toString() + " + " + right.toString() + ")";
	}
}
