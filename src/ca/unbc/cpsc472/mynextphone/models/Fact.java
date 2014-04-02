package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TreeSet;

import android.os.Bundle;
import android.util.Log;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;

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
	
	public static final TreeSet<String> allNames;
	static{
		allNames = new TreeSet<String>(PhoneDataBaseHelper.getInstance(null).getValueNames());
		for(FACT_TYPE f:FACT_TYPE.values())
			allNames.add(f.toString());
	}
	
	private String name;
	private String set;
	private ArrayList<Tuple> tuples;
	
	public static ArrayList<Fact> parseFactsToList(String facts) {
		ArrayList<Fact> list = new ArrayList<Fact>();
		StringTokenizer tok = new StringTokenizer(facts, "&");
		while(tok.hasMoreTokens()) {
			String part = tok.nextToken();
			StringTokenizer tok2 = new StringTokenizer(part, " ");
			String factName = tok2.nextToken();
			String linguistic = tok2.nextToken();
			
			try {
				Fact f = new Fact(factName, linguistic);
				f.setSet(linguistic);
				f.addTuples(PhoneDataBaseHelper.getInstance(null).getLinguisticTuples(linguistic));
				list.add(f);
			}
			catch(Exception ex) {
				Log.e("fact", Arrays.toString(ex.getStackTrace()));
				Log.e("Fact", "Unable to get linguistic tuples for fact: " + factName);
			}
		}
		return list;
	}
	
	public static int totalFactTypes() {
		return FACT_TYPE.values().length;
	}
	
	public static boolean isLinguisticVariable(String lingVar) {
		try {
			FACT_TYPE.valueOf(lingVar);
			return true;
		}
		catch(IllegalArgumentException ex) {
		}
		return false;
	}
	
	public static ArrayList<Fact> allFactTypes() {
		ArrayList<Fact> allFactTypes = new ArrayList<Fact>();
		for(FACT_TYPE type : FACT_TYPE.values()) {
			allFactTypes.add(new Fact(type.name(), null));
		}
		return allFactTypes;
	}
	
	public Fact(String name, String set) {
		this.name = name;
		this.set = set;
		this.tuples = new ArrayList<Tuple>();
	}
	
	public Fact(Bundle bundle, String bundlePrefix) {
		this.name = bundle.getString(bundlePrefix + "name");
		this.set = bundle.getString(bundlePrefix + "set");
		
		this.tuples = new ArrayList<Tuple>();
		ArrayList<String> keys = bundle.getStringArrayList(bundlePrefix + "keys");
		for(String key : keys) {
			tuples.add((Tuple)bundle.getSerializable(key));
		}
	}
	
	public void saveState(Bundle bundle, String bundlePrefix) {
		bundle.putString(bundlePrefix + "name", getName());
		bundle.putString(bundlePrefix + "set", getSet());

		ArrayList<String> keys = new ArrayList<String>();
		for(int i = 0; i < tuples.size(); i++) {
			Tuple t = tuples.get(i);
			bundle.putSerializable(bundlePrefix + "tuples" + i, t);
			keys.add(bundlePrefix + "tuples" + i);
		}
		bundle.putStringArrayList(bundlePrefix + "keys", keys);
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Tuple> getTuples() {
		return tuples;
	}
	
	public int getTupleCount() {
		return tuples.size();
	}
	
	public boolean isEmptySet() {
		for(int i = 0; i < tuples.size(); i++) {
			if((Double)tuples.get(i).getObject(2) != 0) {
				return false;
			}
		}
		return true;
	}
	
	public void addTuple(Tuple tuple) {
		tuples.add(tuple);
	}
	
	public void addTuples(ArrayList<Tuple> tuples) {
		this.tuples.addAll(tuples);
	}

	@Override
	public String toString() {
		String ret = "Fact [name=" + name + ", tuples={";
		for(Tuple t : tuples) {
			ret += t.toString();
			if(t != tuples.get(tuples.size() - 1)) {
				ret += ", ";
			}
		}
		ret += "}]";
		return ret;
	}

	public String getSet() {
		return set;
	}

	private void setSet(String set) {
		this.set = set;
	}
}
