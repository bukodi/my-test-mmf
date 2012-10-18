package my.test.mmf.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;

public class MPackageImpl implements MPackage {

	private ICompilationUnit jdtPackageInfo;

	protected MPackageImpl(ICompilationUnit jdtPackageInfo) {
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
					.getCompilationUnit(MRootImpl.PACKAGE_INFO_CLASS + ".java");
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + name, e);
		}
	}

	@Override
	public MRoot getRoot() {
		return new MRootImpl((IPackageFragmentRoot) jdtPackageInfo.getParent());
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
	public List<MClass> listMClasses() {
		List<MClass> mclassList = new ArrayList<MClass>();
		try {
			IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
					.getParent();
			for (ICompilationUnit cu : jdtPackage.getCompilationUnits()) {
				if( (MRootImpl.PACKAGE_INFO_CLASS + ".java").equals( cu.getElementName()) )
					continue;
				mclassList.add(new MClassImpl(cu));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return mclassList;
	}

	@Override
	public MClass createMClass(String name) {
		return new MClassImpl(jdtPackageInfo, name);
	}

	@Override
	public String toString() {
		return "(" + MPackage.class.getSimpleName() + ")"
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
		MPackageImpl other = (MPackageImpl) obj;
		if (jdtPackageInfo == null) {
			if (other.jdtPackageInfo != null)
				return false;
		} else if (!jdtPackageInfo.equals(other.jdtPackageInfo))
			return false;
		return true;
	}

}
