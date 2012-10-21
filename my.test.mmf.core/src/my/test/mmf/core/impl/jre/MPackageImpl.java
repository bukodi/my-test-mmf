package my.test.mmf.core.impl.jre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMPackage;

import org.eclipse.jdt.annotation.Nullable;

public abstract class MPackageImpl implements MPackage {

	private final MLibrary mlibrary;
	private final Map<String,MClass> mclassesByName;
	private final List<MClass> mclassesOrder;

	protected MPackageImpl(MLibrary mlibrary, Class<?> ... classes  ) {
		this.mlibrary = mlibrary;
		Map<String, MClass> mclassesByName = new HashMap<String, MClass>();
		List<MClass> mclassesOrder = new ArrayList<MClass>();
		for( Class<?> clazz : classes ) {
			MClassImpl mcls = new MClassImpl(this, clazz);
			mclassesByName.put( mcls.getName(), mcls );
			mclassesOrder.add( mcls );
		}
		this.mclassesByName = Collections.unmodifiableMap(mclassesByName);
		this.mclassesOrder = Collections.unmodifiableList(mclassesOrder);
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
		return mclassesOrder;
	}

	@Override
	public String toString() {
		return "(" + ModifiableMPackage.class.getSimpleName() + ")"
				+ getName();
	}
}
