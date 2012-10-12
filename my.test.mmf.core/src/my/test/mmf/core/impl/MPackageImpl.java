package my.test.mmf.core.impl;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.MPackage;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class MPackageImpl implements MPackage, IJDTWrapper {

	private IPackageFragment jdtPackage;

	@Override
	public IJavaElement getJavaElement() {
		return jdtPackage;
	}

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
			jdtPackage.rename(name, true, MyMonitor.currentMonitor());
			if( parent instanceof IPackageFragment ) {
				//jdtPackage = ((IPackageFragment)parent).getPackageFragment(name);	v			
			} else if( parent instanceof IPackageFragmentRoot ) {
				jdtPackage = ((IPackageFragmentRoot)parent).getPackageFragment(name);				
			} else {
				throw new MyRuntimeException( "Invalid parent type: " + parent );
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + name, e);
		}
	}

	@Override
	public List<MPackage> listSubpackages() {
		List<MPackage> subPkgList = new ArrayList<MPackage>();
		try {
			for (IJavaElement child : jdtPackage.getChildren()) {
				if (child instanceof IPackageFragment)
					subPkgList.add(new MPackageImpl((IPackageFragment) child));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return subPkgList;
	} 

	@Override
	public @Nullable
	MPackage getPackage() {
		if (jdtPackage.getParent() instanceof IPackageFragment)
			if (jdtPackage.getParent() != null)
				return new MPackageImpl(
						(IPackageFragment) jdtPackage.getParent());
		return null;
	}

	@Override
	public void setPackage(MPackage containerPackage) {
		IPackageFragment mpkgImpl = (IPackageFragment) (((IJDTWrapper) containerPackage)
				.getJavaElement());
		try {
			jdtPackage.move(mpkgImpl, null, null, false,
					MyMonitor.currentMonitor());
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public void remove() {
		try {
			jdtPackage.delete(true, MyMonitor.currentMonitor());
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

}
