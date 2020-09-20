package knowledgeinjector;

import org.choral.Choral;

public class KnowledgeInjectorTest {

	public static void main ( String[] args ) {

		String targetFolder = "src/tests/choral/KnowledgeInjector";
		String destinationFolder = "build/generatedFromChoral/java/";

		Choral.main(
				//new String[]{"epp -dry -d " + destinationFolder + " -t " + targetFolder + " NestedIf"}
				new String[]{
						"epp",
						"--dry-run",
//						"-debug",
						"-t", destinationFolder,
						"-s", targetFolder,
						"NestedIf" }
		);

	}
}
