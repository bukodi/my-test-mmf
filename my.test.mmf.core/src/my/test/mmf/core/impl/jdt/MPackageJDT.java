package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.ModifiableMLibrary;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class MPackageJDT implements ModifiableMPackage {

	private ICompilationUnit jdtPackageInfo;

	protected MPackageJDT(ICompilationUnit jdtPackageInfo) {
		this.jdtPackageInfo = jdtPackageInfo;
	}

	@Override
	public String getName() {
		return jdtPackageInfo.getParent().getElementName();
	}

	@Override
	public void setName(String name) {
		try {
			IPackageFragment jdtPkg = (IPackageFragment) jdtPackageInfo
					.getParent();
			IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) jdtPkg
					.getParent();
			jdtPkg.rename(name, false, MyMonitor.currentMonitor());
			jdtPkg = srcRoot.getPackageFragment(name);
			jdtPackageInfo = jdtPkg
					.getCompilationUnit(MLibraryJDT.PACKAGE_INFO_CLASS + ".java");
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + name, e);
		}
	}

	@Override
	public ModifiableMLibrary getMLibrary() {
		return new MLibraryJDT((IPackageFragmentRoot) jdtPackageInfo.getParent());
	}

	@Override
	public void delete() {
		try {
			IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
					.getParent();
			jdtPackage.delete(true, MyMonitor.currentMonitor());
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public List<ModifiableMClass> listMClasses() {
		List<ModifiableMClass> mclassList = new ArrayList<ModifiableMClass>();
		try {
			IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
					.getParent();
			for (ICompilationUnit cu : jdtPackage.getCompilationUnits()) {
				if( (MLibraryJDT.PACKAGE_INFO_CLASS + ".java").equals( cu.getElementName()) )
					continue;
				mclassList.add(new MClassJDT(cu));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return mclassList;
	}

	@Override
	public ModifiableMClass createClass(String name) {
		return new MClassJDT(jdtPackageInfo, name);
	}

	@Override
	public String toString() {
		return "(" + ModifiableMPackage.class.getSimpleName() + ")"
				+ jdtPackageInfo.getParent().getElementName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jdtPackageInfo == null) ? 0 : jdtPackageInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		MPackageJDT other = (MPackageJDT) obj;
		if (jdtPackageInfo == null) {
			if (other.jdtPackageInfo != null)
				return false;
		} else if (!jdtPackageInfo.equals(other.jdtPackageInfo))
			return false;
		return true;
	}

}
