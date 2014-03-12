package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;

public class Fact implements Serializable {
	
	//Eclipse is whining at me, had to make facts serializable to pass them as 
	//objects to the Results View and convention says this is a required field.
	public final static long serialVersionUID = 0;	
												
	
	private int id;
	private int resultId;
	private String name;
	private boolean truthFlag;
	
	public Fact(int id, String name, int truthVal, int resultId) {
		this.id = id;
		this.name = name;
		this.truthFlag = truthVal == 1;
		this.resultId = resultId;
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
	
	public int getResultId() {
		return resultId;
	}
	
	public boolean isResult() {
		return resultId != -1;
	}
	
	public void toggleTruthFlag() {
		truthFlag = !truthFlag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + resultId;
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
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (resultId != other.resultId)
			return false;
		if (truthFlag != other.truthFlag)
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return (truthFlag?"":"not ")+this.getName();
	}
}
