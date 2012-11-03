package my.test.mmf.core.impl.jre;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jdt.annotation.Nullable;

import my.test.mmf.core.MAttr;
import my.test.mmf.core.MClass;
import my.test.mmf.core.MPackage;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.util.MyRuntimeException;

public class MClassImpl implements MClass {

	private final MPackageImpl packageInfo;
	private final BeanDescriptor beanDescriptor;
	private final List<MAttr> mattrList;

	MClassImpl( MPackageImpl packageInfo, Class<?> javaClass ) {
		this.packageInfo = packageInfo;
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(javaClass);
		} catch (IntrospectionException e) {
			throw new MyRuntimeException(e);
		}
		this.beanDescriptor = beanInfo.getBeanDescriptor();

		List<MAttr> mattrList = new ArrayList<MAttr>();
		for( Enumeration<String> nameEnum = beanDescriptor.attributeNames(); nameEnum.hasMoreElements();){
			mattrList.add(new MAttrImpl(this, nameEnum.nextElement()));
		}
		this.mattrList = Collections.unmodifiableList(mattrList);
	}

	@Override
	public String getName() {
		return beanDescriptor.getName();
	}

	
	@Override
	public MPackage getMPackage() {
		return packageInfo;
	}


	@Override
	@Nullable
	public MAttr getMAttribute(String name) {
		return null;
	}

	@Override
	public List<MAttr> listMAttributes() {
		return mattrList;
	}

	@Override
	public String toString() {
		return "(" + ModifiableMClass.class.getSimpleName() + ")"
				+ beanDescriptor.getName();
	}

}
