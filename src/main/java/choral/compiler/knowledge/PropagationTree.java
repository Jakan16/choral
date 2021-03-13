package choral.compiler.knowledge;

import choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class PropagationTree {

	public static List< Pair< String, String > > solve( float latency, float processing, String sender, Collection< String > receivers ){
		receivers = receivers.stream().sorted().collect( Collectors.toList() );
		Node root = new Node( sender, 0 );
		Deque< Node > qp = new ArrayDeque<>();
		Deque< Node > qm = new ArrayDeque<>();
		qm.add( root );

		for( String role: receivers ){
			Node parent = extractMin( qp, qm );
			parent.incCost( processing );
			Node newNode = new Node( role, parent.cost );
			newNode.incCost( latency );
			parent.addChild( newNode );
			qp.addLast( parent );
			qm.addLast( newNode );
		}

		List< Pair< String, String > > pairs = new ArrayList<>();
		postorder( root, pairs );
		return pairs;
	}

	private static Node extractMin(Deque< Node > q1, Deque< Node > q2){
		if( q1.isEmpty() ){
			return q2.removeFirst();
		}else if( q2.isEmpty() ){
			return q1.removeFirst();
		}

		if(q1.getFirst().cost <= q2.getFirst().cost){
			return q1.removeFirst();
		}
		return q2.removeFirst();
	}

	private static void postorder( Node node, List< Pair< String, String > > pairs ){
		Collections.reverse( node.children );
		for( Node child: node.children ){
			postorder( child, pairs );
		}

		for( Node child : node.children ) {
			pairs.add( Pair.of( node.role, child.role ) );
		}
	}

	private static class Node{
		String role;
		float cost = 0;
		List<Node> children = new ArrayList<>();

		public Node( String role, float cost ) {
			this.role = role;
			this.cost = cost;
		}

		void incCost( float amount ){
			cost += amount;
		}

		void addChild( Node child ){
			children.add( child );
		}
	}

}
