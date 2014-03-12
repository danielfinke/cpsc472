package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.QuestionManager;
import ca.unbc.cpsc472.mynextphone.models.Result;
import ca.unbc.cpsc472.mynextphone.models.TextListAdapter;
import ca.unbc.cpsc472.mynextphone.models.TileListAdapter;

/**
 * The Activity responsible for displaying to a user a Question, and having the
 * user give an answer for it.
 * 
 * @author Andrew J Toms II
 */
public class QuestionActivity extends Activity {
	
	public static Context appContext;

	private final String QUESTION_ID = "QUESTION_ID";
	private final String TYPE = "TYPE";
	private final String QUESTION = "QUESTION_BODY";
	private final String ANSWER_ID = "ANSWER_ID_";
	private final String ANSWER = "ANSWER_";
	private final String ANSWER_COUNT = "ANSWER_COUNT";
	private Question question;
	private LinearLayout layout;
	private TextView questionBody;
	private ListView textAnswerView;
	private GridView tileAnswerView;
	private QuestionManager qMan;
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_question);
		appContext = this;
		qMan = new QuestionManager(appContext);
		
		//Get the component references
		this.questionBody = (TextView) this.findViewById(
				R.id.question_question_view);
		this.textAnswerView = (ListView) this.findViewById(
				R.id.question_text_answer_view);
		this.tileAnswerView = (GridView) this.findViewById(
				R.id.question_tile_answer_view);
		this.layout = (LinearLayout) this.findViewById(
				R.id.question_answer_view);
		
		//Set up the specific view for the question we are either remembering or
		//want to grab; and then draw the view appropriately.
		if(savedState == null){
			fetchNewQuestion();
		}else{
			restoreQuestions(savedState);
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
		if(this.questionReady()){
			outState.putInt(this.QUESTION_ID, question.getId());
			outState.putString(this.QUESTION, question.getText());
			outState.putBoolean(this.TYPE, 
					(this.question.getType() == QuestionAnswerType.TEXT));
			outState.putInt(this.ANSWER_COUNT, question.getAnswers().size());
			for(int i = 0; i < question.getAnswers().size(); i++){
				outState.putInt(this.ANSWER_ID + i,
						question.getAnswers().get(i).getId());
				outState.putString(this.ANSWER + i, 
						question.getAnswers().get(i).toString());
			}
			
			qMan.saveState(outState, question.getId());
		}
	}
	
	/**
	 * This method grabs a brand new Question from our storage. 
	 * 
	 * //FOR_DANIEL: However we end up storing all of our questions will be here
	 * so if you're going to touch that; here's where to do it.
	 */
	public void fetchNewQuestion(){
		this.question = qMan.getQuestion();
	}
	
	public void answerQuestion(QuestionAnswer qa) {
		qMan.submitAnswer(qa);
	}
	
	/**
	 * This method will restore to this activity the old Question this view had
	 * in the event it is lost by pausing or re-orienting the screen.
	 * 
	 * @param savedState The Bundle object that has all of the data for our old
	 * question.
	 */
	public void restoreQuestions(Bundle savedState){
		int questionId = savedState.getInt(this.QUESTION_ID);
		QuestionAnswerType type = savedState.getBoolean(this.TYPE) ? 
				QuestionAnswerType.TEXT : QuestionAnswerType.TILE;
		String question_name = savedState.getString(this.QUESTION);
		ArrayList<QuestionAnswer> answers = new ArrayList<QuestionAnswer>();
		for(int i = 0; i < savedState.getInt(this.ANSWER_COUNT); i++){
			int id = savedState.getInt(this.ANSWER_ID + i);
			String s = savedState.getString(this.ANSWER + i);
			QuestionAnswer qa = QuestionAnswer.getInstance(id, s, type);
			answers.add(qa);
		}
		this.question = new Question(questionId, question_name,
				type,
				answers);
		
		qMan.restoreState(savedState);
	}

	/**
	 * A void method that draws this Activity's question.
	 */
	public void drawQuestion(){
		//Body of the Question goes here.
		this.questionBody.setText(this.question.getText());
		
		//Give it the appropriate answer view.
		layout.removeAllViews();
		if(this.question.getType() == QuestionAnswerType.TEXT)
			layout.addView(this.textAnswerView);
		else
			layout.addView(this.tileAnswerView);
		
		//The Answer View; when a user selects an answer it needs to remember
		//what they selected and then fetch a new question. Changes dependent 
		//upon the type of ui needed to display the question.
		if(this.question.getType() == QuestionAnswerType.TEXT){

			final TextListAdapter x = new TextListAdapter(this, this.question.getAnswers());
			this.textAnswerView.setAdapter(x);
			this.textAnswerView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
						long arg3) {
					QuestionAnswer a = x.getAnswer(pos);
					answerQuestion(a);
					fetchNewQuestion();
					if(questionReady())
						drawQuestion();
					else{
						gotoResult();
					}
				}
				
			});
			
			
		} else {
			final TileListAdapter x = new TileListAdapter(this, 
					this.question.getAnswers());
			this.tileAnswerView.setAdapter(x);
			this.tileAnswerView.setOnItemClickListener(new OnItemClickListener() {
				
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					answerQuestion(x.answerFor(position));
					fetchNewQuestion();
					if(questionReady())
						drawQuestion();
					else
						gotoResult();
				}
			});
		}
		
	}
	
	public boolean questionReady(){
		return this.question!=null;
	}
	
	public void gotoResult(){
		Intent intent = new Intent(this,ResultActivity.class);
		// Andrew: use qMan.getResults() and serialize as necessary
		ArrayList<Result> res = qMan.getResults();
		intent.putExtra("result", res.get(0));
		this.startActivity(intent);
		this.finish();
	}
}
