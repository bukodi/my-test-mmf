package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface MClass {

	String getName();

	public MPackage getMPackage();

	@Nullable MAttr getMAttribute( String name );

	List<? extends MAttr> listMAttributes();

}
