package my.test.mmf.core;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.impl.MRootImpl;
import my.test.mmf.core.util.EclipseUtils;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMRootTest {

	final static String PROJECT_NAME = "TestMMF01";

	static MRoot mroot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

		mroot = new MRootImpl(srcRoot);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourcesPlugin.getWorkspace().save(true, null);
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMPackage() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			mroot.createMPackage("main01.sub01");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMPackages() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			List<MPackage> pkgList = mroot.listMPackages();
			Assert.assertArrayEquals( "Only one package", new MPackage[]{testpkg}, pkgList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMPackage() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			testpkg.setName("main02.sub02");
			Assert.assertEquals( "New name of the package", "main02.sub02", testpkg.getName());
			List<MPackage> pkgList = mroot.listMPackages();
			Assert.assertArrayEquals( "Only one package", new MPackage[]{testpkg}, pkgList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMClass() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			testpkg.createMClass("MClass01");
			testpkg.createMClass("MClass01");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMClasses() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass testCls = testpkg.createMClass("MClass01");
			List<MClass> clsList = testpkg.listMClasses();
			Assert.assertArrayEquals( "Only one class", new MClass[]{testCls}, clsList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMClass() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass testCls = testpkg.createMClass("MClass01");
			testCls.setName("MClass02");
			Assert.assertEquals( "New name of the class", "MClass02", testCls.getName());
			List<MClass> clsList = testpkg.listMClasses();
			Assert.assertArrayEquals( "Only one class", new MClass[]{testCls}, clsList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testMoveMClass() {
		MPackage testpkg1 = mroot.createMPackage("main01.sub01");
		MPackage testpkg2 = mroot.createMPackage("main02.sub02");
		try {
			MClass testCls = testpkg1.createMClass("MClass01");
			testCls.setMPackage(testpkg2);
			Assert.assertEquals( "New package of the class", testpkg2, testCls.getMPackage());
			Assert.assertTrue( "First package is empty", testpkg1.listMClasses().isEmpty());
			List<MClass> clsList2 = testpkg2.listMClasses();
			Assert.assertArrayEquals( "Only one class in second package", new MClass[]{testCls}, clsList2.toArray());
		} finally {
			testpkg1.delete();
		}
	}

	@Test
	public void testDeleteMClass() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass testCls = testpkg.createMClass("MClass01");
			testCls.delete();
			Assert.assertTrue( "Package is empty", testpkg.listMClasses().isEmpty());
		} finally {
			testpkg.delete();
		}
	}

	@Test(expected = MyRuntimeException.class)
	public void testDuplicateMAttr() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass cls = testpkg.createMClass("MClass01");
			cls.createMAttribute("attr01");
			cls.createMAttribute("attr01");
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testListMAttrs() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass cls = testpkg.createMClass("MClass01");
			MAttr attr = cls.createMAttribute("attr01");
			List<MAttr> attrList = cls.listMAttributes();
			Assert.assertArrayEquals( "Only one attribute", new MAttr[]{attr}, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testRenameMAttr() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass cls = testpkg.createMClass("MClass01");
			MAttr attr = cls.createMAttribute("attr01");
			attr.setName("attr02");
			Assert.assertEquals( "New name of the attr", "attr02", attr.getName());
			List<MAttr> attrList = cls.listMAttributes();
			Assert.assertArrayEquals( "Only one attribute", new MAttr[]{attr}, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testMoveMAttr() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass cls1 = testpkg.createMClass("MClass01");
			MClass cls2 = testpkg.createMClass("MClass02");
			MAttr attr = cls1.createMAttribute("attr01");
			attr.setMClass(cls2);
			Assert.assertEquals( "New owner class of the attribute", cls2, attr.getMClass());
			Assert.assertTrue( "First package is empty", testpkg.listMClasses().isEmpty());
			List<MAttr> attrList = cls2.listMAttributes();
			Assert.assertArrayEquals( "Only one attribute", new MAttr[]{attr}, attrList.toArray());
		} finally {
			testpkg.delete();
		}
	}

	@Test
	public void testDeleteMAttr() {
		MPackage testpkg = mroot.createMPackage("main01.sub01");
		try {
			MClass cls = testpkg.createMClass("MClass01");
			MAttr attr = cls.createMAttribute("attr01");
			attr.delete();
			Assert.assertTrue( "Class has no attribute", cls.listMAttributes().isEmpty());
		} finally {
			testpkg.delete();
		}
	}

}
