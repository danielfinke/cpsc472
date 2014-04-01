package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

public class InferenceEngine {
	private ArrayList<Fact> workingMem;
	private ArrayList<Rule> rules;
	
	public static double defuzzify(Fact f) throws Exception {
		// Get min value of first tuple
		double tMin = (Double)f.getTuples().get(0).getObject(0);
		double tMinVal = (Double)f.getTuples().get(0).getObject(2);
		double num = tMin * tMinVal;
		double denom = tMinVal;
		
		// Begin adding max value of all tuples
		for(int i = 0; i < f.getTuples().size(); i++) {
			double tMax = (Double)f.getTuples().get(i).getObject(0);
			double tMaxVal = (Double)f.getTuples().get(i).getObject(2);
			num += tMax * tMaxVal;
			denom += tMaxVal;
			if(tMaxVal != 0) {
				double tMax2 = (Double)f.getTuples().get(i).getObject(1);
				num += tMax2 * tMaxVal;
				denom += tMaxVal;
			}
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
	
	public InferenceEngine() {
		workingMem = new ArrayList<Fact>();
		try {
			rules = PhoneDataBaseHelper.getInstance(null).getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public InferenceEngine(ArrayList<Fact> initialMem) {
		workingMem = new ArrayList<Fact>();
		addFactsToMem(initialMem);
		try {
			rules = PhoneDataBaseHelper.getInstance(null).getRules();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to fetch rules from database");
		}
	}
	
	public void saveState(Bundle bundle, String bundlePrefix) {
		// Store the working memory
		ArrayList<String> memKeys = new ArrayList<String>();
		for(Fact f : getWorkingMem()) {
			f.saveState(bundle, bundlePrefix + "mem" + f.getName() + "_");
			memKeys.add(f.getName());
		}
		bundle.putStringArrayList(bundlePrefix + "memKeys", memKeys);
		
		// Store the un-evaluated rules
		int[] ruleKeys = new int[rules.size()];
		for(int i = 0; i < rules.size(); i++) {
			Rule r = rules.get(i);
			// TODO daniel save rule state
			//r.saveState(bundle, bundlePrefix + "rule" + r.getRuleId() + "_");
			ruleKeys[i] = r.getRuleId();
		}
		bundle.putIntArray(bundlePrefix + "ruleKeys", ruleKeys);
	}
	
	public void restoreState(Bundle bundle, String bundlePrefix) {
		// First load the working memory
		ArrayList<Fact> facts = new ArrayList<Fact>();
		ArrayList<String> memKeys = bundle.getStringArrayList(bundlePrefix + "memKeys");
		for(String key : memKeys) {
			facts.add(new Fact(bundle, bundlePrefix + "mem" + key + "_"));
		}
		this.workingMem = facts;
		
		// Load previously un-evaluated rules
		ArrayList<Rule> rules = new ArrayList<Rule>();
		int[] ruleKeys = bundle.getIntArray(bundlePrefix + "ruleKeys");
		for(int i = 0; i < ruleKeys.length; i++) {
			rules.add(new Rule(bundle, bundlePrefix + "rule" + ruleKeys[i] + "_"));
		}
		this.rules = rules;
	}
	
	public ArrayList<Fact> getWorkingMem() {
		return workingMem;
	}
	
	public Fact getFact(String lingVar) {
		for(int i = 0; i < workingMem.size(); i++) {
			if(workingMem.get(i).getName().equals(lingVar)) {
				return workingMem.get(i);
			}
		}
		return null;
	}
	
	public ArrayList<Result> getResultsForWorkingMem() {
		try {
			ArrayList<Fact> lookups = new ArrayList<Fact>();
			for(Fact f : workingMem) {
				// Not a part of our where clause
				if(!Fact.isLinguisticVariable(f.getName())) {
					continue;
				}
				
				double dVal = defuzzify(f);
				String resSet = fuzzify(dVal);
				
				Fact resF = new Fact(f.getName());
				resF.addTuples(PhoneDataBaseHelper.getInstance(null).getLinguisticTuples(resSet));
				lookups.add(resF);
			}
			
			ArrayList<Result> results = PhoneDataBaseHelper.getInstance(null).getResultsWithFacts(lookups, workingMem);
		
			if(results.isEmpty()) {
				return getNearestResults(lookups);
			}
			
			return results;
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to get inferred results");
		}
		return null;
	}
	
	/* Find the collection of the closest results that match to what
	 * the user chose for the questions
	 */
	private ArrayList<Result> getNearestResults(ArrayList<Fact> facts) {
		try {
			return PhoneDataBaseHelper.getInstance(null).getNearestResults(facts, workingMem);
		}
		catch(Exception e) {
			Log.e(this.getClass().getName(), "Unable to get nearest results");
		}
		return null;
	}
	
	public void addFactToMem(Fact f) {
		Fact oldF = getFact(f.getName());
		if(oldF != null) {
			workingMem.set(workingMem.indexOf(oldF), calculateAggregate(f, oldF));
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
				if(isAntecedentInMem(rules.get(i))) {
					if(evaluateRule(rules.get(i))) {
						i = 0;
						continue;
					}
				}
				i++;
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
		// Calculate left side (with ANDs)
		Fact comp = calculateCompoundLeft(rule.getLeftSide());
		
		// No point in going on
		if(comp.isEmptySet()) {
			return false;
		}
		
		// Update working memory
		for(Fact f : rule.getRightSide()) {
			// Create implication relation matrix
			double[][] implMtx = calculateImplOperMtx(comp, f);
			// Apply just one fact since we compounded earlier
			Fact applied = applyFactToMtx(getFact(comp.getName()), implMtx, f);
			if(!applied.isEmptySet()) {
				addFactToMem(applied);
			}
		}
		
		// Remove the used rule from the rule set
		rules.remove(rule);
		
		return true;
	}
	
	private Fact calculateCompoundLeft(ArrayList<Fact> left) {
		Fact wFact = left.get(0);
		if(left.size() == 1) {
			return wFact;
		}
		for(int i = 1; i < left.size(); i++) {
			double[][] mtx = calculateImplOperMtx(wFact, left.get(i));
			wFact = applyFactToMtx(getFact(wFact.getName()), mtx, left.get(i));
		}
		return wFact;
	}
	
	// Assumption that tuple dimensions and min/maxs are equivalent
	private Fact calculateAggregate(Fact f1, Fact f2) {
		Fact ret = new Fact(f1.getName());
		for(int i = 0; i < f1.getTupleCount(); i++) {
			Tuple f1T = f1.getTuples().get(i);
			Tuple f2T = f2.getTuples().get(i);
			double min = (Double)f1T.getObject(0);
			double max = (Double)f1T.getObject(1);
			double val = Math.max((Double)f1T.getObject(2), (Double)f2T.getObject(2));
			Tuple t = new Tuple(new Object[] {min, max, val});
			ret.addTuple(t);
		}
		return ret;
	}
	
	private double[][] calculateImplOperMtx(Fact f1, Fact f2) {
		double[][] res = new double[f1.getTupleCount()][f2.getTupleCount()];
		for(int i = 0; i < f1.getTupleCount(); i++) {
			for(int j = 0; j < f2.getTupleCount(); j++) {
				double f1Val = (Double)f1.getTuples().get(i).getObject(2);
				double f2Val = (Double)f2.getTuples().get(j).getObject(2);
				res[i][j] = Math.min(f1Val, f2Val);
			}
		}
		return res;
	}
	
	private Fact applyFactToMtx(Fact f, double[][] mtx, Fact resultType) {
		Fact res = new Fact(resultType.getName());
		for(int i = 0; i < mtx[0].length; i++) {
			double max = 0;
			for(int j = 0; j < mtx.length; j++) {
				Tuple t = f.getTuples().get(j);
				max = Math.max(Math.min((Double)t.getObject(2), mtx[j][i]), max);
			}
			Tuple resultT = resultType.getTuples().get(i);
			double resultTMinVal = (Double)resultT.getObject(0);
			double resultTMaxVal = (Double)resultT.getObject(1);
			Tuple keep = new Tuple(new Object[] {
					resultTMinVal,
					resultTMaxVal,
					max
			});
			res.addTuple(keep);
		}
		return res;
	}
	
	/*
	 * Returns true if enough information has been collected in the working memory
	 * to make a decision
	 */
	// TODO daniel improve with freq/scaling in fact merges
	public boolean isMemSufficientForDecision() {
		ArrayList<Fact> allTypes = Fact.allFactTypes();
		for(Fact type : allTypes) {
			boolean found = false;
			for(int i = 0; i < workingMem.size(); i++) {
				if(workingMem.get(i).getName().equals(type.getName())) {
					found = true;
					break;
				}
			}
			if(!found) {
				return false;
			}
		}

		return true;
	}
	
	public boolean isAntecedentInMem(Rule r) {
		for(Fact f : r.getLeftSide()) {
			boolean inMem = false;
			for(int i = 0; i < workingMem.size(); i++) {
				if(f.getName().equals(workingMem.get(i).getName())) {
					inMem = true;
					break;
				}
			}
			if(!inMem) {
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
