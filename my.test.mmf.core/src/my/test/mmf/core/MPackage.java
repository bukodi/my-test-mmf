package my.test.mmf.core;

import java.util.List;

public interface MPackage extends MPackageableElement {

	String getName();

	void setName(String name);

	List<MPackage> listSubpackages();

}
