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
	
	public void updateMem() {
		int i = 0;
		while(i < rules.size()) {
			try {
				if(evaluateRule(rules.get(i))) {
					if(completionMode == InferenceEngine.CompletionMode.SINGLE && this.memContainsConclusiveFact()) {
						return;
					}
					i = 0;
				}
				else {
					i++;
				}
			} catch (Exception e) {
				Log.e(this.getClass().getName(), "Unable to evaluate rule with rule id: " + rules.get(i).getId());
			}
		}
	}
	
	private boolean evaluateRule(Rule rule) throws Exception {
		ArrayList<Condition> conds = helper.getConditionsForRuleId(rule.getId());
		for(int i = 0; i < conds.size(); i++) {
			Fact condFact = conds.get(i).getFact();
			if(!workingMem.contains(condFact)) {
				return false;
			}
		}
		ArrayList<Answer> newFacts = helper.getAnswersForRuleId(rule.getId());
		this.addFactsToMem(newFacts);
		
		rules.remove(rule);
		
		return true;
	}
	
	public void addFactToMem(Fact f) {
		workingMem.add(f);
	}
	
	private void addFactsToMem(ArrayList<Answer> answerFacts) throws Exception {
		for(int i = 0; i < answerFacts.size(); i++) {
			this.addFactToMem(answerFacts.get(i).getFact());
		}
	}
	
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
