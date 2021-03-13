package selections;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class SelectionsProgramGenerator {

	public static final String className = "SelectionsTester";
	public static final String destinationFolder = "build/generatedChoral/choral/";

	static {
		File directory = new File( destinationFolder );
		if (! directory.exists()){
			directory.mkdirs();
		}
	}

	public static String[] generateProgram( int numRoles ){
		return generateProgram( numRoles, new Random() );
	}

	public static String[] generateProgram(int numRoles, Random sleepRandom ){

		String[] roles = new String[ numRoles ];

		for( int i = 0; i < roles.length; i++ ) {
			roles[i] = "R" + i;
		}

		try ( PrintWriter writer = new PrintWriter(new File(destinationFolder, className + ".ch"))) {
			writer.println("import choral.channels.SymChannel;");
			writer.println("import java.util.Random;");
			writer.print("public class " );
			writer.print( className );
			writer.print( "@(" );
			writer.print( String.join( ",", roles ) );
			writer.println( "){" );

			writer.print(channelDeclarations( roles, ";\n" ));
			writer.println(';');

			for( String s : roles ) {
				writer.print( "Random@" );
				writer.print( s );
				writer.print( " random_" );
				writer.print( s );
				writer.println( ";" );
			}

			writer.print( "public " );
			writer.print( className );
			writer.println( '(' );

			writer.print(channelDeclarations( roles, ",\n" ));
			writer.println();
			writer.println( "){" );

			for( int i = 0; i < roles.length; i++ ) {
				for( int j = i + 1; j < roles.length; j++ ) {
					String var_name = channelName( roles[i], roles[j] );
					writer.print( "this." );
					writer.print( var_name );
					writer.print( " = " );
					writer.print( var_name );
					writer.println( ';' );
				}
			}

			for( String s : roles ) {
				writer.print( "random_" );
				writer.print( s );
				writer.print( " = new Random(" );
				writer.print( sleepRandom.nextInt() );
				writer.println( ");" );
			}

			writer.println( '}' );

			writer.println( "public void run(){" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "}" );

			writer.println( "public void run2(){" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "}" );

			writer.println( "public void run3(){" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "}" );

			writer.println( "public void run4(){" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "run5();" );
			writer.println( "}" );

			writer.println( "public void run5(){" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "selection();" );
			writer.println( "}" );

			writer.println( "public void selection(){" );

			writer.println( "if( false@R0 ){" );

			writer.println( "try {" );
			for( String role : roles ) {
				writer.print( "Thread@" );
				writer.print( role );
				writer.print( ".sleep(" );
				writer.print( "random_" );
				writer.print( role );
				writer.print( ".nextInt( 4 ) + 1" );
				writer.println( " );" );
			}
		/*

		for( int i = 0, rolesLength = roles.length; i < rolesLength; i++ ) {
			String role = roles[ i ];
			writer.print( "Thread@" );
			writer.print( role );
			writer.print( ".sleep(" );
			writer.print( i );
			writer.println( " );" );
		}
		*/
			writer.println('}');
			for( String role : roles ) {
				writer.print( "catch( Exception@" );
				writer.print( role );
				writer.println( " e ) {e.printStackTrace();}" );
			}

			writer.println('}');
			writer.println('}');
			writer.println('}');

		} catch( FileNotFoundException e ) {
			e.printStackTrace();
		}

		return roles;
	}

	private static String channelDeclaration( String role1, String role2 ){
		return "SymChannel@(" + role1 + "," + role2 + ")< Object > " + channelName( role1, role2 );
	}

	private static String channelName(String role1, String role2){
		if( role1.compareTo( role2 ) < 0 ){
			return "ch_" + role1 + "_" + role2;
		}else {
			return "ch_" + role2 + "_" + role1;
		}
	}

	private static String channelDeclarations( String[] roles, String delimiter ){
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < roles.length; i++ ) {
			for( int j = i + 1; j < roles.length; j++ ) {
				if( i > 0 || j > 1 ){
					sb.append( delimiter );
				}

				sb.append( channelDeclaration( roles[i], roles[j] ) );
			}
		}
		return sb.toString();
	}
}
