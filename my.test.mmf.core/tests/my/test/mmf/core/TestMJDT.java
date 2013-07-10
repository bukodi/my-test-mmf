package my.test.mmf.core;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.impl.jdt.MLibraryJDT;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMJDT {

	final static String PROJECT_NAME = "TestMMF01";

	static final IPackageFragment jdtPgkTest;
	static final ModifiableMLibrary mlib;

	static {
		try {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			wsRoot.refreshLocal(IResource.DEPTH_INFINITE, null);

			// Create empty project
			IProject project = wsRoot.getProject(PROJECT_NAME);
			project.create(null);
			project.open(null);

			// Add Java nature
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

			IJavaProject javaProject = JavaCore.create(project);

			// Create source and target location
			// IFolder binFolder = project.getFolder("bin");
			// binFolder.create(false, true, null);
			// javaProject.setOutputLocation(binFolder.getFullPath(), null);
			IFolder sourceFolder = project.getFolder("src_model");
			sourceFolder.create(false, true, null);

			// Add libs to project classpath
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime
					.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(
						element.getSystemLibraryPath(), null, null));
			}
			IPackageFragmentRoot srcRoot = javaProject
					.getPackageFragmentRoot(sourceFolder);
			entries.add(JavaCore.newSourceEntry(srcRoot.getPath()));
			javaProject.setRawClasspath(
					entries.toArray(new IClasspathEntry[entries.size()]), null);

			jdtPgkTest = srcRoot.createPackageFragment("test",
					false, null);
			mlib = new MLibraryJDT(jdtPgkTest, "CommonLibrary");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourcesPlugin.getWorkspace().save(true, null);
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMPackage() {
		assert (mlib != null);
		ModifiableMPackage testpkg = mlib.createMPackage("test.duplicatepkg");
		try {
			mlib.createMPackage("test.duplicatepkg");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMPackages() {
		MLibraryJDT mlib = new MLibraryJDT(jdtPgkTest, "LibraryListPackages");
		ModifiableMPackage testpkg =  mlib.createMPackage("test.listpkgs");
		try {
			ModifiableMPackage pkgFind = mlib.getMPackage("test.listpkgs");
			Assert.assertNotNull(pkgFind);
			List<? extends ModifiableMPackage> pkgList = mlib.listMPackages();
			Assert.assertArrayEquals("Only one package",
					new ModifiableMPackage[] { testpkg }, pkgList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMPackage() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.renamepkg1");
		try {
			testpkg.setName("test.renamepkg2");
			Assert.assertEquals("New name of the package", "test.renamepkg2",
					testpkg.getName());
			List<? extends ModifiableMPackage> pkgList = mlib.listMPackages();
			Assert.assertArrayEquals("Only one package",
					new ModifiableMPackage[] { testpkg }, pkgList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMClass() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.duplicateclass");
		try {
			testpkg.createClass("MClass01");
			testpkg.createClass("MClass01");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMClasses() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.listclasses");
		try {
			ModifiableMClass testCls = testpkg.createClass("MClass01");
			List<? extends ModifiableMClass> clsList = testpkg.listMClasses();
			Assert.assertArrayEquals("Only one class",
					new ModifiableMClass[] { testCls }, clsList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMClass() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.renameclass");
		try {
			ModifiableMClass testCls = testpkg.createClass("MClass01");
			testCls.setName("MClass02");
			Assert.assertEquals("New name of the class", "MClass02",
					testCls.getName());
			List<? extends ModifiableMClass> clsList = testpkg.listMClasses();
			Assert.assertArrayEquals("Only one class",
					new ModifiableMClass[] { testCls }, clsList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testMoveMClass() {
		ModifiableMPackage testpkg1 = mlib.createMPackage("test.moveclass1");
		ModifiableMPackage testpkg2 = mlib.createMPackage("test.moveclass2");
		try {
			ModifiableMClass testCls = testpkg1.createClass("MClass01");
			testCls.setMPackage(testpkg2);
			Assert.assertEquals("New package of the class", testpkg2,
					testCls.getMPackage());
			Assert.assertTrue("First package is empty", testpkg1.listMClasses()
					.isEmpty());
			List<? extends ModifiableMClass> clsList2 = testpkg2.listMClasses();
			Assert.assertArrayEquals("Only one class in second package",
					new ModifiableMClass[] { testCls }, clsList2.toArray());
		} finally {
			testpkg1.delete();
		}
	}

	@Test
	public void testDeleteMClass() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.deleteclass");
		try {
			ModifiableMClass testCls = testpkg.createClass("MClass01");
			testCls.delete();
			Assert.assertTrue("Package is empty", testpkg.listMClasses()
					.isEmpty());
		} finally {
			testpkg.delete();
		}
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMAttr() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.duplicateattr");
		try {
			ModifiableMClass cls = testpkg.createClass("MClass01");
			cls.createAttribute("attr01");
			cls.createAttribute("attr01");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMAttrs() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.listattr");
		try {
			ModifiableMClass cls = testpkg.createClass("MClass01");
			ModifiableMAttr attr = cls.createAttribute("attr01");
			List<? extends MAttr> attrList = cls.listMAttributes();
			Assert.assertArrayEquals("Only one attribute",
					new ModifiableMAttr[] { attr }, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMAttr() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.renameattr");
		try {
			ModifiableMClass cls = testpkg.createClass("MClass01");
			ModifiableMAttr attr = cls.createAttribute("attr01");
			attr.setName("attr02");
			Assert.assertEquals("New name of the attr", "attr02",
					attr.getName());
			List<? extends MAttr> attrList = cls.listMAttributes();
			Assert.assertArrayEquals("Only one attribute",
					new ModifiableMAttr[] { attr }, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testMoveMAttr() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.moveattr");
		try {
			ModifiableMClass cls1 = testpkg.createClass("MClass01");
			ModifiableMClass cls2 = testpkg.createClass("MClass02");
			ModifiableMAttr attr = cls1.createAttribute("attr01");
			attr.setMClass(cls2);
			Assert.assertEquals("New owner class of the attribute", cls2,
					attr.getMClass());
			Assert.assertTrue("First class has no attribute", cls1
					.listMAttributes().isEmpty());
			List<? extends MAttr> attrList = cls2.listMAttributes();
			Assert.assertArrayEquals("Second class has one attribute",
					new ModifiableMAttr[] { attr }, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testDeleteMAttr() {
		ModifiableMPackage testpkg = mlib.createMPackage("test.deleteattr");
		try {
			ModifiableMClass cls = testpkg.createClass("MClass01");
			ModifiableMAttr attr = cls.createAttribute("attr01");
			attr.delete();
			Assert.assertTrue("Class has no attribute", cls.listMAttributes()
					.isEmpty());
		} finally {
			testpkg.delete();
		}
	}

	
	private void astTest() throws Exception {
		   // creation of a Document
		   ICompilationUnit cu = (ICompilationUnit)(new Object()); //jdtLibraryInfo; // content is "public class X {\n}"
		   String source = cu.getSource();
		   Document document= new Document(source);

		   // creation of DOM/AST from a ICompilationUnit
		   ASTParser parser = ASTParser.newParser(AST.JLS4);
		   parser.setSource(cu);
		   CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		   // start record of the modifications
		   astRoot.recordModifications();

		   // modify the AST
		   TypeDeclaration typeDeclaration = (TypeDeclaration)astRoot.types().get(0);
		   SimpleName newName = astRoot.getAST().newSimpleName("Y");
		   typeDeclaration.setName(newName);

		   // computation of the text edits
		   TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));

		   // computation of the new source code
		   edits.apply(document);
		   String newSource = document.get();

		   // update of the compilation unit
		   cu.getBuffer().setContents(newSource);				
	}
}
