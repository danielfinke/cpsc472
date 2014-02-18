package ca.unbc.cpsc472.mynextphone.models;

public class Fact {
	public static enum FactType {INTERMEDIATE, CONCLUSIVE};
	
	private int id;
	private FactType factType;
	private String name;
	private boolean truthFlag;
	
	public Fact(int id, String name, int truthVal, FactType factType) {
		this.id = id;
		this.name = name;
		this.truthFlag = truthVal == 1;
		this.factType = factType;
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
	
	public FactType getFactType() {
		return factType;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean retVal = false;
		
		if(other instanceof Fact) {
			Fact f = (Fact)other;
			retVal = f.getName() == this.getName() && f.getTruthFlag() == this.getTruthFlag();
		}
		return retVal;
	}
}
