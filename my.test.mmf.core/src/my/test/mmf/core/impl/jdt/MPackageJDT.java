package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.MClass;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.ModifiableMLibrary;
import my.test.mmf.core.impl.jre.MLibraryImpl;
import my.test.mmf.core.impl.jre.MPackageImpl;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;

public class MPackageJDT implements ModifiableMPackage {

	private ICompilationUnit jdtPackageInfo;

	protected MPackageJDT(ICompilationUnit jdtPackageInfo) {
		this.jdtPackageInfo = jdtPackageInfo;
	}

	protected MPackageJDT(MLibraryJDT mlib, String pkgName) {
		this(mlib, pkgName, MPackage.PACKAGE_INFO_CLASS);
	}

	protected MPackageJDT(MLibraryJDT mlib, String name, String infoClassName) {
		ICompilationUnit workingCopy = null;
		try {
			/** First part: create source for package */
			IPackageFragment jdtPackage;
			IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) mlib.jdtLibraryInfo
					.getParent().getParent();

			IPackageFragment pkgTest = srcRoot.getPackageFragment(name);
			if (pkgTest != null && pkgTest.exists()) {
				if (pkgTest.getCompilationUnit(infoClassName + ".java")
						.exists())
					throw new MyRuntimeException("Package with name '" + name
							+ "' already exists.");
			}
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			jdtPackage = srcRoot.createPackageFragment(name, true,
					MyMonitor.currentMonitor());

			String libInfoFQN = mlib.jdtLibraryInfo.findPrimaryType()
					.getFullyQualifiedName();
			String content = "";
			content += "public class " + infoClassName + " extends "
					+ MPackageImpl.class.getSimpleName() + " {\n\n";
			content += "\tprivate final static " + infoClassName
					+ " INSTANCE = new " + infoClassName + "(" + libInfoFQN
					+ ".instance());\n";
			content += "\tpublic static " + infoClassName
					+ " instance() { return INSTANCE; }\n";
			content += "\tprivate " + infoClassName
					+ "(MLibraryImpl mlibrary) {super(mlibrary);}\n\n";
			content += "}";

			jdtPackageInfo = jdtPackage.createCompilationUnit(infoClassName
					+ ".java", content, true, null);
			workingCopy = jdtPackageInfo.getWorkingCopy(monitor);
			workingCopy.createPackageDeclaration(name, null);

			String source = ((IOpenable) workingCopy).getBuffer().getContents();

			Map<?, ?> options = EclipseUtils.getJdtCorePreferences(jdtPackage);

			// instantiate the default code formatter with the given options
			final CodeFormatter codeFormatter = ToolFactory
					.createCodeFormatter(options, ToolFactory.M_FORMAT_NEW);

			TextEdit edit = codeFormatter.format(
					CodeFormatter.K_COMPILATION_UNIT, source, 0,
					source.length(), 0, null);

			if (edit == null) {
				throw new MyRuntimeException("Can't format the source: "
						+ source);
			}

			workingCopy.applyTextEdit(edit, monitor);
			workingCopy.commitWorkingCopy(false, monitor);

		} catch (JavaModelException e) {
			throw new MyRuntimeException("Can not create '" + name
					+ "' package.", e);
		} finally {
			if (workingCopy != null)
				try {
					workingCopy.discardWorkingCopy();
				} catch (JavaModelException e) {
					throw new MyRuntimeException(e);
				}
		}
	}

	ICompilationUnit getJdtPackageInfo() {
		return jdtPackageInfo;
	}

	@Override
	public String getName() {
		return jdtPackageInfo.getParent().getElementName();
	}

	@Override
	public void setName(String newName) {
		try {
			String infoClassName = jdtPackageInfo.getElementName();
			IPackageFragment jdtPkg = (IPackageFragment) jdtPackageInfo
					.getParent();
			IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) jdtPkg
					.getParent();
			jdtPkg.rename(newName, false, MyMonitor.currentMonitor());
			jdtPkg = srcRoot.getPackageFragment(newName);
			jdtPackageInfo = jdtPkg
					.getCompilationUnit(infoClassName );			
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Rename " + this + " to " + newName, e);
		}
	}

	@Override
	public ModifiableMLibrary getMLibrary() {
		throw new UnsupportedOperationException("Currently not implemented"); // TODO
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
	@Nullable
	public MClass getMClass(String name) {
		IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
				.getParent();
		ICompilationUnit cu = jdtPackage.getCompilationUnit(name + ".java");
		return cu.exists() ? new MClassJDT(cu) : null;
	}

	@Override
	public List<ModifiableMClass> listMClasses() {
		List<ModifiableMClass> mclassList = new ArrayList<ModifiableMClass>();
		try {
			IPackageFragment jdtPackage = (IPackageFragment) jdtPackageInfo
					.getParent();
			for (ICompilationUnit cu : jdtPackage.getCompilationUnits()) {
				if ((MPackage.PACKAGE_INFO_CLASS + ".java").equals(cu
						.getElementName()))
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
		ICompilationUnit workingCopy = null;
		try {
			/** First part: create the new package */
			MClassJDT mclass = new MClassJDT(this, name);
			/**
			 * Second part: add newly created package source to the library
			 * source
			 */
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			WorkingCopyOwner wco = new WorkingCopyOwner() {
			};
			workingCopy = jdtPackageInfo.getWorkingCopy(monitor);
			workingCopy.createImport(MClass.class.getName(), null, monitor);
			IType jdtType = workingCopy.findPrimaryType();

			String fieldName = "_" + name + "_";
			String classFQN = mclass.getJDTCompilationUnit().findPrimaryType()
					.getFullyQualifiedName();
			String contents = "\tpublic final static "
					+ MClass.class.getSimpleName() + " " + fieldName
					+ " = INSTANCE.registerMClass( " + classFQN + ".class);\n";
			jdtType.createField(contents, null, false, monitor);

			workingCopy.commitWorkingCopy(false, monitor);
			return mclass;
		} catch (JavaModelException e) {
			throw new MyRuntimeException(
					"Can not create '" + name + "' class.", e);
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
