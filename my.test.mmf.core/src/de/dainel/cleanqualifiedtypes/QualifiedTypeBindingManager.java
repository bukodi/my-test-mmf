package de.dainel.cleanqualifiedtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.QualifiedName;

/**
 * @author Michael Ernst
 */
public class QualifiedTypeBindingManager {
	private boolean importAlreadyExists = false;
	
	private List<QualifiedName> qualifiedNames = new ArrayList<QualifiedName>();
	
	public boolean importAlreadyExists() {
		return importAlreadyExists;
	}
	
	public void setImportAlreadyExists(boolean importAlreadyExists) {
		this.importAlreadyExists = importAlreadyExists;
	}
	
	public List<QualifiedName> getQualifiedNames() {
		return Collections.unmodifiableList(qualifiedNames);
	}
	
	public void addQualifiedName(QualifiedName qn) {
		qualifiedNames.add(qn);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QualifiedTypeBindingManager [qualifiedName=");
		builder.append(qualifiedNames.iterator().next().getFullyQualifiedName());
		builder.append(", importAlreadyExists=");
		builder.append(importAlreadyExists);
		builder.append(", amountOfQualifiedNames=");
		builder.append(qualifiedNames.size());
		builder.append("]");
		return builder.toString();
	}
	
	
}
