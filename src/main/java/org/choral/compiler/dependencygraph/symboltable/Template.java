package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.*;
import org.choral.ast.expression.MethodCallExpression;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for templates representing various types such as class, interfaces, and generics.
 */
public abstract class Template {
	private final List< ImportDeclaration > importDeclarations;
	private final Package holdingPackage;
	private Map<String, Template > knownSymbols;
	private final Map<String, GenericTemplate > genericTemplates;
	private final List< Map< String, String > > roleMaps = new ArrayList<>();
	private final List< Map< String, DType > > genericMaps = new ArrayList<>();
	private List< MethodSig > methodSigs;
	private List< MethodSig > constructorSigs;
	private List< DVariable > fields;
	private List< DType > superTypes;

	Template(
			List< ImportDeclaration > importDeclarations,
			Package holdingPackage,
			List< FormalTypeParameter > generics
	) {
		this.importDeclarations = importDeclarations;
		this.holdingPackage = holdingPackage;

		// Collection over generics defined by the type
		genericTemplates = Mapper.mapping(
				generics,
				t -> t.name().identifier(),
				t -> new GenericTemplate( this, t ) );
	}

	/**
	 * A singleton instance of {@link UnknownTemplate}, used whenever a type could not be resolved.
	 * @return A reference to the same {@link UnknownTemplate} instance.
	 */
	public static Template unknownTemplate() {
		return UnknownTemplate.unknownTemplate();
	}

	void prepare(){
		// Symbols cannot be prepared eagerly, as they may reference Templates yet to be loaded
		if( knownSymbols == null ){
			populateKnownSymbols();
			superTypes = prepareSuperType();
			createMappings();
		}
	}

	private void createMappings(){
		// creates mappings for each super class.
		for( DType sType: this.superTypes ){
			Template sTem = sType.getTem();
			this.roleMaps.add( Mapper.mapping( sTem.worldParameters(), sType.getRoles() ) );
			this.genericMaps.add(
					Mapper.mapping( sTem.typeParameters(),
					sType.getTypeArguments() ) );
		}
	}

	private List< DVariable > deriveFields(){
		// Collect fields of the type represented by this Template
		List< DVariable > fields = Mapper.map( fields(), f ->
				new DVariable( f.name().identifier(),
						typeExpressionToNode( f.typeExpression() ) ) );

		// Add fields from super types, mapping roles
		for( int i = 0; i < this.superTypes.size(); i++ ) {
			int index = i;
			this.superTypes.get( i ).getTem().getFields().stream()
					.map( v -> new DVariable( v.getName(), mapType( v.getType(), index ) ) )
					.forEach( fields::add );
		}

		return fields;
	}

	private List< MethodSig > deriveMethodSigs(){
		// Collect method signatures of the type represented by this Template
		List< MethodSig > definitions = methodDefinitions().stream()
				.map( MethodDefinition::signature ).map( s -> new MethodSig( s, this ) )
				.collect( Collectors.toList() );

		// Add method signatures from super types, mapping roles
		for( int i = 0; i < this.superTypes.size(); i++ ) {
			int index = i;
			this.superTypes.get( i ).getTem().getMethodSigs().stream().map( s -> new MethodSig(
					s.getName(),
					Mapper.map( s.getParameters(), p -> this.mapType(p, index ) ),
					s.getTypeParameters(),
					mapType( s.getReturnType(), index ) ) ).forEach( definitions::add );
		}

		return definitions;
	}

	private List< MethodSig > deriveConstructorSigs(){
		// Collect constructor signatures
		List< MethodSig > constructorSigs = constructorDefinitions().stream()
				.map( ConstructorDefinition::signature )
				.map( s -> new MethodSig( s, this ) ).collect( Collectors.toList() );

		// Make sure the default no parameter constructor is available.
		constructorSigs.add( new MethodSig( getName(),
				Collections.emptyList(),
				Collections.emptyList(),
				null ) );
		return constructorSigs;
	}

	private DType mapType( DType type, int index ){
		// Maps a type from the index'th super type into this types world
		if( type.getTem().isGeneric() ){
			DType replaceType = this.genericMaps.get( index ).get( type.getName() );
			if( replaceType != null ) {
				return new DType( replaceType.getTem(),
						Mapper.map( type.getRoles(), this.roleMaps.get( index )::get ),
						replaceType.getTypeArguments() );
			}
		}
		return type.copyWithMapping( this.roleMaps.get( index ) );
	}

