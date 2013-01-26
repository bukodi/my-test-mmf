package my.test.mmf.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.osgi.service.prefs.BackingStoreException;

public class EclipseUtils {

	public static List<IType> findAllGeneratedSubtypes(
	        IPackageFragmentRoot srcRoot, IType supertype,
	        IProgressMonitor monitor) throws JavaModelException {
		IRegion region = JavaCore.newRegion();
		region.add(srcRoot);
		ITypeHierarchy th = srcRoot.getJavaProject().newTypeHierarchy(
		        supertype, region, monitor);
		IType[] subtypes = th.getAllSubtypes(supertype);
		List<IType> retList = new ArrayList<IType>();
		for (IType subType : subtypes) {
			boolean isGenerated = false;
			IAnnotation[] annots = subType.getAnnotations();
			for (IAnnotation annot : annots) {
				String annotName = annot.getElementName();
				if (annotName.contains("Generated"))
					isGenerated = true;
			}
			if (isGenerated)
				retList.add(subType);
		}
		return retList;
	}

	public static Map getJdtCorePreferences(IJavaElement javaElem) {
		IProject project = javaElem.getJavaProject().getProject();
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
		        .getNode(JavaCore.PLUGIN_ID);
		Map<String, String> ret = new HashMap<String, String>();
		try {
			for (String key : projectNode.keys())
				ret.put(key, projectNode.get(key, null));
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public static IPackageFragmentRoot getSrcRoot(String path) {
		try {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IFolder folder = wsRoot.getFolder(Path.fromPortableString(path));
			IJavaProject jProject = JavaCore.create(folder.getProject());
			jProject.open(null);
			IPackageFragmentRoot srcRoot = jProject
			        .getPackageFragmentRoot(folder);
			srcRoot.getUnderlyingResource().refreshLocal(
			        IResource.DEPTH_INFINITE, null);
			return srcRoot;
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

	}
	
	public static abstract class JDTEditor {
		
		JDTEditor( JavaElement jeElem   ) {
			jeElem.getHandleIdentifier();
		}
		
		abstract void doEdit( );
		
	}

}
