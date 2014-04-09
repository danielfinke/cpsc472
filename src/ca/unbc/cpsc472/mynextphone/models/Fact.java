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
	
	/*
	 * Parse a list of facts from a fact string using the app's custom format
	 * 
	 * @param facts		the string representing the list of facts
	 * @return			an ArrayList of the facts the string was representing
	 */
	public static ArrayList<Fact> parseFactsToList(String facts) {
		ArrayList<Fact> list = new ArrayList<Fact>();
		// Break apart facts by AND
		StringTokenizer tok = new StringTokenizer(facts, "&");
		while(tok.hasMoreTokens()) {
			String part = tok.nextToken();
			// Space breaks apart collection name and its set value
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
	
	/*
	 * Return the number of fact types relevant to decision making
	 * 
	 * @return 		the total number of fact types
	 */
	public static int totalFactTypes() {
		return FACT_TYPE.values().length;
	}
	
	/*
	 * Returns the truth value for whether the specified linguistic
	 * variable is relevant to device decision making
	 * 
	 * @param lingVar		the linguistic variable to check
	 * @return				whether the variable is used to determine which device
	 * 						is appropriate
	 */
	public static boolean isLinguisticVariable(String lingVar) {
		try {
			FACT_TYPE.valueOf(lingVar);
			return true;
		}
		catch(IllegalArgumentException ex) {
		}
		return false;
	}
	
	/*
	 * Fetches a list of all the relevant facts to decision making
	 * 
	 * @return		an ArrayList containing an instance of each Fact that
	 * 				is relevant to device choice
	 */
	public static ArrayList<Fact> allFactTypes() {
		ArrayList<Fact> allFactTypes = new ArrayList<Fact>();
		for(FACT_TYPE type : FACT_TYPE.values()) {
			allFactTypes.add(new Fact(type.name(), null));
		}
		return allFactTypes;
	}
	
	/*
	 * Create a new instance from data
	 * 
	 * @param name		the linguistic variable the Fact belongs to
	 * @param set		the set membership value
	 * @return			a new Fact object
	 */
	public Fact(String name, String set) {
		this.name = name;
		this.set = set;
		this.tuples = new ArrayList<Tuple>();
	}
	
	/*
	 * Create a Fact object from a saved state
	 * 
	 * @param bundle		the bundle containing saved information
	 * @param bundlePrefix	the prefix for unique path id'ing for bundle
	 * @return				a restored Fact object
	 */
	public Fact(Bundle bundle, String bundlePrefix) {
		this.name = bundle.getString(bundlePrefix + "name");
		this.set = bundle.getString(bundlePrefix + "set");
		
		this.tuples = new ArrayList<Tuple>();
		ArrayList<String> keys = bundle.getStringArrayList(bundlePrefix + "keys");
		for(String key : keys) {
			tuples.add((Tuple)bundle.getSerializable(key));
		}
	}
	
	/*
	 * Store the state of the Fact in a bundle
	 * 
	 * @param bundle		the bundle to store saved information
	 * @param bundlePrefix	the prefix for unique path id'ing for bundle
	 */
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
	
	/*
	 * Get the linguistic variable for the Fact
	 * 
	 * @return		the name of the linguistic variable
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * Get the set data for the Fact
	 * 
	 * @return		an ArrayList of Tuple objects representing the set membership
	 */
	public ArrayList<Tuple> getTuples() {
		return tuples;
	}
	
	/*
	 * Get the size of the set data
	 * 
	 * @return		the number of Tuples that compose the set membership
	 */
	public int getTupleCount() {
		return tuples.size();
	}
	
	/*
	 * Returns whether the set in this Fact is the empty set
	 * 
	 * @return		true if the set does not have any membership value in any Tuple
	 */
	public boolean isEmptySet() {
		for(int i = 0; i < tuples.size(); i++) {
			if((Double)tuples.get(i).getObject(2) != 0) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Add a new tuple to the set value in this Fact
	 * 
	 * @param tuple		the tuple to be added
	 */
	public void addTuple(Tuple tuple) {
		tuples.add(tuple);
	}
	
	/*
	 * Add multiple tuples to the set value in this Fact
	 * 
	 * @param tuples	all tuples to be added
	 */
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

	/*
	 * Get the name of the set in this Fact
	 * 
	 * @return		name of the set
	 */
	public String getSet() {
		return set;
	}

	private void setSet(String set) {
		this.set = set;
	}
}
