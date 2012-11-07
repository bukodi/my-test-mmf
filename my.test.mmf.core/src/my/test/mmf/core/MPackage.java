package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface MPackage {
	
	final static String PACKAGE_INFO_CLASS = "_PackageInfo_";

	String getName();

	MLibrary getMLibrary();

	@Nullable MClass getMClass( String name );

	List<? extends MClass> listMClasses();

}
