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
public class Result implements Serializable {

	final static long serialVersionUID = 0;
	
	private String phoneName;
	// Andrew: message Daniel about this for why it's been changed to a file path
	private String imgPath;
	private ArrayList<Fact> reasoning;
	public final int id;
	
	public Result(int id, String phoneName, String imgPath, ArrayList<Fact> reasoning){
		this.id = id;
		this.phoneName = phoneName;
		this.imgPath = imgPath;
		this.reasoning = reasoning;
	}

	/**
	 * @return The name of this phone as a String.
	 */
	public String getPhoneName() {
		return phoneName;
	}
	
	public String getImagePath() {
		return imgPath;
	}

	/**
	 * @return The Reasoning as a list of Fact objects.
	 */
	public ArrayList<Fact> getReasoning() {
		return reasoning;
	}
}
