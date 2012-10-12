package my.test.mmf.core.plugin;

import java.util.List;

import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.impl.MRootImpl;
import my.test.mmf.core.util.EclipseUtils;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

public class TestApp01 implements IApplication, IWorkspaceRunnable {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		//JavaCore.run(this, null);
		run( null);
		return null;
	}

	@Override
	public void stop() { 
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {

		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		System.out.println("Workspace location: "
				+ wsRoot.getRawLocation().toOSString());
		
		//JavaCore.getJavaCore().necrLikeExtensions()
		
		IPackageFragmentRoot jdtSourceRoot = EclipseUtils
				.getSrcRoot("/TestMMF01/src_model");
		MRoot mroot = new MRootImpl(jdtSourceRoot );
		List<MPackage> topLevelPkgs = mroot.getTopLevelPackages();
		System.out.println(topLevelPkgs);
		
		final MPackage testpkg = mroot.createTopLevelPackage("testpkg01");
		System.out.println( testpkg.getName());
		JavaCore.run(new IWorkspaceRunnable() {
			
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				testpkg.setName("testpkg01mod4");
			}
		}, null);
		System.out.println( testpkg.getName());
		testpkg.remove(); 

		
	}

}
