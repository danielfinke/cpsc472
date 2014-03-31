package ca.unbc.cpsc472.mynextphone.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.Result;
import ca.unbc.cpsc472.mynextphone.models.Rule;

public class PhoneDataBaseHelper extends DataBaseHelper {
    private static String[] questionColumns = {"_id", "question", "type"};
    private static String[] answerColumns = {"_id", "question_id", "answer", "facts"};
    private static String[] ruleColumns = {"_id", "rule"};
    
    private static final int DATABASE_VERSION = 2;

	public PhoneDataBaseHelper(Context context) {
		super(context);
		DB_PATH = context.getFilesDir().getParent() + "/databases/";
		DB_NAME = "db.sqlite";
		
		if(checkDatabase()) {
			openDataBase();
		}
		else {
			try {
				createDataBase();
				openDataBase();
			} catch(IOException ex) {
				Log.e(this.getClass().getName(), ex.getMessage());
			}
		}
		
	}
    
    /*
	 * Fetches all available questions from the database
	 */
	public ArrayList<Question> getQuestions() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		// Loop, adding each question to the return list
		ArrayList<Question> questions = new ArrayList<Question>();
		Cursor cursor = getQuestionsCursor();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int questionId = cursor.getInt(cursor.getColumnIndex(questionColumns[0]));
			
			// Load the type of question
			QuestionAnswerType answerType =
				QuestionAnswerType.values()[
					cursor.getInt(cursor.getColumnIndex(questionColumns[2]))
				];
			// Instantiate Question wrapper class (will also load answers available)
			Question q = new Question(
					questionId,
					cursor.getString(cursor.getColumnIndex(questionColumns[1])),
					answerType,
					this.getAnswersForQuestionId(questionId, answerType)
			);
			questions.add(q);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return questions;
	}
	
	public long getQuestionCount() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		return DatabaseUtils.queryNumEntries(myDataBase, "question");
	}
	
	public ArrayList<QuestionAnswer> getAnswersForQuestionId(int questionId, QuestionAnswerType type) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<QuestionAnswer> answers = new ArrayList<QuestionAnswer>();
		Cursor cursor = getAnswersCursorForQuestionId(questionId);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			answers.add(QuestionAnswer.getInstance(
					cursor.getInt(cursor.getColumnIndex(answerColumns[0])),
					cursor.getString(cursor.getColumnIndex(answerColumns[2])),
					cursor.getString(cursor.getColumnIndex(answerColumns[3])),
					type
			));
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return answers;
	}
	
	/*
	 * Load up the entire set of rules into memory
	 * Note that each rule row in the database does not represent a unique rule,
	 * I simply compressed the schema of left and right sides into one table.
	 * This simplifies the task of adding new rows in SQLite (no placeholder column)
	 */
	public ArrayList<Rule> getRules() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Rule> rules = new ArrayList<Rule>();
		Cursor cursor = getRulesCursor();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			rules.add(new Rule(
					cursor.getInt(cursor.getColumnIndex(ruleColumns[0])),
					cursor.getString(cursor.getColumnIndex(ruleColumns[1]))
			));
			cursor.moveToNext();
		}
		cursor.close();
		
		return rules;
	}
	
	public ArrayList<Result> getResultsWithFacts(ArrayList<Fact> facts) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Result> results = new ArrayList<Result>();
		Cursor cursor = getResultsCursor(getQueryStringFromLingVars(facts));
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			results.add(new Result(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("description")),
					facts
			));
			cursor.moveToNext();
		}
		cursor.close();
		
		return results;
	}
	
	public ArrayList<String> getValueNames(){
		TreeSet<String> values = new TreeSet<String>();
		
		Cursor cursor = this.getRulesCursor();
		cursor.moveToFirst();
		do{
			String[] split = cursor.getString(cursor.getColumnIndex("rule")).split(">");
			for(Fact f:Fact.parseFactsToList(split[0]))
				values.add(f.getName());
			for(Fact f:Fact.parseFactsToList(split[1]))
				values.add(f.getName());
		}while(cursor.moveToNext());
		
		return new ArrayList<String>(values);
	}
	
	private String getQueryStringFromLingVars(ArrayList<Fact> facts) {
		String query = "";
		for(Fact f : facts) {
			query += f.getName() + "_linguistic = '" + f.getLinguisticValues().get(0) + "'";
			if(f != facts.get(facts.size()-1)) {
				query += " AND ";
			}
		}
		return query;
	}
	
	private Cursor getQuestionsCursor() {
		return myDataBase.query("question", questionColumns, null, null, null, null, null);
	}
	
	private Cursor getAnswersCursorForQuestionId(int questionId) {
		return myDataBase.query("answer", answerColumns, "question_id = " + questionId, null,
				null, null, null);
	}
	
	private Cursor getRulesCursor() {
		return myDataBase.query("rule", ruleColumns, null, null, null, null, null);
	}
	
	private Cursor getResultsCursor(String whereClause) {
		return myDataBase.query("data", null, whereClause, null, null, null, null);
	}
	
	private boolean dbIsOpen() {
		return myDataBase.isOpen();
	}
	
	public boolean checkDatabase() {
		return super.checkDataBase();
	}
	
	public void openDataBase() throws SQLException {
    	super.openDataBase();
    	int old = myDataBase.getVersion();
    	Log.d(this.getClass().getName(), "Old db version: " + old);
    	
    	if(old < DATABASE_VERSION) {
    		onUpgrade(myDataBase, old, DATABASE_VERSION);
    	}
    }
	public void openWriteableDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(checkDatabase()) {
			myDataBase.close();
			try {
				this.copyDataBase();
				openWriteableDataBase();
				myDataBase.setVersion(newVersion);
				myDataBase.close();
				openDataBase();
			} catch (IOException e) {
				Log.e(this.getClass().getName(), "Unable to create SQLite database");
			}
		}
	}
}
