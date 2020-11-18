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
		if( classNode.extendsClass() != null ){
			return Collections.singletonList( typeExpressionToDType( classNode.extendsClass() ) );
		}

		if( classNode.worldParameters().size() == 1 ){
			// If nothing else is extended, Object is implicit
			Template objectTem = getHoldingPackage().getRoot().getPackage( PackageHandler.langPath )
					.getTemplate( "Object" );
			if( objectTem != this ) { // Object cannot extend itself
				return Collections.singletonList(
						new DType( objectTem,
								Collections.singletonList( worldParameters().get( 0 ) ),
								Collections.emptyList() ) );
			}
		}

		return Collections.emptyList();
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
