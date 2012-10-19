package my.test.mmf.core.impl.jdt;

import my.test.mmf.core.ModifiableMAttr;
import my.test.mmf.core.ModifiableMClass;
import my.test.mmf.core.util.MyMonitor;
import my.test.mmf.core.util.MyRuntimeException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class MAttrJDT implements ModifiableMAttr {

	private IField jdtField;

	public MAttrJDT(IField jdtField) {
		this.jdtField = jdtField;
	}

	public MAttrJDT(ICompilationUnit jdtParent, String name) {
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
	public ModifiableMClass getMClass() {
		return new MClassJDT(jdtField.getCompilationUnit());
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
		MAttrJDT other = (MAttrJDT) obj;
		if (jdtField == null) {
			if (other.jdtField != null)
				return false;
		} else if (!jdtField.equals(other.jdtField))
			return false;
		return true;
	}
}
