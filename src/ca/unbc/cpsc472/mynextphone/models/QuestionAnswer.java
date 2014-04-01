package ca.unbc.cpsc472.mynextphone.models;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.R;
import ca.unbc.cpsc472.mynextphone.helpers.BitmapScaler;

/**
 * This is the model for an answer to questions as the user percieves them; an
 * answer that has a physical view defined for it. The different kinds of views
 * are handled through private subclassing; as such there is no public 
 * constructor just a getInstance method where the user passes the type and 
 * String value to initialize the object. The rest is handled through private-
 * access subclasses.
 * 
 * @author Andrew J Toms II
 */
public abstract class QuestionAnswer {
	private int id;
	private String stringValue;
	protected ArrayList<Fact> facts;
	
	/**
	 * Constructor is private to force the use of the GetInstance method.
	 * 
	 * @param val The String representing the data of the QuestionAnswer.
	 */
	private QuestionAnswer(int id, String val, String facts) {
		this.id = id;
		this.stringValue = val;
		this.facts = Fact.parseFactsToList(facts);
	}
	
	private QuestionAnswer(Bundle bundle, String bundlePrefix) {
		this.id = bundle.getInt(bundlePrefix + "id");
		this.stringValue = bundle.getString(bundlePrefix + "stringValue");
		
		ArrayList<Fact> facts = new ArrayList<Fact>();
		ArrayList<String> keys = bundle.getStringArrayList(bundlePrefix + "keys");
		for(String key : keys) {
			facts.add(new Fact(bundle, bundlePrefix + "fact" + key + "_"));
		}
		this.facts = facts;
	}
	
	/**
	 * This is the method used to get Question Answer instances. The type of 
	 * the desired answer is specified, and then the appropriate behaviors are 
	 * initialized based off of that decision.
	 * 
	 * @param value The value used for the Question Answer. If the type of the
	 * answer is TILE, this argument should be the filepath to the 
	 * @param type The type of answer this object represents; either a TILE or
	 * TEXT.
	 * @return An instance of QuestionAnswer generated from the passed values. 
	 */
	public static QuestionAnswer getInstance(int id, String value,
			String facts, QuestionAnswerType type) {
		switch(type){
			case TEXT:
				return new StringAnswer(id, value, facts);
			case TILE:
				try{
					return new ImageAnswer(id, value, facts);
				}catch(IOException ioe){
					throw new IllegalArgumentException("Encountered an  error" +
							" trying to generate Tile Answer from String: " +
							value);
				}
			case SLIDER:
				try {
					return new SliderAnswer(id, value, facts);
				} catch (IOException e) {
					throw new IllegalArgumentException("Encountered an  error" +
							" trying to generate Slider Answer from String: " +
							value);
				}
			default:
				throw new IllegalArgumentException(type + " is an undefined A" +
						"nswer Type.");
		}
	}
	
	public static QuestionAnswer getInstance(Bundle bundle, String bundlePrefix,
			QuestionAnswerType type) {
		switch(type){
		case TEXT:
			return new StringAnswer(bundle, bundlePrefix);
		case TILE:
			try {
				return new ImageAnswer(bundle, bundlePrefix);
			}
			catch(IOException ioe) {
				throw new IllegalArgumentException("Encountered an  error" +
						" trying to generate Tile Answer from Bundle: " +
						bundle.describeContents());
			}
		case SLIDER:
			try {
				return new SliderAnswer(bundle, bundlePrefix);
			} catch (IOException e) {
				throw new IllegalArgumentException("Encountered an  error" +
						" trying to generate Slider Answer from Bundle: " +
						bundle.describeContents());
			}
		default:
			throw new IllegalArgumentException(type + " is an undefined A" +
					"nswer Type.");
		}
	}
	
