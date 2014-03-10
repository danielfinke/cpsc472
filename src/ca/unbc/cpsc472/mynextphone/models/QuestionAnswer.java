package ca.unbc.cpsc472.mynextphone.models;

import java.io.IOException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.R;

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
	
	/**
	 * Constructor is private to force the use of the GetInstance method.
	 * 
	 * @param val The String representing the data of the QuestionAnswer.
	 */
	private QuestionAnswer(int id, String val) {
		this.id = id;
		this.stringValue = val;
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
			QuestionAnswerType type) {
		switch(type){
			case TEXT:
				return new StringAnswer(id, value);
			case TILE:
				try{
					return new ImageAnswer(id, value);
				}catch(IOException ioe){
					throw new IllegalArgumentException("Encountered an  error" +
							" trying to generate Tile Answer from String: " +
							value);
				}
			default:
				throw new IllegalArgumentException(type + " is an undefined A" +
						"nswer Type.");
		}
	}
	
	public int getId() {
		return id;
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

		private StringAnswer(int id, String text){
			super(id, text);
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

		private ImageAnswer(int id, String text) throws IOException{
			super(id, text);
		}
		
		@Override
		public View getView(Context c) {
			LayoutInflater inf = (LayoutInflater) 
					c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ret = inf.inflate(R.layout.item_answer_image, null);
			int id = c.getResources().getIdentifier(this.toString(), "drawable",
					c.getPackageName());
			((ImageView) ret).setImageResource(id);
			((ImageView) ret).setLayoutParams(new GridView.LayoutParams(200, 200));
			((ImageView) ret).setScaleType(ImageView.ScaleType.CENTER_CROP);
			((ImageView) ret).setPadding(8, 8, 8, 8);
			return ret;
		}
		
	}
	
}