	void populateKnownSymbols(){
		knownSymbols = new HashMap<>();

		if( isGeneric() ){
			// Generics uses symbols of its parent
			return;
		}

		// java.lang classes are always implicit imported
		Package javaLang = holdingPackage.getRoot().getPackage( PackageHandler.langPath );
		for( Template def: javaLang.getTemplates() ){
			knownSymbols.put( def.getName(), def );
		}

		// Get all classes sharing package with this class
		for( Template def: holdingPackage.getTemplates() ){
			knownSymbols.put( def.getName(), def );
		}

		// add all imported classes
		for( ImportDeclaration dec: importDeclarations ) {
			int lastIndexOf = dec.name().lastIndexOf(".");
			String packagePart = dec.name().substring(0, lastIndexOf);
			String symbolPart = dec.name().substring(lastIndexOf+1 );

			Package importPackage = holdingPackage.getRoot().getPackage( packagePart );
			if( symbolPart.equals( "*" ) ){
				for( Template def: importPackage.getTemplates() ){
					knownSymbols.put( def.getName(), def );
				}
			}else{
				Template def = importPackage.getTemplate( symbolPart );
				assert def.getName().equals( symbolPart );
				knownSymbols.put( symbolPart, def );
			}
		}
	}

	private Template getPrimitive( String identifier ){
		return holdingPackage.getPackageHandler().getPrimitive( identifier );
	}

	public Template resolveIdentifier( String identifier ){
		prepare();
		Template tem = knownSymbols.get( identifier );
		if( tem != null ){
			return tem;
		}
		tem = genericTemplates.get( identifier );
		if( tem != null ){
			return tem;
		}
		return getPrimitive( identifier );
	}

	protected DType typeExpressionToNode( TypeExpression typeExpression ){
		List< DType > typeArgs = new ArrayList<>();
		for( TypeExpression t: typeExpression.typeArguments() ){
			typeArgs.add( typeExpressionToNode(t) );
		}

		return new DType(
				resolveIdentifier( typeExpression.name().identifier() ),
				Mapper.map( typeExpression.worldArguments(), w -> w.name().identifier() ),
				typeArgs );
	}

	protected List< DType > typeExpressionsToNodes( List< TypeExpression > typeExpressions ){
		return Mapper.map( typeExpressions, this::typeExpressionToNode );
	}

	/**
	 * Get all fields reachable from this Template.
	 * @return A list of Fields
	 */
	public List< DVariable > getFields(){
		if( this.fields == null ){
			this.fields = deriveFields();
		}
		return this.fields;
	}

	/**
	 * finds a field reachable by this Template, with the identifier name.
	 * @param identifier The name of the Field
	 * @return A node representing the Field
	 */
	public DVariable getField( String identifier ){
		for( DVariable field : getFields() ) {
			if( field.getName().equals( identifier ) ) {
				return field;
			}
		}
		throw new IllegalStateException( "Could not find " + identifier + " field in " + getName() );
	}

	/**
	 * gets all method signatures reachable by this Template
	 * @return A list of method signatures
	 */
	public List< MethodSig > getMethodSigs(){
		prepare();
		if( this.methodSigs == null ){
			this.methodSigs = deriveMethodSigs();
		}
		return this.methodSigs;
	}

	/**
	 * Returns the method signature used by the given callExpression
	 * @param callExpression The expression to find the signature for
	 * @return a method signature
	 */
	public MethodSig getMethodSig( MethodCallExpression callExpression ){
		return getMethodSig(callExpression.name().identifier(), callExpression.arguments().size());
	}

	/**
	 * Returns the method signature used by the given method definition
	 * @param md the method definition to find the signature for
	 * @return a method signature
	 */
	public MethodSig getMethodSig( MethodDefinition md ){
		return getMethodSig(md.signature().name().identifier(), md.signature().parameters().size());
	}

	/**
	 * Returns the method signature with the given name and number of arguments
	 * @param name The name of the method signature
	 * @param numParam The number of parameters the signature have
	 * @return a method signature
	 */
	public MethodSig getMethodSig( String name, int numParam ){
		for( MethodSig signature: getMethodSigs() ){
			if( signature.getName().equals( name ) &&
					signature.getParameters().size() == numParam ) {
				return signature;
			}
		}
		throw new IllegalStateException( "Unknown function: " + name +
				" with " + numParam + " argument(s)" );
	}

