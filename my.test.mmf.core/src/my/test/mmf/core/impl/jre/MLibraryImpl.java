package my.test.mmf.core.impl.jre;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.annotation.Nullable;

public abstract class MLibraryImpl implements MLibrary {

	private final LinkedHashMap<String, MPackage> mpackagesByName;
	
	protected MLibraryImpl() {
		mpackagesByName = new LinkedHashMap<String, MPackage>();
	}


	@Override
	@Nullable
	public MPackage getMPackage(String name) {
		return mpackagesByName.get(name);
	}

	@Override
	public List<MPackage> listMPackages() {
		// TODO: replace this with a ForwardingList
		List<MPackage> mpackages = new ArrayList<MPackage>();
		for( MPackage mclass : mpackagesByName.values() ) 
			mpackages.add(mclass);
		return mpackages;
	}

	public <T extends MPackageImpl> T registerMPackage( Class<T> mpkgClass) {
		try {
			Method instanceMethod = mpkgClass.getMethod("instance", new Class[]{});
			if( instanceMethod == null || !Modifier.isStatic( instanceMethod.getModifiers() ) || !Modifier.isPublic(instanceMethod.getModifiers()))
				throw new MyRuntimeException("Missing method: public static " + MPackageImpl.class.getSimpleName() + " instance() from " 
						+ mpkgClass.getName() + ".");
			@SuppressWarnings("unchecked")
			T mpkg = (T) instanceMethod.invoke(null, new Object[]{});
			mpackagesByName.put(mpkg.getName(), mpkg);
			return mpkg;
		} catch (Exception e) {
			throw new MyRuntimeException(e);
		}
	}
}
