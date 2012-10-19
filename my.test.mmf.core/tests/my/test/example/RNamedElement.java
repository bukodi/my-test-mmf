package my.test.example;

import java.io.Serializable;

public class RNamedElement implements INamedElement  {

	
	public RNamedElement() {
		
	}
	
	private String naturalName;

	@Override
	public String getNaturalName() {
		return naturalName;
	}

	@Override
	public void setNaturalName(String naturalName) {
		this.naturalName = naturalName;
	}
		

}
