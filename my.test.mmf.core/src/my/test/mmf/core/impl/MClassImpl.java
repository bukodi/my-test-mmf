package my.test.mmf.core.impl;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

public class MClassImpl implements MClass {

	private ICompilationUnit jdtCu;

	public MClassImpl( ICompilationUnit jdtCu ) {
		this.jdtCu = jdtCu;
	}

	@Override
	public String getName() {
		String name = jdtCu.getElementName();
		return name.substring(0, name.length() - ".java".length());
	}

	@Override
	public void setName(String name) {
		try {
			IPackageFragment jdtPkg = (IPackageFragment) jdtCu.getParent();
			jdtCu.rename(name + ".java", false, MyMonitor.currentMonitor());
			jdtCu = jdtPkg
					.getCompilationUnit(name + ".java");
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + name, e);
		}
	}

	@Override
	public MPackage getMPackage() {
		return new MPackageImpl( ((IPackageFragment) jdtCu.getParent()).getCompilationUnit(MRootImpl.PACKAGE_INFO_CLASS + ".java") );
	}

	@Override
	public void setMPackage(MPackage destMPackage) {
		try {
			String destPackageName = destMPackage.getName();
			IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) jdtCu.getParent().getParent();
			IPackageFragment jdtDestPackage = srcRoot.getPackageFragment(destPackageName);
			jdtCu.move(jdtDestPackage, null, null, false, MyMonitor.currentMonitor());
			jdtCu = jdtDestPackage.getCompilationUnit(jdtCu.getElementName());
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Move " + this + " to " + destMPackage, e);
		}
	}

	@Override
	public void remove() {
	}

	@Override
	public String toString() {
		return "(" + MClass.class.getSimpleName() + ")"
				+ jdtCu.getParent().getElementName() + "." + jdtCu.getElementName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jdtCu == null) ? 0 : jdtCu.hashCode());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		MClassImpl other = (MClassImpl) obj;
		if (jdtCu == null) {
			if (other.jdtCu != null)
				return false;
		} else if (!jdtCu.equals(other.jdtCu))
			return false;
		return true;
	}


}
