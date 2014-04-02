package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.InferenceEngine;
import ca.unbc.cpsc472.mynextphone.models.Result;

public class ResultActivity extends Activity{

	private TextView name;
	private ImageView img;
	private TextView reasons;
	private ArrayList<Result> resultSet;
	private int resultIndex;
	
	private ImageButton next;
	private ImageButton prev;
	private ImageButton approve;
	private ImageButton disapprove;
	private ImageButton restart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		PhoneDataBaseHelper.getInstance(this).openDataBase();
		
		//Set up our views
		this.name = (TextView) this.findViewById(R.id.result_phone_name);
		this.img = (ImageView)this.findViewById(R.id.result_phone_img);
		this.reasons = (TextView) this.findViewById(R.id.result_phone_reasons);
		
		//Set up our buttons
		this.next = (ImageButton) this.findViewById(R.id.next);
		this.prev = (ImageButton) this.findViewById(R.id.prev);
		this.approve = (ImageButton) this.findViewById(R.id.approve);
		this.disapprove = (ImageButton) this.findViewById(R.id.disapprove);
		this.restart = (ImageButton) this.findViewById(R.id.restart);
		
		//Preset them
		Intent x = this.getIntent();
		resultSet = new ArrayList<Result>();
		int count = (Integer) x.getSerializableExtra("result_count");
		for(int i = 0; i < count; i++){
			Result r = (Result) x.getSerializableExtra("result_" + i);
			resultSet.add(r);
		}
		resultIndex = 0;
		
		this.drawResult(); 
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.result, menu);
		return true;
	}
	
	/**
	 * Takes the name of the phone and the list of facts that generated the 
	 * result and composes them into a description string describing the 
	 * reasoning.
	 *  
	 * @param name The name of the phone itself.
	 * @param reasoning The list of facts.
	 * @return A description-of-reasoning string.
	 */
	private String formatReasoning(String name, ArrayList<Fact> reasoning) {
		String ret = "Why should you get the " + name + "?\n\n";
		String hasFact = "";
		try {
			for(Fact f : reasoning) {
				hasFact += "\t\t\t-" + f.getName() + "\t\t\t" +
						InferenceEngine.fuzzify(InferenceEngine.defuzzify(f)) + "\n";
			}
		}
		catch(Exception ex) {
			Log.e(this.getClass().getName(), "Unable to format reasoning");
		}
		 
		if(!hasFact.equals(""))
			ret = ret.concat("The phone scored the following:\n" + hasFact);
		return ret;
	}
	
	/**
	 * A method used to parse out information of the result and display it when 
	 * needed.
	 * 
	 * @param res The result to draw.
	 */
	public void drawResult(){
		Log.i("INDEX",""+this.resultIndex);
		
		Result res = this.resultSet.get(this.resultIndex);
		this.name.setText(res.getPhoneName());
		
		// TODO Add asynchronous image fetch from URL in the img path
		// Perhaps even scrolling through the 3-4 images the phone has online
		/*int resID = getResources().getIdentifier(res.getPrimaryImgPath(), "drawable",
				this.getPackageName());
		this.img.setImageBitmap(BitmapScaler.decodeSampledBitmapFromResource(getResources(), resID, 100, 100));*/
		try{
			// TODO daniel getFactsLeadingToResult
			this.reasons.setText(
					formatReasoning(res.getPhoneName(), 
					res.getReasoning()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		this.reasons.setMovementMethod(new ScrollingMovementMethod());
	}

	/**
	 * This is the restart function. It restarts the questionnaire.
	 * 
	 * @param v The button pressed.
	 */
	public void startOver(View v) {
		Intent intent = new Intent(this, QuestionActivity.class);
		this.startActivity(intent);
		this.finish();
	}
	
	/**
	 * This function shows a user the next applicable result.
	 * 
	 * @param v The button pressed.
	 */
	public void loadNextResult(View v) {
		if(resultIndex < this.resultSet.size() - 1){
			this.resultIndex++;
			if(resultIndex == this.resultSet.size() - 1){
				this.next.setVisibility(View.INVISIBLE);
			}
		}
			
		
		
		this.drawResult();
	}
	
	/**
	 * This function shows a user the previous applicable result.
	 * 
	 * @param v The button pressed.
	 */
	public void loadPrevResult(View v) {
		if(resultIndex > 0){
			this.resultIndex--;
			if(this.resultIndex == 0)
				this.prev.setVisibility(View.INVISIBLE);
		}
		this.drawResult();
	}
	
	/**
	 * This is the learning function if they approve a given result.
	 * 
	 * @param v The button pressed.
	 */
	public void approve(View v) {
		PhoneDataBaseHelper.getInstance(null).openWriteableDataBase();
		PhoneDataBaseHelper.getInstance(null).applyLearning(
				this.resultSet.get(resultIndex), true);
	}
	
	/**
	 * This is the learning function if they disapprove a given result.
	 * 
	 * @param v The button pressed.
	 */
	public void disapprove(View v) {
		PhoneDataBaseHelper.getInstance(null).openWriteableDataBase();
		PhoneDataBaseHelper.getInstance(null).applyLearning(
				this.resultSet.get(resultIndex), false);
	}
}
