package my.test.mmf.core.plugin;

import java.util.List;
import java.util.concurrent.Callable;

import my.test.mmf.core.MPackage;
import my.test.mmf.core.MRoot;
import my.test.mmf.core.impl.MRootImpl;
import my.test.mmf.core.util.EclipseUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TestApp01 implements IApplication, IWorkspaceRunnable {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// JavaCore.run(this, null);
		//run(null);
		return this;
	}

	@Override
	public void stop() {
	}

	static MRoot mroot;
	
	@Override
	public void run(IProgressMonitor monitor) throws CoreException {

		{
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			wsRoot.refreshLocal(IResource.DEPTH_INFINITE, null);
			System.out.println("Workspace location: " 
					+ wsRoot.getRawLocation().toOSString());

			// JavaCore.getJavaCore().necrLikeExtensions()

			IPackageFragmentRoot jdtSourceRoot = EclipseUtils
					.getSrcRoot("/TestMMF01/src_model");
			mroot = new MRootImpl(jdtSourceRoot);
		}
		
		try{
			JavaCore.run(testCreatePackage, null);
			ResourcesPlugin.getWorkspace().save(true, null);			
		} catch ( Exception e ) {
			System.out.println(e);
		}
		
		try{			
			ResourcesPlugin.getWorkspace().addSaveParticipant(ResourcesPlugin.getPlugin(), participant );			
			JavaCore.run(testRollback, null);
			ResourcesPlugin.getWorkspace().save(true, null);			
		} catch ( Exception e ) {
			System.out.println(e);
		}
		
		
		
		{
			MPackage testpkg = mroot.createMPackage("main01.sub02");
			List<MPackage> topLevelPkgs = mroot.listMPackages();
			System.out.println(topLevelPkgs);			
		}

		final MPackage testpkg = mroot.createMPackage("main01.sub01");
		System.out.println(testpkg.getName());
		testpkg.setName("main01.sub02");
		System.out.println(testpkg.getName());
		testpkg.delete();
		ResourcesPlugin.getWorkspace().save(true, null);

	}
	
	static IWorkspaceRunnable testCreatePackage = new IWorkspaceRunnable() {

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			MPackage testpkg = mroot.createMPackage("wstrx.sub02");
		}
		
	};

	static IWorkspaceRunnable testRollback = new IWorkspaceRunnable () {

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			MPackage testpkg = mroot.createMPackage("wstrx.sub03");
			//testpkg = mroot.createPackage("wstrx/sub02");
			throw new JavaModelException(new Throwable("Iz�"), IJavaModelStatus.ERROR);
		}
		
	};

	static ISaveParticipant participant = new ISaveParticipant() {
		
		@Override
		public void saving(ISaveContext context) throws CoreException {
		}
		
		@Override
		public void rollback(ISaveContext context) {
			System.out.println("rollback");
		}
		
		@Override
		public void prepareToSave(ISaveContext context) throws CoreException {
		}
		
		@Override
		public void doneSaving(ISaveContext context) {
			System.out.println("saved");
		}
	};


}
