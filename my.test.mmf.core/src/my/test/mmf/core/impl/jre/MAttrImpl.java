package my.test.mmf.core.impl.jre;

import my.test.mmf.core.MAttr;
import my.test.mmf.core.MClass;
import my.test.mmf.core.ModifiableMAttr;

public class MAttrImpl implements MAttr {

	private final MClassImpl mclass;
	private final String name;
	
	public MAttrImpl(MClassImpl mclass, String name ) {
		this.mclass = mclass;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MClass getMClass() {
		return mclass;
	}

	@Override
	public String toString() {
		return "(" + ModifiableMAttr.class.getSimpleName() + ")"
				+ name; // TODO
	}

}
