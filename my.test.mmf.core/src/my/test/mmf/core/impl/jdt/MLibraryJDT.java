package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.test.example._LibraryInfo_;
import my.test.example._PackageInfo_;
import my.test.mmf.core.MLibrary;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMLibrary;
import my.test.mmf.core.ModifiableMPackage;
import my.test.mmf.core.impl.jre.MLibraryImpl;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class MLibraryJDT implements ModifiableMLibrary {

	final ICompilationUnit jdtLibraryInfo;

	public MLibraryJDT(IPackageFragment jdtPkg) {
		this(jdtPkg, MLibrary.LIBRARY_INFO_CLASS);
	}

	public MLibraryJDT(IPackageFragment jdtPkg, String infoClassName) {
		ICompilationUnit workingCopy = null;
		try {
			if (jdtPkg != null && jdtPkg.exists()) {
				if (jdtPkg.getCompilationUnit(infoClassName + ".java").exists())
					throw new MyRuntimeException("Library with name '"
							+ jdtPkg.getElementName() + "' already exists.");
			}
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			if (!jdtPkg.exists()) {
				jdtPkg = ((IPackageFragmentRoot) jdtPkg.getParent())
						.createPackageFragment("", true,
								MyMonitor.currentMonitor());
			}

			String content = "";
			content += "public class " + infoClassName + " extends "
					+ MLibraryImpl.class.getSimpleName() + " {\n\n";
			content += "\tprivate final static " + infoClassName
					+ " INSTANCE = new " + infoClassName + "();\n";
			content += "\tpublic static " + infoClassName
					+ " instance() { return INSTANCE; }\n";
			content += "\tprivate " + infoClassName + "() {}\n\n";
			content += "}";

			ICompilationUnit cu = jdtPkg.createCompilationUnit(infoClassName
					+ ".java", content, true, monitor);
			workingCopy = cu.getWorkingCopy(monitor);
			workingCopy.createPackageDeclaration(jdtPkg.getElementName(),
					monitor);
			workingCopy.createImport(MLibraryImpl.class.getName(), null,
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

			jdtLibraryInfo = jdtPkg.getCompilationUnit(infoClassName + ".java");
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
		String fieldName = name.replace('.', '_');
		IField pkgField = jdtLibraryInfo.findPrimaryType().getField(
				fieldName);
		return getMPackage(pkgField);
	}

	private @Nullable
	MPackageJDT getMPackage(IField pkgField) {
		try {
			//String src = pkgField.getSource();
			int flags = pkgField.getFlags();
			if( !(Flags.isPublic(flags) && Flags.isStatic(flags) && Flags.isFinal(flags) ) ) 
				return null;
			String typeSign = pkgField.getTypeSignature();
			String fqn = Signature.toString(typeSign);
			IJavaProject jprj = jdtLibraryInfo.getJavaProject();
			IType fieldType = jprj.findType(fqn);
			if( fieldType == null || !fieldType.exists() )
				return null;
			ICompilationUnit fieldCu = fieldType.getCompilationUnit();
			if (fieldCu == null || !fieldCu.exists())
				return null;
			return new MPackageJDT(fieldCu);
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
	}

	@Override
	public List<? extends ModifiableMPackage> listMPackages() {
		List<ModifiableMPackage> topLevelPackages = new ArrayList<ModifiableMPackage>();
		try {
			for (IField field : jdtLibraryInfo.findPrimaryType().getFields()) {
				MPackageJDT mPkg = getMPackage(field);
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
			/** First part: create the new package */
			MPackageJDT mpkg = new MPackageJDT(this, name);
			/**
			 * Second part: add newly created package source to the library
			 * source
			 */
			{
				IProgressMonitor monitor = MyMonitor.currentMonitor();
				WorkingCopyOwner wco = new WorkingCopyOwner() {
				};
				workingCopy = jdtLibraryInfo.getWorkingCopy(monitor);
				workingCopy.createImport(MPackage.class.getName(), null,
						monitor);
				IType jdtType = workingCopy.findPrimaryType();

				String fieldName = name.replace('.', '_');
				String pkgInfoFQN = mpkg.getJdtPackageInfo().findPrimaryType()
						.getFullyQualifiedName();
				String contents = "\tpublic final static " + pkgInfoFQN + " "
						+ fieldName + " = INSTANCE.registerMPackage( "
						+ pkgInfoFQN + ".class);\n";
				jdtType.createField(contents, null, false, monitor);

				workingCopy.commitWorkingCopy(false, monitor);
			}

			return mpkg;
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
