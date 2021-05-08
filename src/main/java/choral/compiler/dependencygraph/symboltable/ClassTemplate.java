package choral.compiler.dependencygraph.symboltable;

import choral.ast.ImportDeclaration;
import choral.ast.body.Class;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.Field;
import choral.ast.body.MethodDefinition;
import choral.compiler.dependencygraph.Mapper;
import choral.compiler.dependencygraph.dnodes.DType;
import choral.compiler.dependencygraph.role.FixedRole;
import choral.compiler.dependencygraph.role.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Template for a class
 */
public class ClassTemplate extends Template {
	private final Class classNode;
	private final List< Role > roles;

	ClassTemplate( Class classNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, classNode.typeParameters() );
		this.classNode = classNode;
		this.roles = Mapper.map( classNode.worldParameters(),
				w -> new FixedRole( w.toWorldArgument().name().identifier() ) );
	}

	@Override
	public List< DType > prepareSuperType(){

		List< DType > extensions = new ArrayList<>();

		if( classNode.extendsClass() != null ){
			extensions.add( typeExpressionToDType( classNode.extendsClass() ) );
		}

		if( classNode.implementsInterfaces().size() > 0 ){
			extensions.addAll( typeExpressionsToDTypes( classNode.implementsInterfaces() ) );
		}

		// multi roled objects does not extend Object,
		// if other extension are available, Object is retrieved through them too
		if( classNode.worldParameters().size() == 1 && extensions.isEmpty() ){
			// If nothing else is extended, Object is implicit
			Template objectTem = getHoldingPackage().getRoot().getPackage( PackageHandler.langPath )
					.getTemplate( "Object" );
			if( objectTem != this ) { // Object cannot extend itself
				extensions.add(
						new DType( objectTem,
								Collections.singletonList( worldParameters().get( 0 ) ),
								Collections.emptyList() ) );
			}
		}
		return extensions;
	}

	@Override
	public String getName() {
		return this.classNode.name().identifier();
	}

	@Override
	public List< Role > worldParameters(){
		return roles;
	}

	@Override
	public List< String > typeParameters(){
		return Mapper.map( classNode.typeParameters(), t -> t.name().identifier() );
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return classNode.methods();
	}

	@Override
	public List< ConstructorDefinition > constructorDefinitions() {
		return classNode.constructors();
	}

	@Override
	public List< Field > fields() {
		return classNode.fields();
	}
}
