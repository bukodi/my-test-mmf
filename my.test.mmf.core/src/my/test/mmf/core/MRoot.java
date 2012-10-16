package my.test.mmf.core;

import java.util.List;

public interface MRoot {
	
	List<MPackage> listMPackages();
	
	MPackage createMPackage( String name );

}
