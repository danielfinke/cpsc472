package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Fact implements Serializable {
	public static enum FACT_TYPE {standby_time, talk_time, capacity, camera_flash,
		camcorder, camera, frontfacing_camera, camera_features, connectivity, rugged,
		weight, width, length, height, colours, touchscreen, features, size,
		resolution_height, resolution_width, pixel_density, cores, storage_expansion,
		built_in_storage, system_memory, processor_speed, builtin_online_services,
		multimedia, sensors, notifications, untitled, hearing_aid_compatibility};
	
	//Eclipse is whining at me, had to make facts serializable to pass them as 
	//objects to the Results View and convention says this is a required field.
	public final static long serialVersionUID = 0;	
	
	private String name;
	private ArrayList<String> lingVals;
	
	public static ArrayList<Fact> parseFactsToList(String facts) {
		ArrayList<Fact> list = new ArrayList<Fact>();
		StringTokenizer tok = new StringTokenizer(facts, "&");
		while(tok.hasMoreTokens()) {
			String part = tok.nextToken();
			StringTokenizer tok2 = new StringTokenizer(part, " ");
			String factName = tok2.nextToken();
			String linguistic = tok2.nextToken();
			
			Fact f = new Fact(factName);
			f.addLinguisticValue(linguistic);
			list.add(f);
		}
		return list;
	}
	
	public static int totalFactTypes() {
		return FACT_TYPE.values().length;
	}
	
	public static ArrayList<Fact> allFactTypes() {
		ArrayList<Fact> allFactTypes = new ArrayList<Fact>();
		for(FACT_TYPE type : FACT_TYPE.values()) {
			allFactTypes.add(new Fact(type.name()));
		}
		return allFactTypes;
	}
	
	public Fact(String name) {
		this.name = name;
		this.lingVals = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getLinguisticValues() {
		return lingVals;
	}
	
	public int getLinguisticValueCount() {
		return lingVals.size();
	}
	
	public void addLinguisticValue(String val) {
		lingVals.add(val);
	}
	
	public void addLinguisticValues(ArrayList<String> vals) {
		lingVals.addAll(vals);
	}
	
	public String getLinguisticVarAvg() {
		double avg = 0;
		for(String s : lingVals) {
			avg += InferenceEngine.defuzzify(s);
		}
		avg /= getLinguisticValueCount();
		return InferenceEngine.fuzzify(avg);
	}

	@Override
	public String toString() {
		if(getLinguisticValueCount() == 1) {
			return "Fact [name=" + name + ", lingVal=" + lingVals.get(0) + "]";
		}
		else {
			return "Fact [name=" + name + ", lingValAvg=" + getLinguisticVarAvg() + "]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public boolean meetsCriteria(Fact other) {
		if(getLinguisticValueCount() == 1) {
			return name.equals(other.name) && lingVals.get(0).equals(other.lingVals.get(0));
		}
		else {
			return name.equals(other.name) &&
					getLinguisticVarAvg().equals(other.getLinguisticVarAvg());
		}
	}
}
