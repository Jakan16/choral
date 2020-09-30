package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
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

public abstract class Template {
	private final List< ImportDeclaration > importDeclarations;
	private final Package holdingPackage;
	private Map<String, Template > knownSymbols;
	private final Map<String, Template > genericTemplates;

	Template( List< ImportDeclaration > importDeclarations, Package holdingPackage, List< String > generics ) {
		this.importDeclarations = importDeclarations;
		this.holdingPackage = holdingPackage;

		genericTemplates = Mapper.createMap(
				generics,
				generics,
				Mapper.id(),
				t -> new GenericTemplate( holdingPackage.getRoot(), t ) );
	}

	public static Template unknownTemplate() {
		return UnknownTemplate.unknownTemplate();
	}

	public Package getHoldingPackage() {
		return holdingPackage;
	}

	private void populateKnownSymbols(){
		knownSymbols = new HashMap<>();
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

	public void prepareSymbols(){
		if( knownSymbols == null ){
			populateKnownSymbols();
		}
	}

	public Template resolveIdentifier( String identifier ){
		prepareSymbols();
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

	public abstract String getName();

	public abstract  List< VariableDNode > getFields();

	public abstract VariableDNode getField( String identifier );

	public abstract List< MethodSignature > getMethodSigs();

	public Template.MethodSignature getMethodSig( String name, int numArgs ){
		for( Template.MethodSignature signature: getMethodSigs() ){
			if( signature.getName().equals( name ) &&
					signature.getParameters().size() == numArgs ) {
				return signature;
			}
		}
		throw new IllegalStateException();
	}

	public abstract List< FormalWorldParameter > worldParameters();

	public abstract List< FormalTypeParameter > typeParameters();

	public boolean isGeneric(){
		return false;
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
