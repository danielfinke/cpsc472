package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.os.Bundle;

public class Rule implements Comparable<Rule> {
	private int ruleId;
	private ArrayList<Fact> leftSide;
	private ArrayList<Fact> rightSide;
	
	/*
	 * Parses out a rule from a rule string in our custom format
	 * 
	 * @param rule		the rule string
	 */
	private void assignConditionAndResult(String rule) {
		StringTokenizer tok = new StringTokenizer(rule, ">");
		
		String left = tok.nextToken();
		this.leftSide = Fact.parseFactsToList(left);
		String right = tok.nextToken();
		this.rightSide = Fact.parseFactsToList(right);
	}
	
	/*
	 * Creates a rule from database data
	 * 
	 * @param ruleId	unique identifier of rule
	 * @param rule		rule string in our custom format
	 * @return			the new rule instance
	 */
	public Rule(int ruleId, String rule) {
		this.ruleId = ruleId;
		assignConditionAndResult(rule);
	}
	
	/*
	 * Create a rule from some previous state
	 * 
	 * @param bundle		the bundle to load previous state from
	 * @param bundlePrefix	path to uniquely ID this rule
	 * @return				the restored rule
	 */
	public Rule(Bundle bundle, String bundlePrefix) {
		this.ruleId = bundle.getInt(bundlePrefix + "id");
		
		ArrayList<Fact> leftSide = new ArrayList<Fact>();
		ArrayList<String> leftKeys = bundle.getStringArrayList(bundlePrefix + "leftKeys");
		for(String key : leftKeys) {
			leftSide.add(new Fact(bundle, bundlePrefix + "leftSide" + key + "_"));
		}
		this.leftSide = leftSide;
		
		ArrayList<Fact> rightSide = new ArrayList<Fact>();
		ArrayList<String> rightKeys = bundle.getStringArrayList(bundlePrefix + "rightKeys");
		for(String key : rightKeys) {
			rightSide.add(new Fact(bundle, bundlePrefix + "rightSide" + key + "_"));
		}
		this.rightSide = rightSide;
	}
	
	/*
	 * Save the state of a rule
	 * 
	 * @param bundle		bundle to store state into
	 * @param bundlePrefix	path to uniquely ID this rule
	 */
	public void saveState(Bundle bundle, String bundlePrefix) {
		bundle.putInt(bundlePrefix + "id", getRuleId());
		
		ArrayList<String> leftKeys = new ArrayList<String>();
		for(Fact f : leftSide) {
			f.saveState(bundle, bundlePrefix + "leftSide" + f.getName() + f.getSet() + "_");
			leftKeys.add(f.getName() + f.getSet());
		}
		bundle.putStringArrayList(bundlePrefix + "leftKeys", leftKeys);
		
		ArrayList<String> rightKeys = new ArrayList<String>();
		for(Fact f : rightSide) {
			f.saveState(bundle, bundlePrefix + "rightSide" + f.getName() + f.getSet() + "_");
			rightKeys.add(f.getName() + f.getSet());
		}
		bundle.putStringArrayList(bundlePrefix + "rightKeys", rightKeys);
	}
	
	/*
	 * Return id of the rule
	 * 
	 * @return		rule id
	 */
	public int getRuleId() {
		return ruleId;
	}
	
	/*
	 * Get all facts in the antecedent of the rule
	 * 
	 * @return		an ArrayList of Fact objects in the antecedent
	 */
	public ArrayList<Fact> getLeftSide() {
		return leftSide;
	}
	
	/*
	 * Get all facts in the conclusion of the rule
	 * 
	 * @return		an ArrayList of Fact objects in the conclusion
	 */
	public ArrayList<Fact> getRightSide() {
		return rightSide;
	}
	
	/*public void setClosenessScore(int closenessScore) {
		this.closenessScore = closenessScore;
	}*/
	
	public int compareTo(Rule other) {
		/*if(closenessScore < other.closenessScore) {
			return -1;
		}
		else if(closenessScore == other.closenessScore) {
			return 0;
		}
		else {
			return 1;
		}*/
		return 0;
	}
}
