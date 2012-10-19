package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.mmf.core.ModifiableMLibrary;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
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

	final static String PACKAGE_INFO_CLASS = "_PackageInfo_";

	private final IPackageFragmentRoot jdtSourceRoot;

	public MLibraryJDT(IPackageFragmentRoot jdtSourceRoot) {
		this.jdtSourceRoot = jdtSourceRoot;
	}

	@Override
	public List<? extends ModifiableMPackage> listMPackages() {
		List<ModifiableMPackage> topLevelPackages = new ArrayList<ModifiableMPackage>();
		try {
			for (IJavaElement child : jdtSourceRoot.getChildren()) {
				if (!(child instanceof IPackageFragment))
					continue;
				IPackageFragment jdtPkg = (IPackageFragment) child;
				String name = child.getElementName();
				if (name == null || name.length() == 0)
					continue; // Skip the default package
				ICompilationUnit jdtPackageInfo = jdtPkg
						.getCompilationUnit(PACKAGE_INFO_CLASS + ".java");
				if (!jdtPackageInfo.exists())
					continue;
				topLevelPackages.add(new MPackageJDT(jdtPackageInfo));
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
			IPackageFragment pkgTest = jdtSourceRoot.getPackageFragment(name);
			if (pkgTest != null && pkgTest.exists()) {
				if (pkgTest.getCompilationUnit(PACKAGE_INFO_CLASS + ".java")
						.exists())
					throw new MyRuntimeException("Package with name '" + name
							+ "' already exists.");
			}
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			IPackageFragment jdtPackage = jdtSourceRoot.createPackageFragment(
					name, true, MyMonitor.currentMonitor());

			String content = "public class " + PACKAGE_INFO_CLASS + " {}";
			ICompilationUnit cu = jdtPackage.createCompilationUnit(
					PACKAGE_INFO_CLASS + ".java", content, true, null);
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
					jdtPackage.getCompilationUnit(PACKAGE_INFO_CLASS + ".java"));
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
