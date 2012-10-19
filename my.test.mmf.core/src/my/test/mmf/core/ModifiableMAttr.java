package my.test.mmf.core;

public interface ModifiableMAttr extends MAttr {

	@Override
	ModifiableMClass getMClass();

	void setName(String name);

	void setMClass(ModifiableMClass ownerMClass);

	void delete();
}
