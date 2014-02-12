package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;

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
	
	public ArrayList<Condition> getConditions() throws Exception {
		return dbHelper.getConditionsForRuleId(id);
	}
	
	public ArrayList<Answer> getAnswers() throws Exception {
		return dbHelper.getAnswersForRuleId(id);
	}
}
