package org.choral.compiler.dependencygraph;

import org.choral.compiler.dependencygraph.dnodes.*;

public class DependencyGraphPrinter implements DNodeVisitorInterface< StringBuilder > {

	private int indent = 0;
	private final StringBuilder sb = new StringBuilder();

	public static String walk( DNode node ){
		return new DependencyGraphPrinter().visit( node ).toString();
	}

	@Override
	public StringBuilder visit( DNode n ) {
		return n.accept( this );
	}

	@Override
	public StringBuilder visit( DClass n ) {
		sb.append( "Class: " ).append( n.toString() ).append( '\n' );
		n.getMethods().accept( this );
		return sb;
	}

	@Override
	public StringBuilder visit( DMethod n ) {
		sb.append( "Method: " ).append( n.toString() ).append( '\n' );
		n.getContent().accept( this );
		return sb;
	}

	@Override
	public StringBuilder visit( DClassInstantiation n ) {
		sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );

		indent++;
		for( DNode arg: n.getArguments() ){
			visit( arg );
		}
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DExpression n ) {
		sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );

		indent++;
		for( DNode arg: n.getDependencies() ){
			visit( arg );
		}
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DAssign n ) {
		sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );

		indent++;
		visit( n.getTarget() );
		visit( n.getValue() );
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DLiteral n ) {
		return sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
	}

	@Override
	public StringBuilder visit( DMethodCall n ) {
		sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
		indent++;
		for( DNode arg: n.getArguments() ){
			visit( arg );
		}
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DReturn n ) {
		sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
		indent++;
		visit( n.getReturnNode() );
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DStaticAccess n ) {
		return sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
	}

	@Override
	public StringBuilder visit( DThis n ) {
		return sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
	}

	@Override
	public StringBuilder visit( DVariable n ) {
		return sb.append( "    ".repeat( indent ) ).append( n.toString() ).append( '\n' );
	}

	@Override
	public StringBuilder visit( DBinaryExpression n ) {
		sb.append( "    ".repeat( indent ) );

		sb.append( n.toString() ).append( '\n' );
		indent++;
		visit( n.getLeft() );
		visit( n.getRight() );
		indent--;
		return sb;
	}

	@Override
	public StringBuilder visit( DRoot n ) {
		boolean first = true;
		for( DNode dNode: n.getNodes() ){
			if( !first ) {
				sb.append( "---------------" ).append( '\n' );
			}
			first = false;
			visit( dNode );
		}

		return sb;
	}
}
