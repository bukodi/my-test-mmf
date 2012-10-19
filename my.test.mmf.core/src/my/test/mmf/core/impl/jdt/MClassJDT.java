package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.ModifiableMAttr;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;

public class MClassJDT implements ModifiableMClass {

	private ICompilationUnit jdtCu;

	public MClassJDT( ICompilationUnit jdtCu ) {
		this.jdtCu = jdtCu;
	}

	public MClassJDT( ICompilationUnit jdtPackageInfo, String name ) {
		String fullname = "???."+ name;
		ICompilationUnit workingCopy = null;
		try {
			IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
					.getParent();
			fullname = jdtPackage.getElementName() +"."+ name;
			if (jdtPackage.getCompilationUnit(name + ".java").exists())
				throw new MyRuntimeException("Class with name '" + fullname
						+ "' already exists.");
			IProgressMonitor monitor = MyMonitor.currentMonitor();

			String content = "public class " + name + " {}";
			ICompilationUnit cu = jdtPackage.createCompilationUnit(
					name + ".java", content, true, null);
			workingCopy = cu.getWorkingCopy(monitor);
			workingCopy.createPackageDeclaration(jdtPackage.getElementName(), monitor);

			String source = ((IOpenable) workingCopy).getBuffer().getContents();

			Map<?, ?> options = EclipseUtils.getJdtCorePreferences(jdtPackage);

			// instantiate the default code formatter with the given options
			final CodeFormatter codeFormatter = ToolFactory
					.createCodeFormatter(options, ToolFactory.M_FORMAT_NEW);

			TextEdit edit = codeFormatter.format(
					CodeFormatter.K_COMPILATION_UNIT, source, 0,
					source.length(), 0, null);

			if (edit == null) {
				throw new MyRuntimeException("Can't format the source: " + source);
			}

			workingCopy.applyTextEdit(edit, monitor);
			workingCopy.commitWorkingCopy(false, monitor);
			workingCopy.discardWorkingCopy();

			jdtCu = jdtPackage.getCompilationUnit(name + ".java");
		} catch (JavaModelException e) {
			throw new MyRuntimeException(
					"Can not create '" + fullname + "' class.", e);
		} finally {
			if (workingCopy != null)
				try {
					workingCopy.discardWorkingCopy();
				} catch (JavaModelException e) {
					throw new MyRuntimeException(e);
				}
		}
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
	public ModifiableMPackage getMPackage() {
		return new MPackageJDT( ((IPackageFragment) jdtCu.getParent()).getCompilationUnit(MLibraryJDT.PACKAGE_INFO_CLASS + ".java") );
	}

	@Override
	public void setMPackage(ModifiableMPackage destMPackage) {
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
	public ModifiableMAttr createAttribute(String name) {
		return new MAttrJDT(jdtCu, name);
	}

	@Override
	public List<ModifiableMAttr> listMAttributes() {
		List<ModifiableMAttr> mattrList = new ArrayList<ModifiableMAttr>();
		try {
			IType jdtType = jdtCu.findPrimaryType();
			for (IField jdtField : jdtType.getFields()) {
				if( ! Flags.isStatic( jdtField.getFlags() )  )
					continue;
				mattrList.add(new MAttrJDT(jdtField));
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return mattrList;
	}

	@Override
	public void delete() {
		try {
			jdtCu.delete(false, MyMonitor.currentMonitor());
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "(" + ModifiableMClass.class.getSimpleName() + ")"
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
		MClassJDT other = (MClassJDT) obj;
		if (jdtCu == null) {
			if (other.jdtCu != null)
				return false;
		} else if (!jdtCu.equals(other.jdtCu))
			return false;
		return true;
	}
}
