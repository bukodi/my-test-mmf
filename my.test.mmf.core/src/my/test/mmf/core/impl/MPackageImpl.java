package my.test.mmf.core.impl;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class MPackageImpl implements MPackage {

	private IPackageFragment jdtPackage;

	protected MPackageImpl(IPackageFragment jdtPackage) {
		this.jdtPackage = jdtPackage;
	}

	@Override
	public String getName() {
		return jdtPackage.getElementName();
	}

	@Override
	public void setName(String name) {
		try {
			IJavaElement parent = jdtPackage.getParent();
			jdtPackage.rename(name, false, MyMonitor.currentMonitor());
			if (parent instanceof IPackageFragmentRoot) {
				jdtPackage = ((IPackageFragmentRoot) parent)
						.getPackageFragment(name);
			} else {
				throw new MyRuntimeException("Invalid parent type: " + parent);
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + name, e);
		}
	}

	@Override
	public MRoot getRoot() {
		return new MRootImpl((IPackageFragmentRoot) jdtPackage.getParent());
	}

	@Override
	public void remove() {
		try {
			jdtPackage.delete(true, MyMonitor.currentMonitor());
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public List<MClass> listMClasses() {
		List<MClass> mclassList = new ArrayList<MClass>(); 
		try {
			for( ICompilationUnit cu :jdtPackage.getCompilationUnits() ) {
				mclassList.add( new MClassImpl(cu));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return mclassList;
	}

	@Override
	public MClass createMClass(String name) {
		String contents = "";
		try {
			return new MClassImpl( jdtPackage.createCompilationUnit(name, contents , false, MyMonitor.currentMonitor()) );
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return MPackage.class.getSimpleName() + " (" + jdtPackage.getElementName() + ")";
	}

	
}
