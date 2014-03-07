package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

/**
 * Mainly used for generating Question objects for testing purposes; this class
 * can and probably should be re-purposed to grab questions from out Question
 * table in the database in final versions of the app.
 * 
 * @author Andrew J Toms II
 */
public class QuestionManager {
	private ArrayList<Question> questions;
	private InferenceEngine engine;
	private PhoneDataBaseHelper helper;
	
	public QuestionManager(Context c) {
		this.helper = new PhoneDataBaseHelper(c);
		this.helper.openDataBase();
	}
	
	private InferenceEngine getEngine() throws Exception {
		if(engine != null) {
			return engine;
		}
		if(this.helper == null) {
			throw new Exception();
		}
		engine = new InferenceEngine(this.helper);
		return engine;
	}
	
	/**
	 * Generates a random question to display in this QuestionActivity.
	 * 
	 * @return A randomly generated Question.
	 */
	public Question getQuestion() {
		if(questions == null) {
			try {
				questions = this.helper.getQuestions();
			} catch (Exception e) {
				Log.e(this.getClass().getName(), "Unable to load questions from database");
				return null;
			}
		}
		
		Random r = new Random();
		int q;
		try{
			q = r.nextInt(questions.size());
		} catch(IllegalArgumentException e){
			return null;
		}
		// Includes the answers already!
		return questions.remove(q);
	}
	
	public void submitAnswer(QuestionAnswer qa) {
		try {
			InferenceEngine e = getEngine();
			e.addFactsToMem(this.helper.getFactsForAnswerId(qa.getId()));
			e.updateMem();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to submit answer");
		}
	}
	
	public ArrayList<Result> getResults() {
		try {
			return getEngine().getResultsForWorkingMem();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "DB helper not instantiated");
		}
		return null;
	}
}
