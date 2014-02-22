package ca.unbc.cpsc472.mynextphone.models;

import java.util.HashSet;

public class Rule {
	public static enum RuleSide {LEFT, RIGHT};
	
	private int ruleId;
	private HashSet<Fact> leftSide;
	private HashSet<Fact> rightSide;
	
	public Rule(int ruleId) {
		this.ruleId = ruleId;
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
}
