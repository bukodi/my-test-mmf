package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMLibrary;
import my.test.mmf.core.ModifiableMPackage;
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

public class MLibraryJDT implements ModifiableMLibrary {

	private final ICompilationUnit jdtLibraryInfo;

	public MLibraryJDT(IPackageFragment jdtPkg) {
		ICompilationUnit workingCopy = null;
		try {
			if (jdtPkg != null && jdtPkg.exists()) {
				if (jdtPkg.getCompilationUnit(
						MLibrary.LIBRARY_INFO_CLASS + ".java").exists())
					throw new MyRuntimeException("Library with name '"
							+ jdtPkg.getElementName() + "' already exists.");
			}
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			if (!jdtPkg.exists()) {
				jdtPkg = ((IPackageFragmentRoot) jdtPkg.getParent())
						.createPackageFragment("", true,
								MyMonitor.currentMonitor());
			}

			String content = "public class " + MLibrary.LIBRARY_INFO_CLASS
					+ " extends " + MLibrary.class.getName() + " {}";
			ICompilationUnit cu = jdtPkg.createCompilationUnit(
					MLibrary.LIBRARY_INFO_CLASS + ".java", content, true,
					monitor);
			workingCopy = cu.getWorkingCopy(monitor);
			workingCopy.createPackageDeclaration(jdtPkg.getElementName(),
					monitor);

			String source = ((IOpenable) workingCopy).getBuffer().getContents();

			Map<?, ?> options = EclipseUtils.getJdtCorePreferences(jdtPkg);

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

			jdtLibraryInfo = jdtPkg
					.getCompilationUnit(MLibrary.LIBRARY_INFO_CLASS + ".java");
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Can not create '"
					+ jdtPkg.getElementName() + "' library.", e);
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
	public @Nullable
	ModifiableMPackage getMPackage(String name) {
		IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) jdtLibraryInfo
				.getParent().getParent();
		IPackageFragment jdtPkg = srcRoot.getPackageFragment(name);
		if (jdtPkg == null || !jdtPkg.exists())
			return null;
		return getMPackage(jdtPkg);
	}

	private @Nullable
	MPackageJDT getMPackage(IPackageFragment jdtPkg) {
		String name = jdtPkg.getElementName();
		if (name == null || name.length() == 0)
			return null; // Skip the default package
		ICompilationUnit jdtPackageInfo = jdtPkg
				.getCompilationUnit(MPackage.PACKAGE_INFO_CLASS + ".java");
		if (!jdtPackageInfo.exists())
			return null;
		return new MPackageJDT(jdtPackageInfo);
	}

	@Override
	public List<? extends ModifiableMPackage> listMPackages() {
		List<ModifiableMPackage> topLevelPackages = new ArrayList<ModifiableMPackage>();
		try {
			for (IJavaElement child : jdtLibraryInfo.getChildren()) {
				if (!(child instanceof IPackageFragment))
					continue;
				IPackageFragment jdtPkg = (IPackageFragment) child;
				MPackageJDT mPkg = getMPackage(jdtPkg);
				if (mPkg != null)
					topLevelPackages.add(mPkg);
			}
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}

		return topLevelPackages;
	}

	@Override
	public ModifiableMPackage createMPackage(String name) {
		ICompilationUnit workingCopy = null;
		try {
			IPackageFragmentRoot srcRoot = (IPackageFragmentRoot) jdtLibraryInfo
					.getParent().getParent();

			IPackageFragment pkgTest = srcRoot.getPackageFragment(name);
			if (pkgTest != null && pkgTest.exists()) {
				if (pkgTest.getCompilationUnit(
						MPackage.PACKAGE_INFO_CLASS + ".java").exists())
					throw new MyRuntimeException("Package with name '" + name
							+ "' already exists.");
			}
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			IPackageFragment jdtPackage = srcRoot.createPackageFragment(
					name, true, MyMonitor.currentMonitor());

			String content = "public class " + MPackage.PACKAGE_INFO_CLASS
					+ " {}";
			ICompilationUnit cu = jdtPackage.createCompilationUnit(
					MPackage.PACKAGE_INFO_CLASS + ".java", content, true, null);
			workingCopy = cu.getWorkingCopy(monitor);
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

			return new MPackageJDT(
					jdtPackage.getCompilationUnit(MPackage.PACKAGE_INFO_CLASS
							+ ".java"));
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
}
