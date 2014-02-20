package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.QuestionGenerator;

/**
 * The Activity responsible for displaying to a user a Question, and having the
 * user give an answer for it.
 * 
 * @author Andrew J Toms II
 */
public class QuestionActivity extends Activity {

	private final String QUESTION= "QUESTION_BODY";
	private final String ANSWER = "ANSWER_";
	private final String ANSWER_COUNT = "ANSWER_COUNT";
	private Question question;
	private TextView questionBody;
	private ListView answerView;
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_question);
		MainActivity.appContext = this;
		
		//Get the component references
		this.questionBody = (TextView) this.findViewById(
				R.id.question_question_view);
		this.answerView = (ListView) this.findViewById(
				R.id.question_answer_view);
		
		//Set up the specific view for the question we are either remembering or
		//want to grab; and then draw the view appropriately.
		if(savedState == null)
			fetchNewQuestion();
		else{
			restoreQuestion(savedState);
		}
		drawQuestion();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question, menu);
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		//Need to remember our question
		outState.putString(this.QUESTION, question.getText());
		outState.putInt(this.ANSWER_COUNT, question.getAnswers().size());
		for(int i = 0; i < question.getAnswers().size(); i++){
			outState.putString(this.ANSWER + i, 
					question.getAnswers().get(i).toString());
		}
	}
	
	/**
	 * This method grabs a brand new Question from our storage. 
	 * 
	 * //FOR_DANIEL: However we end up storing all of our questions will be here
	 * so if you're going to touch that; here's where to do it.
	 */
	public void fetchNewQuestion(){
		this.question = QuestionGenerator.getQuestion();
	}
	
	/**
	 * This method will restore to this activity the old Question this view had
	 * in the event it is lost by pausing or re-orienting the screen.
	 * 
	 * @param savedState The Bundle object that has all of the data for our old
	 * question.
	 */
	public void restoreQuestion(Bundle savedState){
		String question_name = savedState.getString(this.QUESTION);
		ArrayList<QuestionAnswer> answers = new ArrayList<QuestionAnswer>();
		for(int i = 0; i < savedState.getInt(this.ANSWER_COUNT); i++){
			String s = savedState.getString(this.ANSWER + i);
			QuestionAnswer qa = QuestionAnswer.getInstance(s, 
					QuestionAnswerType.TEXT);									//TODO: Proper Types
			answers.add(qa);
		}
		this.question = new Question(question_name, QuestionAnswerType.TEXT,	//TODO: Proper Types
				answers);
	}

	/**
	 * A void method that draws this Activity's question.
	 */
	public void drawQuestion(){
		//Body of the Question goes here.
		this.questionBody.setText(this.question.getText());
		
		//The Answer View; when a user selects an answer it needs to remember
		//what they selected and then fetch a new question.
		this.answerView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//TODO: Update the inference Question here
				fetchNewQuestion();
				drawQuestion();
			}
			
		});
		ArrayAdapter<QuestionAnswer> x = new ArrayAdapter<QuestionAnswer>(this, 
				R.layout.item_question_answer, this.question.getAnswers());
		this.answerView.setAdapter(x);
	}
}
