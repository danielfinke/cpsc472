package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The model for a result. This will allow us to throw what we need from the 
 * inference engine into a nice, simple cohesive object to read out in the 
 * Result Activity.
 * 
 * @author Andrew J Toms II
 */
public class Result implements Serializable{

	final static long serialVersionUID = 0;
	
	private String phoneName;
	private int imgID;
	private ArrayList<Fact> reasoning;
	
	public Result(String phoneName, int img, ArrayList<Fact> reasoning){
		this.phoneName = phoneName;
		this.imgID = img;
		this.reasoning = reasoning;
	}

	/**
	 * @return The name of this phone as a String.
	 */
	public String getPhoneName() {
		return phoneName;
	}
	
	/**
	 * @return The R.drawable.id value for the drawable image for the phone 
	 * represented by this Result object.
	 */
	public int getImageID() {
		return imgID;
	}

	/**
	 * @return The Reasoning as a list of Fact objects.
	 */
	public ArrayList<Fact> getReasoning() {
		return reasoning;
	}
}
