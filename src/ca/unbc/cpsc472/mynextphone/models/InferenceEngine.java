package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

public class InferenceEngine {
	private static enum LING_VAR {VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH};
	private ArrayList<Fact> workingMem;
	private ArrayList<Rule> rules;
	private PhoneDataBaseHelper helper;
	
	public static double defuzzify(String lingVar) {
		double sVal = LING_VAR.valueOf(lingVar).ordinal() * 0.2;
		// Perhaps one day we would have non-boxy ranges for our linguistic vars
		// Only then, could the hero change this for loop
		double num = 0;
		for(int i = 0; i < 2; i++) {
			num += (sVal + i * 0.2) * 1; // Because the value is always 1 in our boxy situation
		}
		double denom = 0;
		for(int i = 0; i < 2; i++) {
			denom += 1; // Again, the value is always 1
		}
		return num / denom;
	}
	
	public static String fuzzify(double val) {
		if(val >= 0 && val <= 0.2) {
			return "VERY_LOW";
		}
		else if(val > 0.2 && val <= 0.4) {
			return "LOW";
		}
		else if(val > 0.4 && val <= 0.6) {
			return "MEDIUM";
		}
		else if(val > 0.6 && val <= 0.8) {
			return "HIGH";
		}
		else if(val > 0.8 && val <= 1) {
			return "VERY_HIGH";
		}
		
		return null;
	}
	
	public InferenceEngine(PhoneDataBaseHelper dbHelper) {
		workingMem = new ArrayList<Fact>();
		helper = dbHelper;
		try {
			rules = helper.getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public InferenceEngine(ArrayList<Fact> initialMem, PhoneDataBaseHelper dbHelper) {
		workingMem = new ArrayList<Fact>();
		addFactsToMem(initialMem);
		helper = dbHelper;
		try {
			rules = helper.getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public ArrayList<Fact> getWorkingMem() {
		return workingMem;
	}
	
	public ArrayList<Result> getResultsForWorkingMem() {
		// First calculate the fuzzy sets of the facts in the memory
		ArrayList<Fact> allFacts = Fact.allFactTypes();
		ArrayList<Fact> resultLingVars = new ArrayList<Fact>();
		for(Fact f : workingMem) {
			// Ignore those without any linguistic vars set, or those that
			// are not part of the phone linguistic variables
			if(f.getLinguisticValueCount() == 0 ||
					!allFacts.contains(f)) {
				continue;
			}
			
			double avg = 0;
			for(int i = 0; i < f.getLinguisticValueCount(); i++) {
				avg += defuzzify(f.getLinguisticValues().get(i));
			}
			avg /= f.getLinguisticValueCount();
			Fact res = new Fact(f.getName());
			res.addLinguisticValue(fuzzify(avg));
			resultLingVars.add(res);
		}
		
		/*if(results.isEmpty()) {
			return getNearestResults();
		}*/
		try {
			return helper.getResultsWithFacts(resultLingVars);
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to get inferred results");
			return null;
		}
	}
	
	/* TODO Get closest results in case of no matching ones
	 * Find the collection of the closest results that match to what
	 * the user chose for the questions
	 */
	/*private ArrayList<Result> getNearestResults() {
		ArrayList<Rule> resultRules;
		try {
			resultRules = helper.getResultRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to get nearest results (rules step)");
			return null;
		}
		
		// Calculate all the rule's score based on current working mem
		for(int i = 0; i < resultRules.size(); i++) {
			Rule r = resultRules.get(i);
			r.setClosenessScore(resultScore(r));
		}
		// Sort them by their scores
		Collections.sort(resultRules);
		
		// Return the list of best results
		// CURRENTLY ONLY CONSIDERING THE LAST ONE EVEN IF THERE ARE TIES!
		ArrayList<Result> results = new ArrayList<Result>();
		Rule best = resultRules.get(resultRules.size()-1);
		HashSet<Fact> producedFacts = best.getRightSide();
		Iterator<Fact> iter = producedFacts.iterator();
		try {
			while(iter.hasNext()) {
				Fact f = iter.next();
				Result res = helper.getResultForFactId(f.getId(), workingMem);
				results.add(res);
			}
		}
		catch(Exception e) {
			Log.e(this.getClass().getName(), "Unable to get nearest results (results step)");
			return null;
		}
		return results;
	}*/
	
	public void addFactToMem(Fact f) {
		if(workingMem.contains(f)) {
			workingMem.get(workingMem.indexOf(f)).addLinguisticValues(f.getLinguisticValues());
		}
		else {
			workingMem.add(f);
		}
	}
	
	public void addFactsToMem(ArrayList<Fact> facts) {
		for(Fact f : facts) {
			addFactToMem(f);
		}
	}
	
	/*
	 * Perform as much inference as possible based on the current set of rules
	 * and the working memory. After return, user can continue to answer questions
	 * and updateMem again later.
	 */
	public void updateMem() {
		int i = 0;
		while(i < rules.size()) {
			try {
				// Attempt to evaluate rule and continue to next one
				if(evaluateRule(rules.get(i))) {
					i = 0;
				}
				else {
					i++;
				}
			// Don't die if rules don't work for some reason
			} catch (Exception e) {
				Log.e(this.getClass().getName(), "Unable to evaluate rule with rule id: " + rules.get(i).getRuleId());
			}
		}
		
		Iterator<Fact> iter = workingMem.iterator();
		while(iter.hasNext()) {
			Fact next = iter.next();
			Log.d(this.getClass().getName(), "Fact in memory: " + next.toString());
		}
	}
	
	/*
	 * Checks if the rule's conditions are met, and if so, adds the deduced facts
	 * to the working memory and discards the rule for future iterations
	 */
	private boolean evaluateRule(Rule rule) throws Exception {
		// Load up the conditions
		ArrayList<Fact> leftSide = rule.getLeftSide();
		Iterator<Fact> leftIter = leftSide.iterator();
		while(leftIter.hasNext()) {
			Fact condFact = leftIter.next();
			boolean condMet = false;
			for(Fact wmf : workingMem) {
				condMet = condMet || wmf.meetsCriteria(condFact);
			}
			if(!condMet) {
				return false;
			}
		}
		
		// Add the new facts
		ArrayList<Fact> rightSide = rule.getRightSide();
		addFactsToMem(rightSide);
		
		// Remove the used rule from the rule set
		rules.remove(rule);
		
		return true;
	}
	
	/*
	 * Returns true if enough information has been collected in the working memory
	 * to make a decision
	 */
	public boolean isMemSufficientForDecision() {
		ArrayList<Fact> allTypes = Fact.allFactTypes();
		for(Fact type : allTypes) {
			if(!workingMem.contains(type) ||
					workingMem.get(workingMem.indexOf(type)).getLinguisticValueCount() < 2) {
				return false;
			}
		}

		return true;
	}
	
	/*
	 * Calculates a "closeness" score for working mem and a result-producing
	 * rule. Used to determine the best match to the user's choices
	 */
	/*private int resultScore(Rule r) {
		int score = 0;
		HashSet<Fact> facts = r.getLeftSide();
		Iterator<Fact> iter = facts.iterator();
		while(iter.hasNext()) {
			Fact f = iter.next();
			// Contains the fact, give it a +1
			if(workingMem.contains(f)) {
				score++;
			}
			else {
				// Contains the negation of the fact, give it a -1
				f.toggleTruthFlag();
				if(workingMem.contains(f)) {
					score--;
				}
			}
			// Doesn't have the fact at all, give it a 0 (no change)
		}
		return score;
	}*/
}
