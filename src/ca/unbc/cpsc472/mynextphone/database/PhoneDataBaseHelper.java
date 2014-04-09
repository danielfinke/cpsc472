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
    
    /*
     * Fetches a singleton instance of the PhoneDatabaseHelper
     * 
     * @param context	a context to tie the instance to
     * @return			the singleton PhoneDataBaseHelper instance
     */
    public static PhoneDataBaseHelper getInstance(Context context) {
    	if(helper == null) {
    		helper = new PhoneDataBaseHelper(context);
    	}
    	return helper;
    }

    /*
     * PhoneDataBaseHelper constructor that automatically tries to create
     * the SQLite database and/or copy it from assets as necessary
     * 
     * @param context	a context to tie the instance to
     * @return			a new PhoneDataBaseHelper tied to the app's SQLite database
     */
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
	 * Fetches all available questions from the database.
	 * 
	 * @return		ArrayList of all questions available for the survey
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
	
	/*
	 * Fetches a count of the total number of questions available to be asked
	 * 
	 * @return		the total number of questions
	 */
	public long getQuestionCount() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		return DatabaseUtils.queryNumEntries(myDataBase, "question");
	}
	
	/*
	 * Produces a list of answers tied to a specific question
	 * 
	 * @param questionId		the id # representing the question
	 * @param type				the question type (for use in QuestionAnswer constructor)
	 * @return					an ArrayList of the potential answers for the question
	 */
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
	 * 
	 * @return		an ArrayList of every rule in the database for inference
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
	
	/*
	 * Gets a list of potential devices based on a list of facts.
	 * 
	 * @param facts			list of query facts
	 * @param reasoning		justification for the results (for their constructors)
	 * @return				an ArrayList of result devices
	 */
	public ArrayList<Result> getResultsWithFacts(ArrayList<Fact> facts, ArrayList<Fact> reasoning) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Result> results = new ArrayList<Result>();
		Cursor cursor = getResultsCursor(getQueryStringFromLingVars(facts), getOrderByString(facts));
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			
			// Create a new result instance
			Result r = new Result(
					id,
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("description")),
					//reasoning
					facts
			);
			
			// Loop over the set of images for the device, and add them to the result
			ArrayList<String> imgPaths = new ArrayList<String>();
			Cursor imgCursor = getPhoneImageCursor(id);
			imgCursor.moveToFirst();
			while(!imgCursor.isAfterLast()) {
				imgPaths.add(imgCursor.getString(imgCursor.getColumnIndex("url")));
				imgPaths.add(imgCursor.getString(imgCursor.getColumnIndex("big_url")));
				imgCursor.moveToNext();
			}
			r.setImgPaths(imgPaths);
			
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
	
	/*
	 * In case of no results for the fact set, returns the closest results instead.
	 * 
	 * @param facts			list of query facts
	 * @param reasoning		justification for the results (for their constructors)
	 * @return				an ArrayList of result devices		
	 */
	public ArrayList<Result> getNearestResults(ArrayList<Fact> facts, ArrayList<Fact> reasoning) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Result> results = new ArrayList<Result>();
		Cursor cursor = getResultsCursor(null, getOrderByString(facts));
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			
			// Create a new result instance
			Result r = new Result(
					id,
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("description")),
					//reasoning
					facts
			);
			
			// Loop over the set of images for the device, and add them to the result
			ArrayList<String> imgPaths = new ArrayList<String>();
			Cursor imgCursor = getPhoneImageCursor(id);
			imgCursor.moveToFirst();
			while(!imgCursor.isAfterLast()) {
				imgPaths.add(imgCursor.getString(imgCursor.getColumnIndex("url")));
				imgPaths.add(imgCursor.getString(imgCursor.getColumnIndex("big_url")));
				imgCursor.moveToNext();
			}
			r.setImgPaths(imgPaths);
			
			results.add(r);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return results;
	}
	
	/*
	 * Fetches all the tuples that cumulatively produce a set.
	 * 
	 * @tuple lingName		name of linguistic variable of the set
	 * @return				an ArrayList of the tuples that represent the set
	 */
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
	
	/*
	 * Adds a new rule to the rule table in the database.
	 * 
	 * @param rule		rule in string format
	 */
	public void addRule(String rule){
		this.openWriteableDataBase();
		ContentValues values = new ContentValues();
		values.put("rule", rule);
		this.myDataBase.insert("rule", null, values);
		this.openDataBase();
	}
	
	/*
	 * Modifies values for a result device to more adequately represent
	 * the user's preferences
	 * 
	 * @param r			the result device to be modified
	 * @param approve	whether the values should become closer (true) or further (false)
	 */
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
			// Values either become closer by 10%
			// or further away by 10%
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
	
	/*
	 * Add a new set into the database, for use in custom rules
	 * 
	 * @param name		the name of the new set
	 * @param grouping	which collection of sets the set should belong to
	 * @param value		one or more values to apply uniformly to the set distribution
	 */
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
	
	private Cursor getPhoneImageCursor(int id) {
		return myDataBase.query("image", null, "phone_id = " + id, null, null, null, null);
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
