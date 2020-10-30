package org.choral.compiler.dependencygraph.dnodes;

public interface DNodeVisitorInterface < R > {

	R visit( DNode n );

	R visit( DClass n );

	R visit( DMethod n );

	R visit( DClassInstantiation n );

	R visit( DExpression n );

	R visit( DAssign n );

	R visit( DLiteral n );

	R visit( DMethodCall n );

	R visit( DReturn n );

	R visit( DStaticAccess n );

	R visit( DThis n );

	R visit( DVariable n );

	R visit( DBinaryExpression n );

	R visit( DRoot n );
}
