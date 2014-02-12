package ca.unbc.cpsc472.mynextphone.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import ca.unbc.cpsc472.mynextphone.models.*;

public class PhoneDataBaseHelper extends DataBaseHelper {
    protected static String DB_NAME = "db.sqlite";
    private static String[] ruleColumns = {"id"};

	public PhoneDataBaseHelper(Context context) {
		super(context);
		DB_PATH = context.getFilesDir().getPath() + "/databases/";
	}
	
	public ArrayList<Rule> getRules() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		ArrayList<Rule> rules = new ArrayList<Rule>();
		Cursor rulesCursor = getRulesCursor();
		while(!rulesCursor.isAfterLast()) {
			Rule r = new Rule(rulesCursor.getInt(rulesCursor.getColumnIndex(ruleColumns[0])));
			rules.add(r);
		}
		
		return rules;
	}
	
	public ArrayList<Condition> getConditions 
	
	private Cursor getRulesCursor() {
		return myDataBase.query("rule", new String[] {"id"}, null, null, null, null, null);
	}
	
	private boolean dbIsOpen() {
		return myDataBase.isOpen();
	}
}
