package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

/**
 * Mainly used for generating Question objects for testing purposes; this class
 * can and probably should be re-purposed to grab questions from out Question
 * table in the database in final versions of the app.
 * 
 * @author Daniel Finke
 */
public class QuestionManager {
	private ArrayList<Question> questions;
	private InferenceEngine engine;
	
	/*
	 * Creates a QuestionManager for a given context
	 * 
	 * @param c		linked context
	 * @return		new QuestionManager
	 */
	public QuestionManager(Context c) {
		PhoneDataBaseHelper.getInstance(c).openDataBase();
	}
	
	/*
	 * Safely recycles this object
	 */
	public void recycle() {
		PhoneDataBaseHelper.getInstance(null).close();
	}
	
	/*
	 * Save the state of the working memory and the remaining questions
	 * 
	 * @param outState			the bundle to save state into
	 * @param curQuestionId		the question the app is currently on
	 */
	public void saveState(Bundle outState, int curQuestionId) {
		// Store the question ids
		int[] questionIds = new int[questions.size()];
		int questionId;
		for(int i = 0; i < questions.size(); i++) {
			questionId = questions.get(i).getId();
			if(questionId != curQuestionId) {
				questionIds[i] = questionId;
			}
		}
		outState.putIntArray("questions", questionIds);
		
		// Save the state of the inference engine
		try {
			getEngine().saveState(outState, "inference_");
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(), "Unable to save working inference engine state");
		}
	}
	
	/*
	 * Reload the working memory and list of remaining questions
	 * 
	 * @param savedState		the bundle to restore state from
	 */
	public void restoreState(Bundle savedState) {
		try {
			// Fetch all questions from the database
			ArrayList<Question> temp = PhoneDataBaseHelper.getInstance(null).getQuestions();
			questions = new ArrayList<Question>();
			// Fetch the question ids that should be restored
			int[] questionIds = savedState.getIntArray("questions");
			Question q;
			for(int i = 0; i < temp.size(); i++) {
				// Only keep the questions from the database that are not yet answered
				q = temp.get(i);
				if(isQuestionInBundle(q, questionIds)) {
					questions.add(q);
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to restore questions from saved state");
		}
		
		// Load up the previous inference engine state
		try {
			getEngine().restoreState(savedState, "inference_");
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(),
					"Unable to restore working inference engine from saved state: "
					+ ex.getMessage());
		}
	}
	
	private InferenceEngine getEngine() {
		if(engine != null) {
			return engine;
		}
		engine = new InferenceEngine();
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
				questions = PhoneDataBaseHelper.getInstance(null).getQuestions();
			} catch (Exception e) {
				Log.e(this.getClass().getName(), "Unable to load questions from database");
				return null;
			}
		}
		
		// Quit if the InferenceEngine says it does not need more info
		try {
			if(getEngine().isMemSufficientForDecision()) {
				return null;
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "DB helper not instantiated");
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
	
	/*
	 * Request results from the inference engine
	 * 
	 * @return		an ArrayList of Result objects (recommended devices for user)
	 */
	public ArrayList<Result> getResults() {
		try {
			return getEngine().getResultsForWorkingMem();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "DB helper not instantiated");
		}
		return null;
	}
	
	/*
	 * Submit an answer to the inference engine and re-evaluate inference
	 * 
	 * @param qa		the answer chosen for the question
	 */
	public void submitAnswer(QuestionAnswer qa) {
		try {
			InferenceEngine e = getEngine();
			e.addFactsToMem(qa.getFacts());
			e.updateMem();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to submit answer");
		}
	}
	
	/*
	 * Returns true if the question id is in the list of ids
	 * 
	 * @param q				question to check for
	 * @param questionIds	all question ids to check
	 * @return				true if the question has an id in the list of ids supplied
	 */
	private boolean isQuestionInBundle(Question q, int[] questionIds) {
		int qId = q.getId();
		for(int i = 0; i < questionIds.length; i++) {
			if(qId == questionIds[i]) {
				return true;
			}
		}
		
		return false;
	}
}