	/**
	 * Get all constructors for this Template
	 * @return A list of constructor signatures
	 */
	public List< MethodSig > getConstructorSigs() {
		if( this.constructorSigs == null ){
			this.constructorSigs = deriveConstructorSigs();
		}
		return constructorSigs;
	}

	/**
	 * Get the constructor with the given number of parameters.
	 * @param numParam number of parameters of the constructor
	 * @return A constructor signature
	 */
	public MethodSig getConstructorSig( int numParam ){
		for( MethodSig signature: getConstructorSigs() ){
			if( signature.getParameters().size() == numParam ){
				return signature;
			}
		}

		throw new IllegalStateException();
	}

	/**
	 * The name of the type represented by this Template
	 * @return name of the template
	 */
	public abstract String getName();

	/**
	 * Loads all super types of this type
	 * @return A list of all super types
	 */
	public abstract List< DType > prepareSuperType();

	/**
	 * The role parameters of this template
	 * @return A list of role parameters
	 */
	public abstract List< String > worldParameters();

	/**
	 * Names of all type parameters of this type
	 * @return A list of type parameters
	 */
	public abstract List< String > typeParameters();

	/**
	 * All method definitions defined directly within this template.
	 * This method is for convenience, so each subtype can return a list in a uniform way.
	 * @return A list of method definitions
	 */
	public List< ? extends MethodDefinition > methodDefinitions(){
		return Collections.emptyList();
	}

	/**
	 * All constructors defined directly within this template.
	 * This method is for convenience, so each subtype can return a list in a uniform way.
	 * @return A list of constructors
	 */
	public List< ConstructorDefinition > constructorDefinitions(){
		return Collections.emptyList();
	}

	/**
	 * All fields defined directly within this template.
	 * This method is for convenience, so each subtype can return a list in a uniform way.
	 * @return A list of fields
	 */
	public List< Field > fields(){
		return Collections.emptyList();
	}

	/**
	 * Whether or not this Template is a generic, that could be substituted
	 * @return {@code true} If this Template represents a generic
	 */
	public boolean isGeneric(){
		return false;
	}

	public List< DType > getSuperTypes() {
		return superTypes;
	}

	public Package getHoldingPackage() {
		return holdingPackage;
	}

	/**
	 * A class representing a method signature
	 */
	public static class MethodSig {
		private final String name;
		private final List< GenericTemplate > typeParameters;
		private final List< DType > parameters;
		private final DType returnType;

		public MethodSig(
				String name, List< DType > parameters,
				List< GenericTemplate > typeParameters,
				DType returnType
		) {
			this.name = name;
			this.parameters = parameters;
			this.returnType = returnType;
			this.typeParameters = typeParameters;
		}

		private MethodSig(
				List< FormalTypeParameter > typeParameters,
				List< FormalMethodParameter > parameters,
				String name,
				TypeExpression returnType,
				Template parentTemplate
		) {
			// method specific generics
			this.typeParameters = Mapper.map( typeParameters,
					t -> new GenericTemplate( parentTemplate, t ) );
			Map< String, Template > funcGenericsMap =
					Mapper.mapping( this.typeParameters, Template::getName, Mapper.id() );
			// make each method generic aware of other method generics
			this.typeParameters.forEach( t -> t.setFuncGenericsMap( funcGenericsMap ) );
			this.name = name;
			this.parameters = parameters.stream().map( FormalMethodParameter::type )
					.map( t -> funcGenericsMap.getOrDefault( t.name().identifier(), parentTemplate )
							.typeExpressionToNode( t )
					).collect( Collectors.toList() );

			if( returnType != null ) {
				this.returnType = funcGenericsMap.getOrDefault(
						returnType.name().identifier(), parentTemplate
				).typeExpressionToNode( returnType );
			}else {
				this.returnType = null;
			}
		}

		public MethodSig( MethodSignature sig, Template parentTemplate ) {
			this( sig.typeParameters(),
					sig.parameters(),
					sig.name().identifier(),
					sig.returnType(),
					parentTemplate );
		}

		public MethodSig( ConstructorSignature sig, Template parentTemplate ) {
			this( sig.typeParameters(),
					sig.parameters(),
					sig.name().identifier(),
					null,
					parentTemplate );
		}

		public String getName() {
			return name;
		}

		public List< DType > getParameters() {
			return parameters;
		}

		public List< GenericTemplate > getTypeParameters() {
			return typeParameters;
		}

		public DType getReturnType() {
			return returnType;
		}
	}
}
