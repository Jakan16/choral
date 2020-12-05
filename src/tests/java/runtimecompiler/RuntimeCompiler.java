package runtimecompiler;

import choral.Choral;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import choral.runtime.LocalChannel.LocalChannel_A;
import choral.runtime.LocalChannel.LocalChannel_B;
import choral.runtime.Media.MessageQueue;
import picocli.CommandLine;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RuntimeCompiler {

	public static final String A = "A";
	public static final String B = "B";
	public static final String C = "C";
	public static final String D = "D";
	public static final String E = "E";
	public static final String F = "F";

	private final String root;
	private final String destinationFolder = "build/generatedFromChoral/java/";
	private final String classPath;
	private final List< String > roles;
	private List< Object > instantiations;

	public static RuntimeCompiler compile( String src, String classPath, List< String > roles ) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
		return new RuntimeCompiler( src, classPath, roles ).compile();
	}

	public RuntimeCompiler( String src, String classPath, List< String > roles ) {
		this.root = src;
		this.classPath = classPath;
		this.roles = roles;
	}

	private RuntimeCompiler compile() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {

		String[] split = classPath.split( "\\." );
		String dest = destinationFolder + split[split.length-1] + "/";

		// delete any previous files
		deleteDirectory( Paths.get( dest ).toFile() );

		// compile choral -> java
		String[] arguments = new String[] {
				"epp",
				"-t", dest,
				"-s", root,
				classPath };

		CommandLine cl = new CommandLine( new Choral() );
		cl.setToggleBooleanFlags( true );
		cl.setCaseInsensitiveEnumValuesAllowed( true );
		int exitCode = cl.execute( arguments );
		if( exitCode != 0 ) {
			throw new IllegalStateException( "Compilation failed" );
		}

		// compile generated java files
		String[] files = Files.walk( Paths.get( dest ) )
				.filter( Files::isRegularFile )
				.filter( f -> f.toString().endsWith( ".java" ) )
				.map( f -> f.toFile().getPath() ).toArray( String[]::new );

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run( null, null, null, files );

		URLClassLoader classLoader = URLClassLoader.newInstance(
				new URL[] { new File( dest ).toURI().toURL() } );

		MessageQueue ab = new MessageQueue();
		MessageQueue ba = new MessageQueue();

		SymChannel_A< Object > channel_a_b = new LocalChannel_A( ab, ba );
		SymChannel_B< Object > channel_b_a = new LocalChannel_B( ba, ab );

		this.instantiations = new ArrayList<>();

		final int numParam = ( this.roles.size() * ( this.roles.size() - 1 ) ) / 2;

		Channel[] channels = channels( numParam );

		for( String role : this.roles ) {
			Class< ? > cls = Class.forName( className( this.classPath, role ), true, classLoader );

			Optional< Constructor< ? > > constructorOptional = Arrays.stream(
					cls.getDeclaredConstructors() )
					.filter( c -> Modifier.isPublic( c.getModifiers() ) )
					.filter( c -> c.getParameterCount() == numParam )
					.filter( c -> {
						for( Class< ? > parameterType : c.getParameterTypes() ) {
							if( !parameterType.isInstance(
									channel_a_b ) && !parameterType.isInstance(
									channel_b_a ) && !parameterType.isInstance( Unit.id ) ) {
								return false;
							}
						}
						return true;
					} ).findAny();

			if( constructorOptional.isEmpty() ) {
				throw new IllegalStateException( "Appropriate Constructor does not exist." );
			}

			Constructor< ? > constructor = constructorOptional.get();

			Object[] roleChannels = new Object[ numParam ];

			for( int i = 0; i < constructor.getParameterCount(); i++ ) {
				Class< ? > parameter = constructor.getParameterTypes()[ i ];
				if( parameter.isInstance( channels[ i ].getChannel_a() ) ) {
					roleChannels[ i ] = channels[ i ].getChannel_a();
				} else if( parameter.isInstance( channels[ i ].getChannel_b() ) ) {
					roleChannels[ i ] = channels[ i ].getChannel_b();
				} else {
					roleChannels[ i ] = Unit.id;
				}
			}

			this.instantiations.add( constructor.newInstance( roleChannels ) );
		}

		return this;
	}

	public MethodCallBuilder method( String name ) {
		return new MethodCallBuilder( name );
	}

	public Result invokeMethod( String name ) throws NoSuchMethodException, ExecutionException, InterruptedException {
		return new MethodCallBuilder( name ).invoke();
	}

	private static Method getMethodDeclaration(Class< ? > clazz, String methodName, Object[] args) throws NoSuchMethodException {
		Optional< Method > optionalMethod = Arrays.stream( clazz.getDeclaredMethods() )
				.filter( m -> m.getParameterCount() == args.length )
				.filter( m -> {
					Class< ? >[] parameterTypes = m.getParameterTypes();
					for( int j = 0, length = parameterTypes.length; j < length; j++ ) {
						if( !parameterTypes[ j ].isInstance( args[ j ] ) ){ return false; }
					}
					return true;
				} ).findAny();
		if( optionalMethod.isPresent() ){
			return optionalMethod.get();
		}
		StringBuilder builder = new StringBuilder();
		builder.append( methodName ).append( '(' );

		boolean first = true;
		for( Object object: args ){
			if( !first ){
				builder.append( ", " );
			}
			first = false;
			builder.append( object.getClass() );
		}

		builder.append( ')' );

		throw new NoSuchMethodException( builder.toString() );
	}

	private static String className( String classPath, String role ) {
		return classPath.replaceAll( "(\\|/)", "." ) + "_" + role;
	}

	private static boolean deleteDirectory( File directoryToBeDeleted ) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if( allContents != null ) {
			for( File file : allContents ) {
				deleteDirectory( file );
			}
		}
		return directoryToBeDeleted.delete();
	}

	private static Channel[] channels( int count ) {
		List< Channel > channels = new ArrayList<>( count );
		for( int i = 0; i < count; i++ ) {
			channels.add( new Channel() );
		}
		return channels.toArray( Channel[]::new );
	}

	private static class Channel {
		private final SymChannel_A< Object > channel_a;
		private final SymChannel_B< Object > channel_b;

		Channel() {
			MessageQueue ab = new MessageQueue();
			MessageQueue ba = new MessageQueue();

			this.channel_a = new LocalChannel_A( ab, ba );
			this.channel_b = new LocalChannel_B( ba, ab );
		}

		public SymChannel_A< Object > getChannel_a() {
			return channel_a;
		}

		public SymChannel_B< Object > getChannel_b() {
			return channel_b;
		}
	}

	public class MethodCallBuilder{
		private final String methodName;
		private final List< Object[] > args = new ArrayList<>( instantiations.size() );
		private final Object[] emptyArray = new Object[0];

		private MethodCallBuilder( String methodName ) {
			this.methodName = methodName;
			for( int i = 0; i < instantiations.size(); i++ ) {
				args.add( emptyArray );
			}
		}

		public MethodCallBuilder argAt( String role, Object[] arguments ){

			int index = roles.indexOf( role );
			assert args.get( index ) == emptyArray;
			args.set( index, arguments );
			return this;
		}

		public MethodCallBuilder argAt( String role, Object argument ){
			return this.argAt( role, new Object[]{ argument } );
		}

		public MethodCallBuilder argAt( String role, List< Object > arguments ){
			return this.argAt( role, arguments.toArray() );
		}

		public Result invoke() throws NoSuchMethodException, ExecutionException, InterruptedException {
			List< CompletableFuture< Object > > tasks = new ArrayList<>();
			for( int i = 0, objectsSize = instantiations.size(); i < objectsSize; i++ ) {
				final int index = i;
				Object instantiation = instantiations.get( index );
				Method method = getMethodDeclaration( instantiation.getClass(), methodName, args.get( index ) );

				tasks.add( CompletableFuture.supplyAsync( () -> {
					try {
						return method.invoke( instantiation, args.get( index ) );
					} catch( InvocationTargetException e ) {
						return new RoleException( "Error occurred at role " + roles.get( index ), e );
					} catch( IllegalAccessException e ) {
						return e;
					}
				} ) );
			}

			final List< Object > results = new ArrayList<>( tasks.size() );
			for( CompletableFuture< Object > future : tasks ) {
				results.add( future.get() );
			}
			return new Result( results );
		}
	}

	public class Result{

		private final List< Object > result;

		private Result( List< Object > result ) {
			this.result = result;
		}

		public List< Object > getResult() {
			return result;
		}

		public Result assertNoErrors() throws Throwable {
			Assert.assertNoErrors( result );
			return this;
		}

		public Result assertEqualAt( String role, Object targetValue ) {
			Assert.assertEqual( result, targetValue, roles.indexOf( role ) );
			return this;
		}
	}
}
