package example;

import my.test.mmf.core.MAttr;
import my.test.mmf.core.impl.jdt.MAttrJDT;

public interface REntity extends RNamedElement {

	long getId();
	
	void setId( long id );
}
