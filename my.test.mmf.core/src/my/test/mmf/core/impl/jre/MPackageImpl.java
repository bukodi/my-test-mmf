package my.test.mmf.core.impl.jre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMPackage;

import org.eclipse.jdt.annotation.Nullable;

public abstract class MPackageImpl implements MPackage {

	private final MLibrary mlibrary;
	private final LinkedHashMap<String,MClass> mclassesByName;

	protected MPackageImpl(MLibrary mlibrary) {
		this.mlibrary = mlibrary;
		this.mclassesByName = new LinkedHashMap<String, MClass>();
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
	@Nullable
	public MClass getMClass(String name) {
		return mclassesByName.get(name);
	}

	@Override
	public List<MClass> listMClasses() {
		// TODO: replace this with a ForwardingList
		List<MClass> mclasses = new ArrayList<MClass>();
		for( MClass mclass : mclassesByName.values() ) 
			mclasses.add(mclass);
		return mclasses;
	}

	@Override
	public String toString() {
		return "(" + ModifiableMPackage.class.getSimpleName() + ")"
				+ getName();
	}
	
	public MClassImpl registerMClass( Class<?> javaClass ) {
		MClassImpl mclass = new MClassImpl(this, javaClass);
		mclassesByName.put(mclass.getName(), mclass);
		return mclass;
	}
}
