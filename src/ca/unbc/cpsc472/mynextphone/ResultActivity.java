package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import ca.unbc.cpsc472.mynextphone.models.Fact;
import ca.unbc.cpsc472.mynextphone.models.Result;

public class ResultActivity extends Activity {

	private TextView name;
	private ImageView img;
	private TextView reasons;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		//Set up our views
		this.name = (TextView) this.findViewById(R.id.result_phone_name);
		this.img = (ImageView)this.findViewById(R.id.result_phone_img);
		this.reasons = (TextView) this.findViewById(R.id.result_phone_reasons);
		
		//Preset them
		Intent x = this.getIntent();
		Bundle b = x.getBundleExtra("result");
		Result res = (Result) b.getSerializable("ca.unbc.cpsc472.mynextphone." +
				"models.Result");
		this.name.setText(res.getPhoneName());
		this.img.setImageResource(R.drawable.cell_phone_placeholder);
		this.reasons.setText(
				formatReasoning(res.getPhoneName(), 
				res.getReasoning()));
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
	private String formatReasoning(String name, ArrayList<Fact> reasoning){
		String ret = "Why should you get the " + name + "?\n\n";
		for(Fact f : reasoning){
			ret += "\t\t\t-" + f.getName() + "\n";
		}
		return ret;
	}

}
