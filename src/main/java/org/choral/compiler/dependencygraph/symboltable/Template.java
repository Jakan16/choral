package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Field;
import org.choral.ast.body.FormalMethodParameter;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.TypeDNode;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Template {
	private final List< ImportDeclaration > importDeclarations;
	private final Package holdingPackage;
	private Map<String, Template > knownSymbols;
	private final Map<String, GenericTemplate > genericTemplates;
	private final List< Map< String, String > > roleMaps = new ArrayList<>();
	private final List< Map< String, TypeDNode > > genericMaps = new ArrayList<>();
	private List< MethodSignature > methodDefinitions;
	private List< VariableDNode > fields;
	private List< TypeDNode > superTypes;

	Template( List< ImportDeclaration > importDeclarations, Package holdingPackage, List< FormalTypeParameter > generics ) {
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
		for( TypeDNode sType: this.superTypes ){
			Template sTem = sType.getTem();
			this.roleMaps.add( Mapper.mapping( sTem.worldParameters(), sType.getRoles(), w -> w.name().identifier(), Mapper.id() ) );
			this.genericMaps.add( Mapper.mapping( sTem.typeParameters(), sType.getTypeArguments(), g -> g.name().identifier(), Mapper.id() ) );
		}
	}

	private List< VariableDNode > deriveFields(){
		prepare();

		List< VariableDNode > fields = Mapper.map( fields(), f -> new VariableDNode( f.name().identifier(), typeExpressionToNode( f.typeExpression() ) ) );

		for( int i = 0; i < this.superTypes.size(); i++ ) {
			int index = i;
			this.superTypes.get( i ).getTem().getFields().stream()
					.map( v -> new VariableDNode( v.getName(), mapType( v.getType(), index ) ) )
					.forEach( fields::add );
		}

		return fields;
	}

	private List< MethodSignature > deriveMethodDefinitions(){
		prepare();

		List< MethodSignature > definitions = methodDefinitions().stream()
				.map( MethodDefinition::signature )
				.map( s -> new MethodSignature(
						s.name().identifier(),
						s.parameters().stream().map( FormalMethodParameter::type )
								.map( this::typeExpressionToNode ).collect(	Collectors.toList() ),
						typeExpressionToNode( s.returnType() ) ) )
				.collect( Collectors.toList() );

		for( int i = 0; i < this.superTypes.size(); i++ ) {
			int index = i;
			this.superTypes.get( i ).getTem().getMethodSigs().stream().map( s ->
					new MethodSignature( s.getName(),
							Mapper.map( s.getParameters(), p -> this.mapType(p, index ) ),
							mapType( s.getReturnType(), index ) )
			).forEach( definitions::add );
		}

		return definitions;
	}

	private TypeDNode mapType( TypeDNode type, int index ){
		if( type.getTem().isGeneric() ){
			TypeDNode replaceType = this.genericMaps.get( index ).get( type.getName() );
			return new TypeDNode( replaceType.getTem(), Mapper.map( type.getRoles(), this.roleMaps.get( index )::get ), replaceType.getTypeArguments() );
		}else{
			return type.copyWithMapping( this.roleMaps.get( index ) );
		}
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

	protected TypeDNode typeExpressionToNode( TypeExpression typeExpression ){
		List< TypeDNode > typeArgs = new ArrayList<>();
		for( TypeExpression t: typeExpression.typeArguments() ){
			typeArgs.add( typeExpressionToNode(t) );
		}

		return new TypeDNode(
				resolveIdentifier( typeExpression.name().identifier() ),
				Mapper.map( typeExpression.worldArguments(), w -> w.name().identifier() ),
				typeArgs );
	}

	protected List< TypeDNode > typeExpressionsToNodes( List< TypeExpression > typeExpressions ){
		return Mapper.map( typeExpressions, this::typeExpressionToNode );
	}

	public List< VariableDNode > getFields(){
		if( this.fields == null ){
			this.fields = deriveFields();
		}
		return this.fields;
	}

	public VariableDNode getField( String identifier ){
		for( VariableDNode field : getFields() ) {
			if( field.getName().equals( identifier ) ) {
				return field;
			}
		}
		throw new IllegalStateException();
	}

	public List< MethodSignature > getMethodSigs(){
		prepare();
		if( this.methodDefinitions == null ){
			this.methodDefinitions = deriveMethodDefinitions();
		}
		return this.methodDefinitions;
	}

	public Template.MethodSignature getMethodSig( String name, int numArgs ){
		for( Template.MethodSignature signature: getMethodSigs() ){
			if( signature.getName().equals( name ) &&
					signature.getParameters().size() == numArgs ) {
				return signature;
			}
		}
		throw new IllegalStateException( "Unknown function: " + name + " with " + numArgs + " argument(s)" );
	}

	public abstract String getName();

	public abstract List< TypeDNode > prepareSuperType();

	public abstract List< FormalWorldParameter > worldParameters();

	public abstract List< FormalTypeParameter > typeParameters();

	public abstract List< ? extends MethodDefinition > methodDefinitions();

	public abstract List< Field > fields();

	public boolean isGeneric(){
		return false;
	}

	public List< TypeDNode > getSuperTypes() {
		return superTypes;
	}

	public Package getHoldingPackage() {
		return holdingPackage;
	}

	public static class MethodSignature{
		private final String name;
		//TODO support typeParameters
		//private final List< TypeExpression > typeParameters;
		private final List< TypeDNode > parameters;
		private final TypeDNode returnType;

		public MethodSignature(
				String name, List< TypeDNode > parameters,
				TypeDNode returnType
		) {
			this.name = name;
			this.parameters = parameters;
			this.returnType = returnType;
		}

		public String getName() {
			return name;
		}

		public List< TypeDNode > getParameters() {
			return parameters;
		}

		public TypeDNode getReturnType() {
			return returnType;
		}
	}
}
