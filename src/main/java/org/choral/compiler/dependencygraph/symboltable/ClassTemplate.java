package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Class;
import org.choral.ast.body.FormalMethodParameter;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.TypeDNode;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassTemplate extends Template {
	private final Class classNode;
	private List< MethodSignature > methodDefinitions;
	private List< VariableDNode > fields;
	private Map< String, String > roleMap;
	private Map< String, TypeDNode > genericMap;

	ClassTemplate( Class classNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super(importDeclarations, holdingPackage, Mapper.map( classNode.typeParameters(), t -> t.name().identifier() ) );
		this.classNode = classNode;
	}

	private void createMappings(){
		TypeDNode sType = typeExpressionToNode( classNode.extendsClass() );
		Template superClass = sType.getTem();
		this.roleMap = Mapper.createMap( superClass.worldParameters(), sType.getRoles(), w -> w.name().identifier(), Mapper.id() );
		this.genericMap = Mapper.createMap( superClass.typeParameters(), sType.getTypeArguments(), g -> g.name().identifier(), Mapper.id() );
	}

	private List< MethodSignature > deriveMethodDefinitions(){
		List< MethodSignature > definitions = classNode.methods().stream()
				.map( MethodDefinition::signature )
				.map( s -> new MethodSignature(
						s.name().identifier(),
						s.parameters().stream().map( FormalMethodParameter::type )
								.map( this::typeExpressionToNode ).collect(	Collectors.toList() ),
						typeExpressionToNode( s.returnType() ) ) )
				.collect( Collectors.toList() );

		if( classNode.extendsClass() == null ){
			return definitions;
		}
		if( roleMap == null ){
			createMappings();
		}
		Template superClass = typeExpressionToNode( classNode.extendsClass() ).getTem();
		superClass.getMethodSigs().stream().map( s ->
				new MethodSignature( s.getName(),
				Mapper.map( s.getParameters(), this::mapType ),
				mapType( s.getReturnType() ) )
		).forEach( definitions::add );

		return definitions;
	}

	private List< VariableDNode > deriveFields(){
		List< VariableDNode > fields = classNode.fields().stream().map( f -> {
			TypeDNode type = typeExpressionToNode( f.typeExpression() );
			return new VariableDNode( f.name().identifier(), type.getRoles(), type );
		} ).collect( Collectors.toList());
		if( classNode.extendsClass() == null ){
			return fields;
		}
		if( roleMap == null ){
			createMappings();
		}

		Template superClass = typeExpressionToNode( classNode.extendsClass() ).getTem();
		superClass.getFields().stream().map( v -> {
			TypeDNode mappedType = mapType( v.getType() );
			return new VariableDNode( v.getName(), mappedType.getRoles(), mappedType );
		} ).forEach( fields::add );

		return fields;
	}

	private TypeDNode mapType( TypeDNode type ){
		if( type.getTem().isGeneric() ){
			TypeDNode replaceType = this.genericMap.get( type.getName() );
			return new TypeDNode( replaceType.getTem(), Mapper.map( type.getRoles(), this.roleMap::get ), replaceType.getTypeArguments() );
		}else{
			return type.copyWithMapping( this.roleMap );
		}
	}

	@Override
	public String getName() {
		return this.classNode.name().identifier();
	}

	@Override
	public List< VariableDNode > getFields(){
		if( this.fields == null ){
			this.fields = deriveFields();
		}
		return this.fields;
	}

	@Override
	public VariableDNode getField( String identifier ){
		for( VariableDNode field : getFields() ) {
			if( field.getName().equals( identifier ) ) {
				return field;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public List< MethodSignature > getMethodSigs(){
		prepareSymbols();
		if( this.methodDefinitions == null ){
			this.methodDefinitions = deriveMethodDefinitions();
		}
		return this.methodDefinitions;
	}

	@Override
	public List< FormalWorldParameter > worldParameters(){
		return classNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters(){
		return classNode.typeParameters();
	}
}
