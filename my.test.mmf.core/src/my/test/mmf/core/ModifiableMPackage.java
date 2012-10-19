package my.test.mmf.core;

import java.util.List;

public interface ModifiableMPackage extends MPackage {

	@Override
	ModifiableMLibrary getMLibrary();

	@Override
	List<? extends ModifiableMClass> listMClasses();

	void setName(String name);

	void delete(); 

	ModifiableMClass createClass( String name );

}
