package my.test.mmf.core.impl;

import my.test.mmf.core.MAttr;
import my.test.mmf.core.MClass;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;

public class MAttrImpl implements MAttr {

	private IField jdtField;

	public MAttrImpl(IField jdtField) {
		this.jdtField = jdtField;
	}

	public MAttrImpl(ICompilationUnit jdtParent, String name) {
		ICompilationUnit workingCopy = null;
		try {
			if (jdtParent.findPrimaryType().getField(name).exists())
				throw new MyRuntimeException("Attribute with name '" + name
						+ "' already exists.");

			IProgressMonitor monitor = MyMonitor.currentMonitor();
			workingCopy = jdtParent.getWorkingCopy(monitor);
			IType jdtType = workingCopy.findPrimaryType();
			String contents = "public final static MAttr " + name + " = null;";
			jdtField = jdtType.createField(contents, null, false,
					MyMonitor.currentMonitor());
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
		return jdtField.getElementName();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MClass getMClass() {
		return new MClassImpl(jdtField.getCompilationUnit());
	}

	@Override
	public void setMClass(MClass ownerMClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "(" + MAttr.class.getSimpleName() + ")"
				+ jdtField.getParent().getElementName() + "#" + jdtField.getElementName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jdtField == null) ? 0 : jdtField.hashCode());
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
		MAttrImpl other = (MAttrImpl) obj;
		if (jdtField == null) {
			if (other.jdtField != null)
				return false;
		} else if (!jdtField.equals(other.jdtField))
			return false;
		return true;
	}
}
