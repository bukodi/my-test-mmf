package de.dainel.cleanqualifiedtypes;

import java.util.List;

import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.dainel.cleanqualifiedtypes.util.ManipulatorHelper;

/**
 * @author Michael Ernst
 */
public class CleanQualifiedTypes {

	public void run(ICompilationUnit lwUnit) {
		try {
			if (!lwUnit.isStructureKnown()) 
				throw new MyRuntimeException("The given compilation unit contains compiling errors, so qualified types could not be added to the imports.");
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Could not handle resource.", e);
		}
		CompilationUnit unit = parse(lwUnit);
		run(unit);
	}

	public void run(char[] source) {
		CompilationUnit unit = parse(source);
		run(unit);
	}

	private void run(CompilationUnit unit) {
		QualifiedTypeDetector detector = new QualifiedTypeDetector();
		unit.accept(detector);
		boolean isChanged = rewrite(unit, detector.getTypeManagers());
		if (isChanged) {
			save(unit);
		}
	}

	protected boolean rewrite(CompilationUnit unit,
			List<QualifiedTypeBindingManager> typeManagers) {
		QualifiedTypeRewriter qualifiedTypeRewriter = createRewriter(unit,
				typeManagers);
		return qualifiedTypeRewriter.rewrite();
	}

	protected QualifiedTypeRewriter createRewriter(CompilationUnit unit,
			List<QualifiedTypeBindingManager> typeManagers) {
		return new QualifiedTypeRewriter(unit, typeManagers);
	}

	protected void save(CompilationUnit unit) {
		try {
			// write changes back to Java source code
			ManipulatorHelper.saveDirectlyModifiedUnit(unit);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses source code.
	 * 
	 * @param lwUnit
	 *            the Java Model handle for the compilation unit
	 * @return the root AST node of the parsed source
	 */
	protected CompilationUnit parse(char[] source) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source); // set source
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}

	/**
	 * Parses source code.
	 * 
	 * @param lwUnit
	 *            the Java Model handle for the compilation unit
	 * @return the root AST node of the parsed source
	 */
	protected CompilationUnit parse(ICompilationUnit lwUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(lwUnit); // set source
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}

}
