package ca.unbc.cpsc472.mynextphone.database;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import ca.unbc.cpsc472.mynextphone.models.*;

public class PhoneDataBaseHelper extends DataBaseHelper {
    protected static String DB_NAME = "db.sqlite";
    private static String[] ruleColumns = {"id"};
    private static String[] conditionColumns = {"id", "rule_id", "fact_id"};
    private static String[] answerColumns = {"id", "rule_id", "fact_id"};
    private static String[] factColumns = {"id", "name", "truth_flag"};

	public PhoneDataBaseHelper(Context context) {
		super(context);
		DB_PATH = context.getFilesDir().getPath() + "/databases/";
	}
	
	public ArrayList<Rule> getRules() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		ArrayList<Rule> rules = new ArrayList<Rule>();
		Cursor cursor = getRulesCursor();
		while(!cursor.isAfterLast()) {
			Rule r = new Rule(cursor.getInt(cursor.getColumnIndex(ruleColumns[0])), this);
			rules.add(r);
			cursor.moveToNext();
		}
		
		return rules;
	}
	
	public ArrayList<Condition> getConditionsForRuleId(int ruleId) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		ArrayList<Condition> conditions = new ArrayList<Condition>();
		Cursor cursor = getConditionsCursorForRuleId(ruleId);
		while(!cursor.isAfterLast()) {
			Condition c = new Condition(
					cursor.getInt(cursor.getColumnIndex(conditionColumns[0])),
					cursor.getInt(cursor.getColumnIndex(conditionColumns[1])),
					cursor.getInt(cursor.getColumnIndex(conditionColumns[2])),
					this
			);
			conditions.add(c);
			cursor.moveToNext();
		}
		
		return conditions;
	}
	
	public ArrayList<Answer> getAnswersForRuleId(int ruleId) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		ArrayList<Answer> answers = new ArrayList<Answer>();
		Cursor cursor = getAnswersCursorForRuleId(ruleId);
		while(!cursor.isAfterLast()) {
			Answer a = new Answer(
					cursor.getInt(cursor.getColumnIndex(answerColumns[0])),
					cursor.getInt(cursor.getColumnIndex(answerColumns[1])),
					cursor.getInt(cursor.getColumnIndex(answerColumns[2])),
					this
			);
			answers.add(a);
			cursor.moveToNext();
		}
		
		return answers;
	}
	
	public Fact getFactForFactId(int factId) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		Cursor cursor = getFactsCursorForFactId(factId);
		return new Fact(
				cursor.getInt(cursor.getColumnIndex(factColumns[0])),
				cursor.getString(cursor.getColumnIndex(factColumns[1])),
				cursor.getInt(cursor.getColumnIndex(factColumns[2]))
		);
	}
	
	private Cursor getRulesCursor() {
		return myDataBase.query("rule", ruleColumns, null, null, null, null, null);
	}
	
	private Cursor getConditionsCursorForRuleId(int ruleId) {
		return myDataBase.query("condition", conditionColumns, "rule_id = " + ruleId, null, null,
				null, null);
	}
	
	private Cursor getAnswersCursorForRuleId(int ruleId) {
		return myDataBase.query("answer", answerColumns, "rule_id = " + ruleId, null, null,
				null, null);
	}
	
	private Cursor getFactsCursorForFactId(int factId) {
		return myDataBase.query("fact", factColumns, "id = " + factId, null, null,
				null, null);
	}
	
	private boolean dbIsOpen() {
		return myDataBase.isOpen();
	}
}
