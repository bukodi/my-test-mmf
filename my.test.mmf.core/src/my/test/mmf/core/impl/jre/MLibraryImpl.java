package my.test.mmf.core.impl.jre;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.annotation.Nullable;

public abstract class MLibraryImpl implements MLibrary {

	private final Map<String, MPackage> mpackagesByName;
	private final List<MPackage> mpackageOrder;
	
	@SafeVarargs
	protected MLibraryImpl(Class<? extends MPackageImpl>... classes) {
		Map<String, MPackage> mpackagesByName = new HashMap<String, MPackage>();
		List<MPackage> mpackageOrder = new ArrayList<MPackage>();
		for (Class<? extends MPackageImpl> mpkgClass : classes) {
			MPackageImpl mpkg;
			try {
				Constructor<?> constructor = null;
				for (Constructor<?> ctor : mpkgClass.getConstructors()) {
					if (ctor.getParameterTypes().length != 1)
						continue;
					if (!ctor.getParameterTypes()[0].isAssignableFrom(this
							.getClass()))
						continue;
					if (constructor != null)
						throw new MyRuntimeException("Ambiguous constructors"
								+ mpkgClass.getName());
					constructor = ctor;
				}
				if (constructor == null)
					throw new MyRuntimeException("No appropriate constructor"
							+ mpkgClass.getName());
				mpkg = (MPackageImpl) constructor.newInstance(this);
				mpackagesByName.put(mpkg.getName(), mpkg);
				mpackageOrder.add(mpkg);
			} catch (Exception e) {
				throw new MyRuntimeException(e);
			}
		}
		this.mpackagesByName = Collections.unmodifiableMap(mpackagesByName);
		this.mpackageOrder = Collections.unmodifiableList(mpackageOrder);
	}


	@Override
	@Nullable
	public MPackage getMPackage(String name) {
		return mpackagesByName.get(name);
	}

	@Override
	public List<MPackage> listMPackages() {
		return mpackageOrder;
	}

}
