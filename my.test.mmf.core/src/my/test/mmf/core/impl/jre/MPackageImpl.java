package my.test.mmf.core.impl.jre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.ModifiableMPackage;

public class MPackageImpl implements MPackage {

	private final MLibrary mlibrary;
	private final List<MClass> mclasses;

	protected MPackageImpl( Class<?> ... classes  ) {
		this.mlibrary = null;
		List<MClass> mclasses = new ArrayList<MClass>();
		for( Class<?> clazz : classes ) {
			mclasses.add( new MClassImpl(this, clazz));
		}
		this.mclasses = Collections.unmodifiableList(mclasses);
	}

	@Override
	public String getName() {
		return getClass().getPackage().getName();
	}

	@Override
	public MLibrary getMLibrary() {
		return mlibrary;
	}

	@Override
	public List<MClass> listMClasses() {
		return mclasses;
	}

	@Override
	public String toString() {
		return "(" + ModifiableMPackage.class.getSimpleName() + ")"
				+ getName();
	}
}
