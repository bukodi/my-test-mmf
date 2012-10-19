package my.test.mmf.core.impl.jre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMPackage;

public class MLibraryImpl implements MLibrary {

	final static String PACKAGE_INFO_CLASS = "_PackageInfo_";

	private final List<MPackage> mpackageList;

	public MLibraryImpl(ClassLoader classLoader) {
		List<MPackage> mpackageList = new ArrayList<MPackage>();
		ServiceLoader<MPackage> serviceLoader = ServiceLoader.load(MPackage.class, classLoader);
		Iterator<MPackage> itService = serviceLoader.iterator(); 
		for( ;itService.hasNext(); ) {
			mpackageList.add( itService.next() );
		}
		this.mpackageList = Collections.unmodifiableList(mpackageList);
	}

	@Override
	public List<MPackage> listMPackages() {
		return mpackageList;
	}

}
