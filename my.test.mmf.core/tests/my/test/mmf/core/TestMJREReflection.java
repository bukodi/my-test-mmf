package my.test.mmf.core;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;

import my.test.example.IEntity;
import my.test.example._LibraryInfo_;
import my.test.mmf.core.impl.jre.MLibraryImpl;

import org.junit.Test;

public class TestMJREReflection {
	
	@Test
	public void testBeans() throws Exception {
		java.beans.BeanInfo beanInfo = Introspector.getBeanInfo(IEntity.class);
		
		for( PropertyDescriptor  pd : beanInfo.getPropertyDescriptors()) {
			System.out.println(pd.getName()); 
		}
	}

	@Test 
	public void testListMPackages() throws Exception {
		MLibraryImpl mlib = _LibraryInfo_.instance();
		List<MPackage> mpkgList = mlib.listMPackages();
		System.out.println(mpkgList);
	}

	@Test
	public void testListMClasses() throws Exception {
		MLibraryImpl mlib = _LibraryInfo_.instance();
		MPackage mpkg = mlib.listMPackages().get(0);
		List<? extends MClass> mclasses = mpkg.listMClasses();
		System.out.println(mclasses);
	}

	@Test
	public void testListMAttrs() throws Exception {
		MLibraryImpl mlib = _LibraryInfo_.instance();
		MPackage mpkg = mlib.listMPackages().get(0);
		for (MClass mclass : mpkg.listMClasses())
			System.out.println(mclass.listMAttributes());
	}

}
