package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface MLibrary {

	final static String LIBRARY_INFO_CLASS = "_LibraryInfo_";

	@Nullable
	MPackage getMPackage(String name);

	List<? extends MPackage> listMPackages();

}
