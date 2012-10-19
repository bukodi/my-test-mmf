package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface ModifiableMPackage extends MPackage {

	@Override
	ModifiableMLibrary getMLibrary();

	@Override @Nullable MClass getMClass( String name );

	@Override
	List<? extends ModifiableMClass> listMClasses();

	void setName(String name);

	void delete();

	ModifiableMClass createClass( String name );

}
