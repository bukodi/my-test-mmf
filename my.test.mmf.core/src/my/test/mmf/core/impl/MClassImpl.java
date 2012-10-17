package my.test.mmf.core.impl;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;

public class MClassImpl implements MClass {
	
	private ICompilationUnit jdtCu;
	
	public MClassImpl( ICompilationUnit jdtCu ) {
		this.jdtCu = jdtCu; 
	}

	@Override
	public String getName() {
		return jdtCu.getElementName();
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public MPackage getMPackage() {		
		return new MPackageImpl( ((IPackageFragment) jdtCu.getParent()).getCompilationUnit(MRootImpl.PACKAGE_INFO_CLASS + ".java") );
	}

	@Override
	public void setMPackage(MPackage containerPackage) {
	}

	@Override
	public void remove() {
	}

}
