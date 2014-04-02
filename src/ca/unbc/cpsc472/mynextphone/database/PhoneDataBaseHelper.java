package ca.unbc.cpsc472.mynextphone.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.InferenceEngine;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.Result;
import ca.unbc.cpsc472.mynextphone.models.Rule;
import ca.unbc.cpsc472.mynextphone.models.Tuple;

public class PhoneDataBaseHelper extends DataBaseHelper {
    private static String[] questionColumns = {"_id", "question", "type"};
    private static String[] answerColumns = {"_id", "question_id", "answer", "facts"};
    private static String[] ruleColumns = {"_id", "rule"};
    private static String[] linguisticColumns = {"_id", "\"set\"", "min", "max", "value", "grouping"};
    
    private static final int DATABASE_VERSION = 2;
    
    private static PhoneDataBaseHelper helper = null;
    
    public static PhoneDataBaseHelper getInstance(Context context) {
    	if(helper == null) {
    		helper = new PhoneDataBaseHelper(context);
    	}
    	return helper;
    }

	protected PhoneDataBaseHelper(Context context) {
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
	
	public ArrayList<Result> getResultsWithFacts(ArrayList<Fact> facts, ArrayList<Fact> reasoning) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Result> results = new ArrayList<Result>();
		Cursor cursor = getResultsCursor(getQueryStringFromLingVars(facts), getOrderByString(facts));
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			Result r = new Result(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("description")),
					//reasoning
					facts
			);
			ArrayList<String> imgPaths = new ArrayList<String>();
			imgPaths.add(cursor.getString(cursor.getColumnIndex("web_irl")));
			results.add(r);
			
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
		
		cursor.close();
		
