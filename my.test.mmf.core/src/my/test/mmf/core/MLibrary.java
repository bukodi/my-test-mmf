package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface MLibrary {

	@Nullable
	MPackage getMPackage(String name);

	List<? extends MPackage> listMPackages();

}
