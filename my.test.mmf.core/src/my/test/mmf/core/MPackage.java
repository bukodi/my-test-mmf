package my.test.mmf.core;

import java.util.List;

public interface MPackage  {

	String getName();

	void setName(String name);

	MLibrary getMLibrary();

	void delete(); 

	List<MClass> listMClasses(); 

	MClass createMClass( String name );

}
