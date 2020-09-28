package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Template {
	private final List< ImportDeclaration > importDeclarations;
	private final Package holdingPackage;
	private Map<String, Template > knownSymbols;

	Template( List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		this.importDeclarations = importDeclarations;
		this.holdingPackage = holdingPackage;
	}

	public static Template unknownTemplate() {
		return UnknownTemplate.unknownTemplate;
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

	public abstract String getName();

	public Template resolveIdentifier( String identifier ){
		if( knownSymbols == null ){
			populateKnownSymbols();
		}

		Template tem = knownSymbols.get( identifier );
		if( tem == null ){
			return Template.unknownTemplate();
		}
		return tem;
	}

	public abstract Field getField( String identifier );

	public abstract List< ? extends MethodDefinition > getMethodDefs();

	public abstract List< FormalWorldParameter > worldParameters();

	public abstract List< FormalTypeParameter > typeParameters();
}
