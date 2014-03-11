package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.Result;

public class ResultActivity extends Activity{

	private TextView name;
	private ImageView img;
	private TextView reasons;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		//Set up our views
		this.name = (TextView) this.findViewById(R.id.result_phone_name);
		this.img = (ImageView)this.findViewById(R.id.result_phone_img);
		this.reasons = (TextView) this.findViewById(R.id.result_phone_reasons);
		
		//Preset them
		Intent x = this.getIntent();
		Result res = (Result) x.getSerializableExtra("result");
		this.name.setText(res.getPhoneName());
		
		int resID = getResources().getIdentifier(res.getImagePath(), "drawable",
				this.getPackageName());
		this.img.setImageResource(resID);
		PhoneDataBaseHelper helper = new PhoneDataBaseHelper(this);
		helper.openDataBase();
		try{
			this.reasons.setText(
					formatReasoning(res.getPhoneName(), 
					res.getReasoning(),
					helper.getFactsLeadingToResult(res.id)));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		helper.close();
		this.reasons.setMovementMethod(new ScrollingMovementMethod());
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
	private String formatReasoning(String name, ArrayList<Fact> reasoning, Set<Fact> reason){
		String ret = "Why should you get the " + name + "?\n\n";
		String hasFact = "";
		String oppositeFact = "";
		String leftOver = "";
		for(Fact f : reasoning){
			if(reason.contains(f))
				hasFact += "\t\t\t-" + f.toString() + "\n";
			else {
				f.toggleTruthFlag();
				if(reason.contains(f)){
					f.toggleTruthFlag();
					oppositeFact += "\t\t\t-" + f.toString() + "\n";
				}else{
					f.toggleTruthFlag();
					leftOver += "\t\t\t-" + f.toString() + "\n";
				}
			}
		}
		 
		if(!hasFact.equals(""))
			ret = ret.concat("These facts matched the phone:\n"+hasFact);
		if(!oppositeFact.equals(""))
			ret = ret.concat("These facts are not found in the phone:\n"+oppositeFact);
		if(!leftOver.equals(""))
			ret = ret.concat("These are left over:\n"+leftOver);
		return ret;
	}

	public void startOver(View v) {
		Intent intent = new Intent(this, QuestionActivity.class);
		this.startActivity(intent);
		this.finish();
	}
}
