package ca.unbc.cpsc472.mynextphone.models;

import java.util.HashSet;

public class Rule implements Comparable<Rule> {
	public static enum RuleSide {LEFT, RIGHT};
	
	private int ruleId;
	private int closenessScore;
	private HashSet<Fact> leftSide;
	private HashSet<Fact> rightSide;
	
	public Rule(int ruleId) {
		this.ruleId = ruleId;
		this.closenessScore = 0;
		this.leftSide = new HashSet<Fact>();
		this.rightSide = new HashSet<Fact>();
	}
	
	public int getRuleId() {
		return ruleId;
	}
	
	public HashSet<Fact> getLeftSide() {
		return leftSide;
	}
	
	public HashSet<Fact> getRightSide() {
		return rightSide;
	}
	
	public void addFactCondition(Fact f) {
		leftSide.add(f);
	}
	
	public void addFactDeduction(Fact f) {
		rightSide.add(f);
	}
	
	public void setClosenessScore(int closenessScore) {
		this.closenessScore = closenessScore;
	}
	
	public int compareTo(Rule other) {
		if(closenessScore < other.closenessScore) {
			return -1;
		}
		else if(closenessScore == other.closenessScore) {
			return 0;
		}
		else {
			return 1;
		}
	}
}
