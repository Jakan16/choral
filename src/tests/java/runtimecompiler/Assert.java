package runtimecompiler;

import choral.lang.Unit;

import java.util.List;

public class Assert {

	public static void assertNoErrors( List< Object > objects ) throws Throwable {
		for( Object object : objects ) {
			if( object instanceof Exception ) {
				throw (Throwable) object;
			}
		}
	}

	public static void assertError( List< Object > objects ) {
		boolean containsError = false;
		for( Object object : objects ) {
			if( object instanceof RoleException ) {
				containsError = true;
			} else if( object instanceof Exception ) {
				throw new IllegalStateException( "Unexpected error type", (Throwable) object );
			}
		}

		if( !containsError ) {
			throw new IllegalStateException( "Expected at least one exception, found none" );
		}
	}

	public static void assertEqual( List< Object > objects, Object targetValue, int position ) {
		for( int i = 0, objectsSize = objects.size(); i < objectsSize; i++ ) {
			Object object = objects.get( i );
			if( i != position && object != Unit.id ) {
				throw new IllegalStateException(
						"The value at position " + i + " was expected to be Unit, but was "
								+ object.getClass() + " " + object );
			} else if( i == position && !object.equals( targetValue ) ) {
				throw new IllegalStateException(
						"The value at position " + i + " was expected to be "
								+ targetValue + ", but was " + object );
			}
		}
	}
}
