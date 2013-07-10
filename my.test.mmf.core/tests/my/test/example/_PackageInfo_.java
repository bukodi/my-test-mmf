package my.test.example;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.impl.jre.MLibraryImpl;
import my.test.mmf.core.impl.jre.MPackageImpl;

public class _PackageInfo_ extends MPackageImpl {
	
	private final static MPackageImpl INSTANCE = new _PackageInfo_(_LibraryInfo_.instance());
	public static MPackage instance() { return INSTANCE; };
	private _PackageInfo_(MLibraryImpl mlibrary) {super(mlibrary);}
	
	
	public final static MClass _INamedElement_ = INSTANCE.registerMClass( INamedElement.class );
	public final static MClass _IEntity_ = INSTANCE.registerMClass( IEntity.class );
	
}
