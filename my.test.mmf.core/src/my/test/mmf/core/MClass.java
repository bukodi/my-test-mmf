package my.test.mmf.core;

import java.util.List;

public interface MClass {

	String getName();

	void setName(String name);

	public MPackage getMPackage();

	public void setMPackage( MPackage containerPackage );

	public void delete();

	MAttr createMAttribute(String name);

	List<MAttr> listMAttributes();
}
