package ca.unbc.cpsc472.mynextphone.models;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

public class Rule {
	private int id;
	private PhoneDataBaseHelper dbHelper;
	
	public Rule(int id, PhoneDataBaseHelper dbHelper) {
		this.id = id;
		this.dbHelper = dbHelper;
	}
	
	public int getId() {
		return id;
	}
	
	
}
