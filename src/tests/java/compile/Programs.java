package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static runtimecompiler.Assert.assertEqual;
import static runtimecompiler.Assert.assertNoErrors;

public class Programs {
	
	@Test
	public void mergesort() throws Throwable {
		RuntimeCompiler compiler = new RuntimeCompiler(
				"src/tests/choral/tests/mergesort",
				"Mergesort",
				Arrays.asList( "A", "B", "C" )
		).compile();

		//List< Object > results = compiler.invokeMethod( "sortTest" );
		List< Integer > numbers = new ArrayList<>( Arrays.asList( -20, -4, 1, 3, 7, 7, 12, 33, 44, 56, 100, 85 ) );
		List< Object > results = compiler.invokeMethod( "sort", new Object[]{numbers}, new Object[0], new Object[0] );
		assertNoErrors( results );

		assertEqual( results, Arrays.asList( -20, -4, 1, 3, 7, 7, 12, 33, 44, 56, 85, 100 ), 0 );
	}

}
