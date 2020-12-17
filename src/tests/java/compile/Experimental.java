package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.Arrays;

import static runtimecompiler.RuntimeCompiler.*;

public class Experimental {

	@Test
	public void sets_variables() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/experimental/sets_and_variables",
				"Coalesce",
				Arrays.asList( A, B, C )
		);
	}

}
