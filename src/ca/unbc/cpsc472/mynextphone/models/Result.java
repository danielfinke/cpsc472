package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

/**
 * The model for a result. This will allow us to throw what we need from the 
 * inference engine into a nice, simple cohesive object to read out in the 
 * Result Activity.
 * 
 * @author Andrew J Toms II
 */
public class Result implements Serializable {

	final static long serialVersionUID = 0;
	
	private String phoneName;
	private String phoneDesc;
	private ArrayList<String> imgPaths;
	private ArrayList<Fact> reasoning;
	public final int id;
	
	public Result(int id, String phoneName, String phoneDesc, ArrayList<Fact> reasoning){
		this.id = id;
		this.phoneName = phoneName;
		this.phoneDesc = phoneDesc;
		this.reasoning = reasoning;
	}

	/**
	 * @return The name of this phone as a String.
	 */
	public String getPhoneName() {
		return phoneName;
	}
	
	public String getPhoneDesc() {
		return phoneDesc;
	}
	
	public ArrayList<String> getImgPaths() {
		return imgPaths;
	}
	
	public String getPrimaryImgPath() {
		try {
			return imgPaths.get(0);
		}
		catch(IndexOutOfBoundsException ex) {
			Log.e(this.getClass().getName(), "No image available for phone: " + phoneName);
			return null;
		}
	}

	/**
	 * @return The Reasoning as a list of Fact objects.
	 */
	public ArrayList<Fact> getReasoning() {
		return reasoning;
	}
	
	public void setImgPaths(ArrayList<String> paths) {
		this.imgPaths = paths;
	}
	
	@Override
	public String toString() {
		String ret = "Result [phoneName=" + phoneName + ", reasoning={";
		for(Fact f : reasoning) {
			ret += f.toString();
			if(f != reasoning.get(reasoning.size() - 1)) {
				ret += ", ";
			}
		}
		ret += "}]";
		return ret;
	}
}
