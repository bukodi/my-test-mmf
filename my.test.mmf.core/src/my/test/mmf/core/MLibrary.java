package my.test.mmf.core;

import java.util.List;

public interface MLibrary {
	
	List<MPackage> listMPackages();
	
	MPackage createMPackage( String name );

}
