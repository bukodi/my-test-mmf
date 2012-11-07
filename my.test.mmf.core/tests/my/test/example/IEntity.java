package my.test.example;


public interface IEntity extends INamedElement {

	String getTableName();

	void setTableName(String tableName);

	String getIdColumn();

}