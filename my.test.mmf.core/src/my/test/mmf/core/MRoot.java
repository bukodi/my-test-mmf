package my.test.mmf.core;

import java.util.List;

public interface MRoot {
	
	List<MPackage> getTopLevelPackages();
	
	MPackage createTopLevelPackage( String name );
}
