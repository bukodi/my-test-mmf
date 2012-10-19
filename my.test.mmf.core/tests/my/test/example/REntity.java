package my.test.example;


public class REntity extends RNamedElement implements IEntity {
	
	private String tableName;
	private String idColumn;
	
	@Override
	public String getTableName() {
		return tableName;
	}
	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	@Override
	public String getIdColumn() {
		return tableName + "_ID";
	}
	
}
