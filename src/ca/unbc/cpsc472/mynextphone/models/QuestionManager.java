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
 * @author Daniel Finke
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
	
	public void recycle() {
		helper.close();
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
	
	public void submitAnswer(QuestionAnswer qa) {
		try {
			InferenceEngine e = getEngine();
			e.addFactsToMem(qa.getFacts());
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
	
	/*
	 * Save the state of the working memory and the remaining questions
	 */
	/*public void saveState(Bundle outState, int curQuestionId) {
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
		// Store the working memory fact ids & their truth values
		try {
			ArrayList<Fact> workingMemFacts = getEngine().getWorkingMem();
			int[] workingMemIds = new int[workingMemFacts.size()];
			boolean[] workingMemFlags = new boolean[workingMemFacts.size()];
			for(int i = 0; i < workingMemFacts.size(); i++) {
				workingMemIds[i] = workingMemFacts.get(i).getId();
				workingMemFlags[i] = workingMemFacts.get(i).getTruthFlag();
			}
			outState.putIntArray("workingMem", workingMemIds);
			outState.putBooleanArray("workingMemFlags", workingMemFlags);
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(), "Unable to save working mem state");
		}
	}*/
	
	/*
	 * Reload the working memory and list of remaining questions
	 */
	/*public void restoreState(Bundle savedState) {
		try {
			// Fetch all questions from the database
			ArrayList<Question> temp = helper.getQuestions();
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
		
		// Load up the working memory
		try {
			int[] workingMemIds = savedState.getIntArray("workingMem");
			boolean[] workingMemFlags = savedState.getBooleanArray("workingMemFlags");
			ArrayList<Fact> facts = new ArrayList<Fact>();
			for(int i = 0; i < workingMemIds.length; i++) {
				// Fetch the facts from the db for their ids
				facts.add(helper.getFactForFactId(workingMemIds[i], workingMemFlags[i] ? 1 : 0));
			}
			getEngine().addFactsToMem(facts);
			getEngine().updateMem();
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(), "Unable to restore working mem from saved state");
		}
	}*/
	
	/*
	 * Returns true if the question id is in the list of ids
	 */
	/*private boolean isQuestionInBundle(Question q, int[] questionIds) {
		int qId = q.getId();
		for(int i = 0; i < questionIds.length; i++) {
			if(qId == questionIds[i]) {
				return true;
			}
		}
		
		return false;
	}*/
}
