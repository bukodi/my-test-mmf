package my.test.mmf.core;

public interface MClass {
	
	String getName();

	void setName(String name);

	public MPackage getMPackage();	
	
	public void setMPackage( MPackage containerPackage );

	public void remove();
}
