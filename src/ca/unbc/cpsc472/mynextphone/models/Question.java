package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;

/**
 * This is a basic Question Class that 
 * 
 * @author Andrew J Toms II
 */
public class Question {

	private QuestionAnswerType type;
	private String text;
	private ArrayList<QuestionAnswer> answers;
	
	/**
	 * A basic constructor that initializes fields and doesn't do anything fancy
	 * 
	 * @param text The text body of the question.
	 * @param answerSet The set of answers to this question.
	 * @param type The type of answers this question accepts.
	 */
	public  Question(String text, QuestionAnswerType type, 
			ArrayList<QuestionAnswer> answerSet){
		this.text = text;
		this.type = type;
		this.answers = answerSet;
	}
	
	/**
	 * @return The type of answer display this question object has. We need the
	 * type at this level so that the QuestionActivity can see it.
	 */
	public QuestionAnswerType getType() {
		return type;
	}

	/**
	 * @return The text body of this Question.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return The answers for the question.
	 */
	public ArrayList<QuestionAnswer> getAnswers() {
		return answers;
	}
	
	
}
