package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;
import java.util.Random;

/**
 * Mainly used for generating Question objects for testing purposes; this class
 * can and probably should be re-purposed to grab questions from out Question
 * table in the database in final versions of the app.
 * 
 * @author Andrew J Toms II
 */
public class QuestionGenerator {

	private static String[] bodies = new String[]{
			"What is your name?", 
			"What is your favorite color?",
			"What is your quest?",
			"What is the air-speed velocity of an unladen swallow?",
			"Stormcloak or Imperials?",
			"Got Milk?",
			"Should I start this song off with a question?"
	};
	private static String[] strAns = new String[]{
			"Daniel Finke",
			"Andrew J. Toms II",
			"Joel Knudsen",
			"Jed",
			"Not Jed",
			"Blue",
			"Yellow",
			"Green",
			"Red",
			"Mauve",
			"To find the holy grail!",
			"That third one in the chain to get attuned for MC",
			"To take over the world!",
			"I... I don't know",
			"12 arbitrary Speed units",
			"It doesn't matter, all sparrows are laden",
			"Imperial",
			"Duh, obvs Imperial",
			"Not Stormcloak",
			"Always",
			"Nope",
			"Too late, you already did",
			"These answers are all messed up and out of order."
	};
	//private String[] imgAns = new String[]{};
	
	/**
	 * Generates a random question to display in this QuestionActivity.
	 * 
	 * @return A randomly generated Question.
	 */
	public static Question getQuestion(){
		ArrayList<QuestionAnswer> list = new ArrayList<QuestionAnswer>();
		Random r = new Random();
		int q = r.nextInt(bodies.length);
		int a = r.nextInt(3)+2;
		for(int i = 0; i < a; i++){
			list.add(QuestionAnswer.getInstance(
					strAns[r.nextInt(strAns.length)], QuestionAnswerType.TEXT));
		}
		Question ret = new Question(bodies[q], QuestionAnswerType.TEXT, list);
		return ret;
	}
	
}
