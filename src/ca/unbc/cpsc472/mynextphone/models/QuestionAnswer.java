package ca.unbc.cpsc472.mynextphone.models;

import java.io.IOException;

import ca.unbc.cpsc472.mynextphone.MainActivity;

import android.view.View;
import android.widget.TextView;

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

	private String stringValue;
	
	/**
	 * Constructor is private to force the use of the GetInstance method.
	 * 
	 * @param val The String representing the data of the QuestionAnswer.
	 */
	private QuestionAnswer(String val){
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
	public static QuestionAnswer getInstance(String value, 
			QuestionAnswerType type){
		switch(type){
			case TEXT:
				return new StringAnswer(value);
			case TILE:
				try{
					return new ImageAnswer(value);
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
	
	/**
	 * This method returns the appropriate view representation of this Question
	 * Answer. This is an abstract base-class method; the subclass 
	 * implementations are defined below.
	 * 
	 * @return A view representing this Question Answer.
	 */
	public abstract View getView();
	
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

		private StringAnswer(String text){
			super(text);
		}
		
		@Override
		public View getView() {
			TextView tv = new TextView(MainActivity.appContext);
			tv.setText(this.toString());
			return tv;
		}
		
	}
	
	private static class ImageAnswer extends QuestionAnswer{

		private ImageAnswer(String text) throws IOException{
			super(text);
		}
		
		@Override
		public View getView() {
			//TODO: ONLY TEMPORARY; CONVERT TO RETURN AN APPROPRIATE IMAGEVIEW
			TextView tv = new TextView(MainActivity.appContext);
			tv.setText(this.toString());
			return tv;
		}
		
	}
	
}
