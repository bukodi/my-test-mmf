package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface ModifiableMClass extends MClass {

	@Override
	ModifiableMPackage getMPackage();

	@Override
	@Nullable
	ModifiableMAttr getMAttribute(String name);

	@Override
	List<? extends MAttr> listMAttributes();

	void setName(String name);

	void setMPackage(ModifiableMPackage containerPackage);

	void delete();

	ModifiableMAttr createAttribute(String name);

}
