package my.test.mmf.core;

import java.util.List;

public interface MClass {

	String getName();

	public MPackage getMPackage();

	List<? extends MAttr> listMAttributes();

}
