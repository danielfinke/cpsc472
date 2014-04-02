package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.models.Question;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswer.SliderAnswer;
import ca.unbc.cpsc472.mynextphone.models.QuestionAnswerType;
import ca.unbc.cpsc472.mynextphone.models.QuestionManager;
import ca.unbc.cpsc472.mynextphone.models.Result;
import ca.unbc.cpsc472.mynextphone.models.SliderListAdapter;
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
	private final String ANSWER_COUNT = "ANSWER_COUNT";
	private Question question;
	private LinearLayout layout;
	private TextView questionBody;
	private ListView textAnswerView;
	private GridView tileAnswerView;
	private ListView sldrAnswerView;
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
		this.sldrAnswerView = (ListView) this.findViewById(
				R.id.question_slider_answer_view);
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
	public void onSaveInstanceState(Bundle bundle){
		// Need to remember our question
		if(this.questionReady()){
			bundle.putInt(this.QUESTION_ID, question.getId());
			bundle.putString(this.QUESTION, question.getText());
			bundle.putInt(this.TYPE, question.getType().ordinal());
			bundle.putInt(this.ANSWER_COUNT, question.getAnswers().size());
			
			// Save the possible answers
			int[] keys = new int[question.getAnswers().size()];
			for(int i = 0; i < question.getAnswers().size(); i++) {
				QuestionAnswer qa = question.getAnswers().get(i);
				qa.saveState(bundle, "questionAnswer" + qa.getId() + "_");
				keys[i] = qa.getId();
			}
			bundle.putIntArray("answerKeys", keys);
			
			qMan.saveState(bundle, question.getId());
		}
	}
	
	/**
	 * This method grabs a brand new Question from our storage. 
	 */
	public void fetchNewQuestion() {
		this.question = qMan.getQuestion();
	}
	
	public void answerQuestion(QuestionAnswer qa) {
		qMan.submitAnswer(qa);
	}
	
	/**
	 * This method will restore to this activity the old Question this view had
	 * in the event it is lost by pausing or re-orienting the screen.
	 * 
	 * @param bundle The Bundle object that has all of the data for our old
	 * question.
	 */
	public void restoreQuestions(Bundle bundle){
		int questionId = bundle.getInt(this.QUESTION_ID);
		String question_name = bundle.getString(this.QUESTION);
		QuestionAnswerType type = QuestionAnswerType.values()[
		     bundle.getInt(this.TYPE)
		];
		
		ArrayList<QuestionAnswer> answers = new ArrayList<QuestionAnswer>();
		int[] keys = bundle.getIntArray("answerKeys");
		for(int i = 0; i < bundle.getInt(this.ANSWER_COUNT); i++) {
			QuestionAnswer qa = QuestionAnswer.getInstance(bundle,
					"questionAnswer" + keys[i] + "_",
					type);
			answers.add(qa);
		}
		
		this.question = new Question(questionId, question_name,
				type,
				answers);
		
		qMan.restoreState(bundle);
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
		else if(this.question.getType() == QuestionAnswerType.SLIDER)
			layout.addView(this.sldrAnswerView);
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
			
			
		} else if (this.question.getType() == QuestionAnswerType.SLIDER){
			final SliderListAdapter x = new SliderListAdapter(this, 
					this.question.getAnswers());
			this.sldrAnswerView.setAdapter(x);
			RelativeLayout sliderView = (RelativeLayout) x.getView(0, null, null);
			//Grab the button on this layer
			Button b =(Button) sliderView.getChildAt(1);
			sliderView = (RelativeLayout) sliderView.getChildAt(0);
			
			//Drop to the next layer to get the seek bar and text fields. Then
			//write the update code to those.
			final SeekBar skbr = (SeekBar) sliderView.getChildAt(0);
			final TextView text = (TextView) sliderView.getChildAt(1);
			final TextView perc = (TextView) sliderView.getChildAt(2);
			skbr.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar skbr, int progress,
						boolean fromUser) {
					if(fromUser){
						
						//Get our concrete value and print it nicely
						if(progress == 100)
							perc.setText("(1.00)");
						else if(progress < 10)
							perc.setText("(0.0" + progress + ")");
						else
							perc.setText("(0." + progress + ")");
						
						//Get our linguistic variable and display it.
						if(progress < 20){			//[0,20)
							text.setText("Very Low");
							text.setTextColor(getResources().getColor(
									R.color.red));
							perc.setTextColor(getResources().getColor(
									R.color.red));
						} else if (progress < 40){	//[20,40)
							text.setText("Low");
							text.setTextColor(getResources().getColor(
									R.color.orange));
							perc.setTextColor(getResources().getColor(
									R.color.orange));
						} else if (progress < 60){	//[40,60)
							text.setText("Medium");
							text.setTextColor(getResources().getColor(
									R.color.yellow));
							perc.setTextColor(getResources().getColor(
									R.color.yellow));
						} else if (progress < 80){	//[60,80)
							text.setText("High");
							text.setTextColor(getResources().getColor(
									R.color.lime_green));
							perc.setTextColor(getResources().getColor(
									R.color.lime_green));
						} else { //[80,100]
							text.setText("Very High");
							text.setTextColor(getResources().getColor(
									R.color.green));
							perc.setTextColor(getResources().getColor(
									R.color.green));
						}
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
					//Not pertinent to our intended purpose
				}

				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
					//Not pertinent to our intended purpose
				}
				
			});
			
			//Now set up the button's onItemClick listener
			b.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					SliderAnswer sa = (SliderAnswer)question.getAnswers().get(0);
					sa.applySliderValue(skbr.getProgress() / 100.0);
					answerQuestion(question.getAnswers().get(0));
					fetchNewQuestion();
					if(questionReady())
						drawQuestion();
					else
						gotoResult();
				}
				
				
				
			});
			
		}else {
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
	
	public void hello(View v){
		Log.i("Andrew", "Herro!");
	}
	
	public boolean questionReady(){
		return this.question!=null;
	}
	
	public void gotoResult(){
		Intent intent = new Intent(this,ResultActivity.class);
		// Andrew: use qMan.getResults() and serialize as necessary
		ArrayList<Result> res = qMan.getResults();
		intent.putExtra("result_count", res.size());
		for(int i = 0; i < res.size(); i++){
			intent.putExtra("result_" + i, res.get(i));
		}
		this.startActivity(intent);
		this.finish();
	}
}
