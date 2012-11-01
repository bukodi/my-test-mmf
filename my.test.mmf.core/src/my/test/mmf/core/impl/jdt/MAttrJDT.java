package my.test.mmf.core.impl.jdt;

import java.util.ArrayList;
import java.util.List;

import my.test.mmf.core.ModifiableMAttr;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

public class MAttrJDT implements ModifiableMAttr {

	private IMethod jdtGetter;
	private IMethod jdtSetter;

	MAttrJDT(IMethod jdtGetter ) {
		this.jdtGetter = jdtGetter;
		// TODO: Find jdtSetter, if available
		this.jdtSetter = jdtSetter;
	}

	public MAttrJDT(ICompilationUnit jdtParent, String name ) {
		ICompilationUnit workingCopy = null;
		try {
			if (jdtParent.findPrimaryType().getField(name).exists())
				throw new MyRuntimeException("Attribute with name '" + name
						+ "' already exists.");

			if( ! Character.isLowerCase( name.charAt(0) ) )
				throw new MyRuntimeException("First character must be lowercase: '" + name
						+ "'");
							
			IProgressMonitor monitor = MyMonitor.currentMonitor();
			workingCopy = jdtParent.getWorkingCopy(monitor);
			IType jdtType = workingCopy.findPrimaryType();
			
			String methodName = Character.toUpperCase( name.charAt(0) ) + name.substring(1);
			String contents = "String get" + methodName + "();";
			jdtGetter = jdtType.createMethod(contents, null, false, MyMonitor.currentMonitor());
			contents = "void set" + methodName + "("+name+");";
			jdtSetter = jdtType.createMethod(contents, null, false, MyMonitor.currentMonitor());
			workingCopy.commitWorkingCopy(false, monitor);
		} catch (JavaModelException e) {
			throw new MyRuntimeException("Can not create attribute '" + name + "'.", e);
		} finally {
			if (workingCopy != null)
				try {
					workingCopy.discardWorkingCopy();
				} catch (JavaModelException e) {
					throw new MyRuntimeException(e);
				}
		}
	}

	@Override
	public String getName() {
		String methodName = jdtGetter.getElementName();
		if( methodName.startsWith("is") )
			methodName = methodName.substring(2);
		else if( methodName.startsWith("get") )
			methodName = methodName.substring(3);
		else
			throw new MyRuntimeException("Invalid getter name:" + methodName );
		methodName = Character.toLowerCase( methodName.charAt(0) ) + methodName.substring(1);
		return methodName;
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ModifiableMClass getMClass() {
		return new MClassJDT(jdtGetter.getCompilationUnit());
	}

	@Override
	public void setMClass(ModifiableMClass ownerMClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "(" + ModifiableMAttr.class.getSimpleName() + ")"
				+ jdtGetter.getParent().getElementName() + "#" + jdtGetter.getElementName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jdtGetter == null) ? 0 : jdtGetter.hashCode());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MAttrJDT other = (MAttrJDT) obj;
		if (jdtGetter == null) {
			if (other.jdtGetter != null)
				return false;
		} else if (!jdtGetter.equals(other.jdtGetter))
			return false;
		return true;
	}
	
	static List<ModifiableMAttr> listAttributes( ITypeRoot jdtCu, @Nullable String name ) {
		List<ModifiableMAttr> mattrList = new ArrayList<ModifiableMAttr>();
		try {
			IType jdtType = jdtCu.findPrimaryType();
			for (IMethod jdtGetter : jdtType.getMethods()) {
				int flags = jdtGetter.getFlags();
				if( Flags.isStatic( flags ) || Flags.isPrivate(flags) )
					continue;
				String methodName = jdtGetter.getElementName();
				String varName;
				if( methodName.startsWith("is") ) {
					String retTypeSignature = jdtGetter.getReturnType();
					if(!( "Z".equals(retTypeSignature) || "QBoolean".equals(retTypeSignature) || "java.lang.Boolean".equals(retTypeSignature) ))
						continue;
					if( name != null && !name.equals(Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3) ) )
						continue;
				} else if( methodName.startsWith("get") ) {
					if( name != null && !name.equals(Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4) ) )
							continue;
				} else {
					continue;
				}
				mattrList.add(new MAttrJDT(jdtGetter));
			}
			if( name != null && mattrList.size() > 0 )
				throw new MyRuntimeException("Ambiguous name:" + name);
		} catch (JavaModelException e) {
			throw new MyRuntimeException(e);
		}
		return mattrList;
	}
}