		return new ArrayList<String>(values);
	}
	
	public ArrayList<Result> getNearestResults(ArrayList<Fact> facts, ArrayList<Fact> reasoning) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Result> results = new ArrayList<Result>();
		Cursor cursor = getResultsCursor(null, getOrderByString(facts));
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			results.add(new Result(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("description")),
					//reasoning
					facts
			));
			cursor.moveToNext();
		}
		cursor.close();
		
		return results;
	}
	
	public ArrayList<Tuple> getLinguisticTuples(String lingName) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		Cursor cursor = getLinguisticTuplesCursor(lingName);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			tuples.add(new Tuple(new Object[] {
					cursor.getDouble(cursor.getColumnIndex("min")),
					cursor.getDouble(cursor.getColumnIndex("max")),
					cursor.getDouble(cursor.getColumnIndex("value"))
			}));
			cursor.moveToNext();
		}
		cursor.close();
		
		return tuples;
	}
	
	public ArrayList<String> getLinguisticNames(int grouping){
		ArrayList<String> names = new ArrayList<String>();
		Cursor cursor = getLinguisticTuplesCursor(grouping);
		if(cursor.moveToFirst())
			do{
				String name = cursor.getString(cursor.getColumnIndex("set"));
				if(names.isEmpty() || !names.get(names.size()-1).equals(name)){
					names.add(name);
				}
			}while(cursor.moveToNext());
		cursor.close();
		
		return names;
	}
	
	public int getSetGroupingByValueName(String name){
		if(Fact.isLinguisticVariable(name)){
			return 1;
		}
		
		Cursor cursor = this.getRulesCursor();
		cursor.moveToFirst();
		do{
			String[] split = cursor.getString(cursor.getColumnIndex("rule")).split(">");
			for(Fact f:Fact.parseFactsToList(split[0]))
				if(name.equals(f.getName())){
					Cursor c = getLinguisticTuplesCursor(f.getSet());
					c.moveToFirst();
					return c.getInt(c.getColumnIndex("grouping"));
				}
			for(Fact f:Fact.parseFactsToList(split[1]))
				if(name.equals(f.getName())){
					Cursor c = getLinguisticTuplesCursor(f.getSet());
					c.moveToFirst();
					return c.getInt(c.getColumnIndex("grouping"));
				}
		}while(cursor.moveToNext());
		cursor.close();
		//if the name has not been used => no set applies yet
		return -1;
	}
	
	public void addRule(String rule){
		this.openWriteableDataBase();
		ContentValues values = new ContentValues();
		values.put("rule", rule);
		this.myDataBase.insert("rule", null, values);
		this.openDataBase();
	}
	
	public void applyLearning(Result r, boolean approve) {
		try {
			// Calculate the values based on working memory
			ArrayList<Fact> reasoning = r.getReasoning();
			double[] decV = new double[Fact.totalFactTypes()];
			for(int i = 0; i < reasoning.size(); i++) {
				Fact f = reasoning.get(i);
				decV[Fact.FACT_TYPE.valueOf(f.getName()).ordinal()] =
						InferenceEngine.defuzzify(f);
			}
	
			// Fetch the current value for phone from db
			Cursor cursor = this.getResult(r.id);
			cursor.moveToFirst();
			ArrayList<Fact> allFacts = Fact.allFactTypes();
			double[] dbV = new double[Fact.totalFactTypes()];
			for(int i = 0; i < Fact.totalFactTypes(); i++) {
				String column = allFacts.get(i).getName() + "_value";
				dbV[i] = cursor.getDouble(cursor.getColumnIndex(column));
			}
			
			// Calculate new value for phone
			ContentValues cv = new ContentValues();
			for(int i = 0; i < Fact.totalFactTypes(); i++) {
				double diff = Math.abs(dbV[i] - decV[i]) * 0.10;
				if(approve) {
					if(dbV[i] > decV[i]) {
						double newV = dbV[i] - diff;
						cv.put(allFacts.get(i).getName() + "_value", newV);
					}
					else if(dbV[i] < decV[i]) {
						double newV = dbV[i] + diff;
						cv.put(allFacts.get(i).getName() + "_value", newV);
					}
					else {
						cv.put(allFacts.get(i).getName() + "_value", dbV[i]);
					}
				}
				else {
					if(dbV[i] > decV[i]) {
						double newV = dbV[i] + diff;
						cv.put(allFacts.get(i).getName() + "_value", newV);
					}
					else if(dbV[i] < decV[i]) {
						double newV = dbV[i] - diff;
						cv.put(allFacts.get(i).getName() + "_value", newV);
					}
					else {
						double newV = dbV[i] + diff;
						cv.put(allFacts.get(i).getName() + "_value", newV);
					}
				}
			}
			
			// Finally update the database
			myDataBase.update("data", cv, "_id = " + r.id, null);
			// Returns incorrect values due to triggers, but still works :/
			/*if(upRes > 1) {
				Log.e(this.getClass().getName(), "More than one row was affected by learning. There are probably multiple phones with the same id.");
			}
			else if(upRes < 1) {
				Log.e(this.getClass().getName(), "No rows were updated by learning.");
			}*/
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(), "Unable to apply learning");
		}
	}
		
	public void addSet(String name, int grouping, double... value){
		this.openWriteableDataBase();
		ContentValues values = new ContentValues();
		for(int i = 0; i<value.length; i++){
			values.put("\"set\"", name);
			values.put("min", i/value.length);
			values.put("max", i/value.length+1/value.length);
			values.put("value", value[i]);
			values.put("grouping", grouping);
			
		}
		this.myDataBase.insert("linguistic", null, values);
		this.openDataBase();
	}
	
	private String getQueryStringFromLingVars(ArrayList<Fact> facts) throws Exception {
		String query = "";
		for(Fact f : facts) {
			query += f.getName() + "_linguistic = '" +
					InferenceEngine.fuzzify(InferenceEngine.defuzzify(f)) + "'";
			if(f != facts.get(facts.size()-1)) {
				query += " AND ";
			}
		}
		return query;
	}
	
	private String getOrderByString(ArrayList<Fact> facts) throws Exception {
		String query = "";
		for(Fact f : facts) {
			query += "abs(" + f.getName() + "_value - " +
					InferenceEngine.defuzzify(f) + ")";
			if(f != facts.get(facts.size()-1)) {
				query += " + ";
			}
		}
		query += " ASC";
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
	
	/*private Cursor getResultsCursor(String whereClause) {
		return myDataBase.query("data", null, whereClause, null, null, null, null);
	}*/
	
	private Cursor getResultsCursor(String whereClause, String orderBy) {
		return myDataBase.query("data", null, whereClause, null, null, null, orderBy, "10");
	}
	
	private Cursor getResult(int id) {
		return myDataBase.query("data", null, "_id = " + id, null, null, null, null);
	}
	
	private Cursor getLinguisticTuplesCursor(String lingName) {
		return myDataBase.query("linguistic", linguisticColumns, "\"set\" = '" + lingName + "'",
				null, null, null, null);
	}
	
	private Cursor getLinguisticTuplesCursor(int grouping){
		return myDataBase.query("linguistic", linguisticColumns, grouping==-1?null:"grouping = "+grouping, null, null, null, null);
	}
	
	private boolean dbIsOpen() {
		return myDataBase.isOpen();
	}
	
	public boolean checkDatabase() {
		return super.checkDataBase();
	}
	
	public void openDataBase() throws SQLException {
		if(myDataBase == null || !myDataBase.isOpen()){
	    	super.openDataBase();
	    	int old = myDataBase.getVersion();
	    	Log.d(this.getClass().getName(), "Old db version: " + old);
	    	
	    	if(old < DATABASE_VERSION) {
	    		onUpgrade(myDataBase, old, DATABASE_VERSION);
	    	}
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
