package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.os.Bundle;

public class Rule implements Comparable<Rule> {
	private int ruleId;
	private ArrayList<Fact> leftSide;
	private ArrayList<Fact> rightSide;
	
	private void assignConditionAndResult(String rule) {
		StringTokenizer tok = new StringTokenizer(rule, ">");
		
		String left = tok.nextToken();
		this.leftSide = Fact.parseFactsToList(left);
		String right = tok.nextToken();
		this.rightSide = Fact.parseFactsToList(right);
	}
	
	public Rule(int ruleId, String rule) {
		this.ruleId = ruleId;
		assignConditionAndResult(rule);
	}
	
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
	
	public int getRuleId() {
		return ruleId;
	}
	
	public ArrayList<Fact> getLeftSide() {
		return leftSide;
	}
	
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
