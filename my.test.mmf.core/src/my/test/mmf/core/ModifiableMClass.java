package my.test.mmf.core;

import java.util.List;

public interface ModifiableMClass extends MClass {
	
	@Override
	public ModifiableMPackage getMPackage();

	@Override
	List<? extends MAttr> listMAttributes();

	void setName(String name);

	public void setMPackage( ModifiableMPackage containerPackage );

	public void delete();

	ModifiableMAttr createAttribute(String name);

}
