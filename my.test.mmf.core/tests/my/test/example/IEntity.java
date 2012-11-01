package my.test.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface IEntity extends INamedElement {

	//@formatter:off
	public final static List<String> _FIELD_ORDER_ = Collections.unmodifiableList(Arrays.asList(
			"tableName", 
			"idColumn"));
	//@formatter:on

	String getTableName();

	void setTableName(String tableName);

	String getIdColumn();

}