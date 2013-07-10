package my.test.example;

import my.test.mmf.core.MPackage;
import my.test.mmf.core.impl.jre.MLibraryImpl;

public class _LibraryInfo_ extends MLibraryImpl {
	
	private final static _LibraryInfo_ INSTANCE = new _LibraryInfo_();
	
	public static _LibraryInfo_ instance() { return INSTANCE; }
	
	private _LibraryInfo_() {}

	public final static _PackageInfo_ _default_package_ = INSTANCE.registerMPackage(_PackageInfo_.class);
}
