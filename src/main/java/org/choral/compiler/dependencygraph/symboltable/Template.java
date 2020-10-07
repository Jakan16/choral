package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.*;
import org.choral.ast.expression.MethodCallExpression;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.*;
import java.util.stream.Collectors;

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

		genericTemplates = Mapper.mapping(
				generics,
				t -> t.name().identifier(),
				t -> new GenericTemplate( this, t ) );
	}

	public static Template unknownTemplate() {
		return UnknownTemplate.unknownTemplate();
	}

	void prepare(){
		if( knownSymbols == null ){
			populateKnownSymbols();
			superTypes = prepareSuperType();
			createMappings();
		}
	}

	private void createMappings(){
		for( DType sType: this.superTypes ){
			Template sTem = sType.getTem();
			this.roleMaps.add( Mapper.mapping( sTem.worldParameters(), sType.getRoles(),
					w -> w.name().identifier(), Mapper.id() ) );
			this.genericMaps.add( Mapper.mapping( sTem.typeParameters(), sType.getTypeArguments(),
					g -> g.name().identifier(), Mapper.id() ) );
		}
	}

	private List< DVariable > deriveFields(){
		List< DVariable > fields = Mapper.map( fields(), f ->
				new DVariable( f.name().identifier(),
						typeExpressionToNode( f.typeExpression() ) ) );

		for( int i = 0; i < this.superTypes.size(); i++ ) {
			int index = i;
			this.superTypes.get( i ).getTem().getFields().stream()
					.map( v -> new DVariable( v.getName(), mapType( v.getType(), index ) ) )
					.forEach( fields::add );
		}

		return fields;
	}

	private List< MethodSig > deriveMethodSigs(){
		List< MethodSig > definitions = methodDefinitions().stream()
				.map( MethodDefinition::signature ).map( s -> new MethodSig( s, this ) )
				.collect( Collectors.toList() );

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
		List< MethodSig > constructorSigs = constructorDefinitions().stream()
				.map( ConstructorDefinition::signature ).map( s -> new MethodSig( s, this ) )
				.collect( Collectors.toList() );
		constructorSigs.add( new MethodSig( getName(), Collections.emptyList(), Collections.emptyList(), null ) );
		return constructorSigs;
	}

	private DType mapType( DType type, int index ){
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

	public void populateKnownSymbols(){
		knownSymbols = new HashMap<>();

		if( isGeneric() ){
			// Generics uses symbols of its parent
			return;
		}

		Package javaLang = holdingPackage.getRoot().getPackage( "java.lang" );
		for( Template def: javaLang.getTemplates() ){
			knownSymbols.put( def.getName(), def );
		}

		for( Template def: holdingPackage.getTemplates() ){
			knownSymbols.put( def.getName(), def );
		}

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
		return Template.unknownTemplate();
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

	public List< DVariable > getFields(){
		if( this.fields == null ){
			this.fields = deriveFields();
		}
		return this.fields;
	}

	public DVariable getField( String identifier ){
		for( DVariable field : getFields() ) {
			if( field.getName().equals( identifier ) ) {
				return field;
			}
		}
		throw new IllegalStateException( "Could not find " + identifier + " field in " + getName() );
	}

	public List< MethodSig > getMethodSigs(){
		prepare();
		if( this.methodSigs == null ){
			this.methodSigs = deriveMethodSigs();
		}
		return this.methodSigs;
	}

	public MethodSig getMethodSig( MethodCallExpression callExpression ){
		return getMethodSig(callExpression.name().identifier(), callExpression.arguments().size());
	}

	public MethodSig getMethodSig( MethodDefinition md ){
		return getMethodSig(md.signature().name().identifier(), md.signature().parameters().size());
	}

	public MethodSig getMethodSig( String name, int numArgs ){
		for( MethodSig signature: getMethodSigs() ){
			if( signature.getName().equals( name ) &&
					signature.getParameters().size() == numArgs ) {
				return signature;
			}
		}
		throw new IllegalStateException( "Unknown function: " + name +
				" with " + numArgs + " argument(s)" );
	}

	public List< MethodSig > getConstructorSigs() {
		if( this.constructorSigs == null ){
			this.constructorSigs = deriveConstructorSigs();
		}
		return constructorSigs;
	}

	public MethodSig getConstructorSig( int numArgs ){
		for( MethodSig signature: getConstructorSigs() ){
			if( signature.getParameters().size() == numArgs ){
				return signature;
			}
		}

		throw new IllegalStateException();
	}

	public abstract String getName();

	public abstract List< DType > prepareSuperType();

	public abstract List< FormalWorldParameter > worldParameters();

	public abstract List< FormalTypeParameter > typeParameters();

	public abstract List< ? extends MethodDefinition > methodDefinitions();

	public List< ConstructorDefinition > constructorDefinitions(){
		return Collections.emptyList();
	}

	public abstract List< Field > fields();

	public boolean isGeneric(){
		return false;
	}

	public List< DType > getSuperTypes() {
		return superTypes;
	}

	public Package getHoldingPackage() {
		return holdingPackage;
	}

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

		public MethodSig( MethodSignature sig, Template parentTemplate ) {
			this.typeParameters = Mapper.map( sig.typeParameters(),
					t -> new GenericTemplate( parentTemplate, t ) );
			Map< String, Template > funcGenericsMap =
					Mapper.mapping( this.typeParameters, Template::getName, Mapper.id() );
			this.typeParameters.forEach( t -> t.setFuncGenericsMap( funcGenericsMap ) );
			this.name = sig.name().identifier();
			this.parameters = sig.parameters().stream().map( FormalMethodParameter::type )
					.map( t ->
							funcGenericsMap.getOrDefault( t.name().identifier(), parentTemplate )
							.typeExpressionToNode( t )
					).collect( Collectors.toList() );

			this.returnType = funcGenericsMap.getOrDefault(
					sig.returnType().name().identifier(), parentTemplate
			).typeExpressionToNode( sig.returnType() );
		}

		public MethodSig( ConstructorSignature sig, Template parentTemplate ) {
			this.typeParameters = Mapper.map( sig.typeParameters(),
					t -> new GenericTemplate( parentTemplate, t ) );
			Map< String, Template > funcGenericsMap =
					Mapper.mapping( this.typeParameters, Template::getName, Mapper.id() );
			this.typeParameters.forEach( t -> t.setFuncGenericsMap( funcGenericsMap ) );
			this.name = sig.name().identifier();
			this.parameters = sig.parameters().stream().map( FormalMethodParameter::type )
					.map( t ->
							funcGenericsMap.getOrDefault( t.name().identifier(), parentTemplate )
									.typeExpressionToNode( t )
					).collect( Collectors.toList() );

			this.returnType = null;
		}

		public String getName() {
			return name;
		}

		public List< DType > getParameters() {
			return parameters;
		}

		public DType getReturnType() {
			return returnType;
		}

		public List< GenericTemplate > getTypeParameters() {
			return typeParameters;
		}
	}
}
