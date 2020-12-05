package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.*;

import static runtimecompiler.RuntimeCompiler.*;

public class Programs {

	@Test
	public void mergesort() throws Throwable {

		RuntimeCompiler instance = RuntimeCompiler.compile(
				"src/tests/choral/tests/mergesort",
				"Mergesort",
				Arrays.asList( A, B, C )
		);

		Random random = new Random( 15 );
		List< Integer > numbers = new ArrayList<>();

		for( int i = 0; i < 200; i++ ) {
			numbers.add( i );
		}

		List< Integer > shuffled = new ArrayList<>( numbers );

		for( int i = 0; i < 10; i++ ) {
			Collections.shuffle( shuffled, random );

			instance.method( "sort" )
					.argAt( A, shuffled )
					.invoke()
					.assertNoErrors()
					.assertEqualAt( A, numbers );
		}
	}

}
