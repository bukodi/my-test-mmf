package my.test.mmf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

public class MyMonitor {
	
	private final static ThreadLocal<IProgressMonitor> monitor_TL = new ThreadLocal<IProgressMonitor>();

	private MyMonitor() {
	}

	public static @Nullable IProgressMonitor currentMonitor() {
		return monitor_TL.get();
	}
}
