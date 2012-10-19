package my.test.mmf.core.impl.jre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;

import org.eclipse.jdt.annotation.Nullable;

public class MLibraryImpl implements MLibrary {

	final static String PACKAGE_INFO_CLASS = "_PackageInfo_";

	private final Map<String, MPackage> mpackagesByName;
	private final List<MPackage> mpackageOrder;

	public MLibraryImpl(ClassLoader classLoader) {
		Map<String, MPackage> mpackagesByName = new HashMap<String, MPackage>();
		List<MPackage> mpackageOrder = new ArrayList<MPackage>();
		ServiceLoader<MPackage> serviceLoader = ServiceLoader.load(MPackage.class, classLoader);
		Iterator<MPackage> itService = serviceLoader.iterator();
		for( ;itService.hasNext(); ) {
			MPackage mpkg = itService.next();
			mpackagesByName.put( mpkg.getName(), mpkg  );
			mpackageOrder.add(mpkg);
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
