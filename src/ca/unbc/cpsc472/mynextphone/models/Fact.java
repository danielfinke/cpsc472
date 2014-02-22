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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((factType == null) ? 0 : factType.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (truthFlag ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fact other = (Fact) obj;
		if (factType != other.factType)
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (truthFlag != other.truthFlag)
			return false;
		return true;
	}
	
	/*@Override
	public boolean equals(Object other) {
		boolean retVal = false;
		
		if(other instanceof Fact) {
			Fact f = (Fact)other;
			retVal = f.getName() == this.getName() && f.getTruthFlag() == this.getTruthFlag();
		}
		return retVal;
	}
	
	@Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + 
        hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 89 * hash + this.age;
        return hash;
    }*/
}