	// TODO daniel save question answer state
	/*public void saveState(Bundle bundle, String bundlePrefix) {
		bundle.putInt(bundlePrefix + "id", getId());
		bundle.putString(bundlePrefix + "stringValue", stringValue);
		
		ArrayList<String> keys = new ArrayList<String>();
		for(Fact f : facts) {
			f.saveState(bundle, bundlePrefix + "fact" + f.getName() + f.getLinguisticVarString() + "_");
			keys.add(f.getName() + f.getLinguisticVarString());
		}
		bundle.putStringArrayList(bundlePrefix + "keys", keys);
	}*/
	
	public int getId() {
		return id;
	}
	
	public ArrayList<Fact> getFacts() {
		return facts;
	}
	
	/**
	 * This method returns the appropriate view representation of this Question
	 * Answer. This is an abstract base-class method; the subclass 
	 * implementations are defined below.
	 * 
	 * @return A view representing this Question Answer.
	 */
	public abstract View getView(Context c);
	
	/**
	 * @return This QuestionAnswer represented by a string value, either a 
	 * filepath to a the image file or the string that outlines the answer.
	 */
	public String toString(){
		return this.stringValue;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// The following are sub classes used to get the view object legitimately;
	// without resorting to poor engineering practices.
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private static class StringAnswer extends QuestionAnswer{

		private StringAnswer(int id, String text, String facts){
			super(id, text, facts);
		}
		
		private StringAnswer(Bundle bundle, String bundlePrefix) {
			super(bundle, bundlePrefix);
		}
		
		@Override
		public View getView(Context c) {
			LayoutInflater inf = (LayoutInflater) 
					c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ret = inf.inflate(R.layout.item_answer_text, null);
			((TextView) ret).setText(this.toString());
			return ret;
		}
		
	}
	
	private static class ImageAnswer extends QuestionAnswer{
		private ImageAnswer(int id, String text, String facts) throws IOException{
			super(id, text, facts);
		}
		
		private ImageAnswer(Bundle bundle, String bundlePrefix) throws IOException {
			super(bundle, bundlePrefix);
		}
		
		@Override
		public View getView(Context c) {
			LayoutInflater inf = (LayoutInflater) 
					c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ret = inf.inflate(R.layout.item_answer_image, null);
			int id = c.getResources().getIdentifier(this.toString(), "drawable",
					c.getPackageName());
			((ImageView) ret).setImageBitmap(
					BitmapScaler.decodeSampledBitmapFromResource(
							c.getResources(), id, 200, 200));
			((ImageView) ret).setLayoutParams(new GridView.LayoutParams(200, 200));
			((ImageView) ret).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			((ImageView) ret).setPadding(10, 10, 10, 10);
			
			return ret;
		}
		
	}
	
	public static class SliderAnswer extends QuestionAnswer {

		private SliderAnswer(int id, String text, String facts) throws IOException{
			super(id, text, facts);
		}
		
		private SliderAnswer(Bundle bundle, String bundlePrefix) throws IOException {
			super(bundle, bundlePrefix);
		}
		
		@Override
		public View getView(Context c) {
			LayoutInflater inf = (LayoutInflater) 
					c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ret = inf.inflate(R.layout.item_answer_slider, null);
//			((TextView) ret).setText(this.toString());
			return ret;
		}
		
		public void applySliderValue(double value) {
			ArrayList<Fact> newFacts = new ArrayList<Fact>();
			for(int i = 0; i < facts.size(); i++) {
				Fact old = facts.get(i);
				Fact newF = new Fact(old.getName());
				for(int j = 0; j < old.getTupleCount(); j++) {
					Tuple oldT = old.getTuples().get(j);
					double oldMin = (Double)oldT.getObject(0);
					double oldMax = (Double)oldT.getObject(1);
					double oldVal = (Double)oldT.getObject(2);
					Tuple newT = new Tuple(new Object[] {
							oldMin,
							oldMax,
							oldVal * value
					});
					newF.addTuple(newT);
				}
				newFacts.add(newF);
			}
			this.facts = newFacts;
		}
		
	}
	
}
