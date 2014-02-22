package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.util.Log;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

public class InferenceEngine {
	public static enum CompletionMode {SINGLE, EXHAUSTIVE};
	
	private CompletionMode completionMode;
	HashSet<Fact> workingMem;
	ArrayList<Rule> rules;
	PhoneDataBaseHelper helper;
	
	public InferenceEngine(PhoneDataBaseHelper dbHelper) {
		completionMode = InferenceEngine.CompletionMode.SINGLE;
		workingMem = new HashSet<Fact>();
		helper = dbHelper;
		try {
			rules = helper.getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public InferenceEngine(ArrayList<Fact> initialMem, PhoneDataBaseHelper dbHelper) {
		completionMode = InferenceEngine.CompletionMode.SINGLE;
		workingMem = new HashSet<Fact>();
		workingMem.addAll(initialMem);
		helper = dbHelper;
		try {
			rules = helper.getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public void setCompletionMode(CompletionMode compMode) {
		completionMode = compMode;
	}
	
	public ArrayList<Fact> getWorkingMem() {
		ArrayList<Fact> mem = new ArrayList<Fact>();
		mem.addAll(workingMem);
		return mem;
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
					if(completionMode == InferenceEngine.CompletionMode.SINGLE && this.memContainsConclusiveFact()) {
						return;
					}
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
			Log.d(this.getClass().getName(), "Fact in memory: " + next.getName() + " (" + next.getTruthFlag() + ")");
		}
	}
	
	/*
	 * Checks if the rule's conditions are met, and if so, adds the deduced facts
	 * to the working memory and discards the rule for future iterations
	 */
	private boolean evaluateRule(Rule rule) throws Exception {
		// Load up the conditions
		HashSet<Fact> leftSide = rule.getLeftSide();
		Iterator<Fact> leftIter = leftSide.iterator();
		while(leftIter.hasNext()) {
			Fact condFact = leftIter.next();
			if(!workingMem.contains(condFact)) {
				return false;
			}
		}
		
		// Add the new facts
		HashSet<Fact> rightSide = rule.getRightSide();
		workingMem.addAll(rightSide);
		
		// Remove the used rule from the rule set
		rules.remove(rule);
		
		return true;
	}
	
	public void addFactToMem(Fact f) {
		workingMem.add(f);
	}
	
	public void addFactsToMem(ArrayList<Fact> facts) {
		workingMem.addAll(facts);
	}
	
	/*
	 * Returns true if the memory already contains a fact considered
	 * an "end result"
	 */
	private boolean memContainsConclusiveFact() {
		Iterator<Fact> iter = workingMem.iterator();
		while(iter.hasNext()) {
			if(iter.next().getFactType() == Fact.FactType.CONCLUSIVE) {
				return true;
			}
		}
		return false;
	}
}
