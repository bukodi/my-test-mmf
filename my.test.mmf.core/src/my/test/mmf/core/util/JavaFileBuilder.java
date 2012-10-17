package my.test.mmf.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Az osztály segítségével Java forrásokat hozhatunk létre.
 * 
 * 
 * @author lbukodi
 * 
 */
public class JavaFileBuilder {

	public static enum Sections {
		HEADER, VARIABLES, METHODS;
	}

	private final Set<String> imports = new HashSet<String>();
	private String packageName;
	private final EnumMap<Sections, StringWriter> sw = new EnumMap<JavaFileBuilder.Sections, StringWriter>(
	        Sections.class);
	private final EnumMap<Sections, PrintWriter> buff = new EnumMap<JavaFileBuilder.Sections, PrintWriter>(
	        Sections.class);
	private final ICompilationUnit existingSource;

	/**
	 * A megadott source folder-ben keresi a megadott nevû osztály forrását. Ha
	 * nem létezik, akkor létrehoz egy alapértelmezett forrást. Ha a forrás már
	 * létezik, akkor a {@link #saveIfModified(IProgressMonitor)}
	 * összehasonlítja a generátumot vele.
	 * 
	 * @param srcRoot
	 * @param fqn
	 */
	public JavaFileBuilder(IPackageFragmentRoot srcRoot, String fqn) {
		packageName = fqn.substring(0, fqn.lastIndexOf('.'));
		String className = fqn.substring(fqn.lastIndexOf('.') + 1);

		try {
			ICompilationUnit cu;
			IPackageFragment jPkg = srcRoot.createPackageFragment(packageName,
			        true, null);
			cu = jPkg.getCompilationUnit(className + ".java");
			if (!cu.exists()) {
				String newContent = "public class " + className + " {}";
				cu = jPkg.createCompilationUnit(className + ".java",
				        newContent, true, null);
				cu.createPackageDeclaration(packageName, null);
			}
			existingSource = cu;

		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	public JavaFileBuilder(ICompilationUnit existingSource) {
		this.existingSource = existingSource;
		try {
			packageName = existingSource.getPackageDeclarations()[0]
			        .getElementName();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	public void addImport(String importedClassName) {
		imports.add(importedClassName.trim());
	}

	public void addImport(Class<?> importedClass) {
		imports.add(importedClass.getName());
	}

	public boolean saveIfModified(IProgressMonitor monitor) {
		try {
			// Format the source
			Map options = EclipseUtils.getJdtCorePreferences(existingSource);

			String source = getUnformattedSource();

			// instantiate the default code formatter with the given options
			final CodeFormatter codeFormatter = ToolFactory
			        .createCodeFormatter(options, ToolFactory.M_FORMAT_NEW);

			TextEdit edit = codeFormatter.format(
			        CodeFormatter.K_COMPILATION_UNIT, source, 0,
			        source.length(), 0, null);

			if (edit == null) {
				throw new RuntimeException("Can't format the source: " + source);
			}

			IDocument newlyGeneratedSource = new Document(source);
			TextEdit edit2 = edit.copy();
			edit.apply(newlyGeneratedSource);

			// Load the existing source
			ICompilationUnit workingCopy = existingSource
			        .getWorkingCopy(monitor);
			IBuffer buffer = ((IOpenable) workingCopy).getBuffer();
			String originalSource = buffer.getContents();

			if (originalSource == null
			        || !originalSource.equals(newlyGeneratedSource.get())) {
				// ha nem egyezik az eredeti és a most generált, akkor
				// felülírjuk
				buffer.setContents(newlyGeneratedSource.get());
				// Commit changes
				workingCopy.commitWorkingCopy(true, monitor);

				// Destroy working copy
				workingCopy.discardWorkingCopy();
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeTo(PrintWriter out) {
		out.println("package " + packageName + ";");
		out.println();
		List<String> impList = new ArrayList<String>(imports);
		Collections.sort(impList);
		for (String imp : impList)
			out.println("import " + imp + ";");

		out.println();

		for (Sections section : Sections.values()) {
			PrintWriter b = buff.get(section);
			if (b == null)
				continue;
			b.flush();
			buff.get(section).flush();
			out.write(sw.get(section).toString());
		}

		out.println("}"); // Hozzáadja az osztályt lezáró } jelet
	}

	public void addLine(Sections section, String line) {
		PrintWriter b = buff.get(section);
		if (b == null) {
			StringWriter s = new StringWriter();
			sw.put(section, s);
			buff.put(section, b = new PrintWriter(s));
		}
		b.println(line);
	}

	public void addHeaderLine(String line) {
		addLine(Sections.HEADER, line);
	}

	public void addVariableLine(String line) {
		addLine(Sections.VARIABLES, line);
	}

	public void addMethodLine(String line) {
		addLine(Sections.METHODS, line);
	}

	public void addGeneratedAnnotation(String generatorName) {
		imports.add(Generated.class.getName());
		addHeaderLine("@Generated(\"" + generatorName + "\")");
	}

	public String getUnformattedSource() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		writeTo(pw);
		pw.flush();
		return sw.toString();
	}

}
