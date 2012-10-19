package my.test.mmf.core;

import java.util.List;

public interface MPackage {

	String getName();

	MLibrary getMLibrary();

	List<? extends MClass> listMClasses();

}
