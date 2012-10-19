package my.test.mmf.core;

import java.util.List;

public interface ModifiableMLibrary extends MLibrary {
	
	@Override
	List<? extends ModifiableMPackage> listMPackages();

	ModifiableMPackage createMPackage( String name );

}
