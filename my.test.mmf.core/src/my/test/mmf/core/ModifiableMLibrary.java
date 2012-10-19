package my.test.mmf.core;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public interface ModifiableMLibrary extends MLibrary {

	@Override
	@Nullable
	ModifiableMPackage getMPackage(String name);

	@Override
	List<? extends ModifiableMPackage> listMPackages();

	ModifiableMPackage createMPackage(String name);

}
