package my.test.mmf.core;

public interface MAttr {

	String getName();

	void setName(String name);

	MClass getMClass();

	void setMClass(MClass ownerMClass);

	void delete();
}
