package ca.unbc.cpsc472.mynextphone.models;

public class Fact {
	private int id;
	private String name;
	private boolean truthFlag;
	
	public Fact(int id, String name, int truthVal) {
		this.id = id;
		this.name = name;
		this.truthFlag = truthVal == 1;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getTruthFlag() {
		return truthFlag;
	}
}
