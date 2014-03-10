package ca.unbc.cpsc472.mynextphone.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.Result;
import ca.unbc.cpsc472.mynextphone.models.Rule;

public class PhoneDataBaseHelper extends DataBaseHelper {
    private static String[] ruleColumns = {"_id", "rule_id", "fact_id", "left_right", "truth_flag"};
    private static String[] questionColumns = {"_id", "question", "type"};
    private static String[] answerColumns = {"_id", "question_id", "answer"};
    private static String[] factColumns = {"_id", "name", "result_id"};
    private static String[] answerFactColumns = {"_id", "answer_id", "fact_id", "truth_flag"};
    private static String[] resultColumns = {"_id", "name", "image_file"};

	public PhoneDataBaseHelper(Context context) {
		super(context);
		DB_PATH = context.getFilesDir().getParent() + "/databases/";
		DB_NAME = "db.sqlite";
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
		
		// Rules are stored by their ruleId attribute in this array
		SparseArray<Rule> rules = new SparseArray<Rule>();
		Cursor cursor = getRulesCursor();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			// Try to find if this rule has already been loaded, if so, append new facts
			int ruleId = cursor.getInt(cursor.getColumnIndex(ruleColumns[1]));
			Rule r = rules.get(ruleId);
			if(r == null) {
				r = new Rule(ruleId);
			}
			
			// The new fact to be added to the rule in memory
			Fact f = this.getFactForFactId(
					cursor.getInt(cursor.getColumnIndex(ruleColumns[2])),
					cursor.getInt(cursor.getColumnIndex(ruleColumns[4]))
			);
			
			// Add to the left or right
			int leftRight = cursor.getInt(cursor.getColumnIndex(ruleColumns[3]));
			if(leftRight == Rule.RuleSide.LEFT.ordinal()) {
				r.addFactCondition(f);
			}
			else {
				r.addFactDeduction(f);
			}
			
			rules.append(ruleId, r);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return this.sparseArrayToArrayList(rules);
	}
	
	/*
	 * Returns only rules which can be inferred to produce a result
	 * Used for the "closeness" check at the end of the questions, when
	 * the user might not have got /exactly/ a result in the working mem
	 */
	public ArrayList<Rule> getResultRules() throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		SparseArray<Rule> rules = new SparseArray<Rule>();
		Cursor cursor = getResultRulesCursor();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			// Try to find if this rule has already been loaded, if so, append new facts
			int ruleId = cursor.getInt(cursor.getColumnIndex(ruleColumns[1]));
			Rule r = rules.get(ruleId);
			if(r == null) {
				r = new Rule(ruleId);
			}
			
			// The new fact to be added to the rule in memory
			Fact f = this.getFactForFactId(
					cursor.getInt(cursor.getColumnIndex(ruleColumns[2])),
					cursor.getInt(cursor.getColumnIndex(ruleColumns[4]))
			);
			
			// Add to the left or right
			int leftRight = cursor.getInt(cursor.getColumnIndex(ruleColumns[3]));
			if(leftRight == Rule.RuleSide.LEFT.ordinal()) {
				r.addFactCondition(f);
			}
			else {
				r.addFactDeduction(f);
			}
			
			rules.append(ruleId, r);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return this.sparseArrayToArrayList(rules);
	}
	
	private ArrayList<Rule> sparseArrayToArrayList(SparseArray<Rule> sparse) {
		ArrayList<Rule> res = new ArrayList<Rule>();
		for(int i = 0; i < sparse.size(); i++) {
			res.add(sparse.get(sparse.keyAt(i)));
		}
		return res;
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
	
	public Fact getFactForFactId(int factId, int truthFlag) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		Cursor cursor = getFactsCursorForFactId(factId);
		cursor.moveToFirst();
		// Handle result_id being null
		int resultId = cursor.isNull(cursor.getColumnIndex(factColumns[2])) ? -1 :
			cursor.getInt(cursor.getColumnIndex(factColumns[2]));
		Fact f = new Fact(
				cursor.getInt(cursor.getColumnIndex(factColumns[0])),
				cursor.getString(cursor.getColumnIndex(factColumns[1])),
				truthFlag,
				resultId
		);
		cursor.close();
		return f;
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
					type
			));
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return answers;
	}
	
	public ArrayList<Fact> getFactsForAnswerId(int answerId) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}

		ArrayList<Fact> facts = new ArrayList<Fact>();
		Cursor cursor = getAnswerFactsCursorForAnswerId(answerId);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			facts.add(this.getFactForFactId(
					cursor.getInt(cursor.getColumnIndex(answerFactColumns[2])),
					cursor.getInt(cursor.getColumnIndex(answerFactColumns[3]))
			));
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return facts;
	}
	
	public Result getResultForFactId(int factId, HashSet<Fact> workingMem) throws Exception {
		if(!dbIsOpen()) {
			throw new Exception();
		}
		
		// THIS SHOULD BE CHANGED TO DEALING WITH THE ANSWERS
		// NOT THE FACTS FROM THE WORKING MEM
		Fact f = getFactForFactId(factId, 1); // Might have to reconsider the always-true
		ArrayList<Fact> temp = new ArrayList<Fact>();
		temp.addAll(workingMem);
		Cursor cursor = getResultsCursorForResultId(f.getResultId());
		cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			Result r = new Result(
					cursor.getString(cursor.getColumnIndex(resultColumns[1])),
					cursor.getString(cursor.getColumnIndex(resultColumns[2])),
					temp
			);
			cursor.close();
			return r;
		}
		cursor.close();
		
		throw new Exception();
	}
	
	private Cursor getRulesCursor() {
		return myDataBase.query("rule", ruleColumns, null, null, null, null, null);
	}
	
	private Cursor getResultRulesCursor() {
		return myDataBase.rawQuery("SELECT * FROM rule WHERE rule_id IN (SELECT a.rule_id FROM rule a INNER JOIN fact b ON a.fact_id = b._id WHERE b.result_id IS NOT NULL);", null);
	}
	
	private Cursor getQuestionsCursor() {
		return myDataBase.query("question", questionColumns, null, null, null, null, null);
	}
	
	private Cursor getFactsCursorForFactId(int factId) {
		return myDataBase.query("fact", factColumns, "_id = " + factId, null, null,
				null, null);
	}
	
	private Cursor getAnswersCursorForQuestionId(int questionId) {
		return myDataBase.query("answer", answerColumns, "question_id = " + questionId, null,
				null, null, null);
	}
	
	private Cursor getAnswerFactsCursorForAnswerId(int answerId) {
		return myDataBase.query("answer_fact", answerFactColumns,
				"answer_id = " + answerId,
				null, null, null, null);
	}
	
	private Cursor getResultsCursorForResultId(int resultId) {
		return myDataBase.query("result", resultColumns,
				"_id = " + resultId,
				null, null, null, null);
	}
	
	private boolean dbIsOpen() {
		return myDataBase.isOpen();
	}

	public void openWriteableDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
	
	public void openDataBase() {
		if(checkDataBase()) {
			super.openDataBase();
		}
		else {
			try {
				createDataBase();
				openWriteableDataBase();
				myDataBase.close();
				super.openDataBase();
			} catch(IOException ex) {
				Log.e(this.getClass().getName(), "Unable to create SQLite database");
			}
		}
	}
}
