package my.test.mmf.core;

import org.eclipse.jdt.annotation.Nullable;

public interface MPackageableElement extends MNamedElement {
	
	public @Nullable MPackage getPackage();
	
	public void setPackage( MPackage containerPackage );
	
	public void remove();

}
