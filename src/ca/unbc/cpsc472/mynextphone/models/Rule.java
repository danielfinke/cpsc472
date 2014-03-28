package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.StringTokenizer;

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
