package ca.unbc.cpsc472.mynextphone.models;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

public class Condition {
	private int id;
	private int ruleId;
	private int factId;
	private PhoneDataBaseHelper dbHelper;
	
	public Condition(int id, int ruleId, int factId, PhoneDataBaseHelper dbHelper) {
		this.id = id;
		this.ruleId = ruleId;
		this.factId = factId;
		this.dbHelper = dbHelper;
	}
	
	public int getId() {
		return id;
	}
	
	public int getRuleId() {
		return ruleId;
	}
	
	public int getFactId() {
		return factId;
	}
	
	public Fact getFact() throws Exception {
		return dbHelper.getFactForFactId(factId);
	}
}
