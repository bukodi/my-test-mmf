package my.test.mmf.core.impl;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class MRootImpl implements MRoot {
	
	private final IPackageFragmentRoot jdtSourceRoot; 

	public MRootImpl( IPackageFragmentRoot jdtSourceRoot ) {
		this.jdtSourceRoot = jdtSourceRoot;
	}

	@Override
	public List<MPackage> getTopLevelPackages() {
		List<MPackage> topLevelPackages = new ArrayList<MPackage>();
		try {
			for( IJavaElement child : jdtSourceRoot.getChildren() ) {
				if( ! (child instanceof IPackageFragment ))
					continue;
				String name = child.getElementName();
				if( name == null || name.length() == 0 || name.contains("."))
					continue;
				topLevelPackages.add(new MPackageImpl((IPackageFragment) child));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		
		return topLevelPackages;
	}

	@Override
	public MPackage createTopLevelPackage(String name) {
		try {
			IPackageFragment jdNewPkg = jdtSourceRoot.createPackageFragment(name, true, MyMonitor.currentMonitor());
			return new MPackageImpl(jdNewPkg);
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}
}
