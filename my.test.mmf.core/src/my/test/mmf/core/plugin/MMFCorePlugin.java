package my.test.mmf.core.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class MMFCorePlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		MMFCorePlugin.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		MMFCorePlugin.context = null;
	}

}
