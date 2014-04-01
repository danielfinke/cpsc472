package ca.unbc.cpsc472.mynextphone;

import java.util.ArrayList;
import java.util.List;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AddSetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_set);
		PhoneDataBaseHelper.getInstance(this).openDataBase();
		
		ArrayList<String> values = new ArrayList<String>();
		values.add("New");
		int i = 0;
		
		while(true){
			List<String> group = PhoneDataBaseHelper.getInstance(this).getLinguisticNames(i++);
			if(group.size() == 0)
				break;
			values.add(group.toString());
		}
		
		Spinner valueSpinner = (Spinner) findViewById(R.id.group_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				values);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		valueSpinner.setAdapter(adapter);
		final ViewGroup mtxSizeView = (ViewGroup) findViewById(R.id.mtx_size);
		final ViewGroup sliderView = (ViewGroup) findViewById(R.id.sliders);
		final Context context = this;
		valueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	String groupName = (String)parentView.getItemAtPosition(position);
		    	sliderView.removeAllViews();
		    	mtxSizeView.removeAllViews();
		    	if("New".equalsIgnoreCase(groupName)){
	    			
		    		//create size of mtx box
		    		ViewGroup text = (ViewGroup)LayoutInflater.from(getBaseContext()).inflate(R.layout.size_of_mtx,
		    			mtxSizeView, true);
		    		final EditText field = (EditText)text.findViewById(R.id.name_field);
		    		
		    		field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    	        @Override
		    	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	        	InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		    	            if (imm != null) {
		    	                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		    	            } 
		    	        	sliderView.removeAllViews();
		    	            if (actionId == EditorInfo.IME_ACTION_DONE) {
		    	            	try{
		    	            		int j = Integer.parseInt(v.getText().toString());
		    	            	
		    	            		for(int i = 0; i<j; i++){
		    	            			ViewGroup text = (ViewGroup)LayoutInflater.from(getBaseContext()).inflate(R.layout.value_slider,
		    		    		    			sliderView, true);
		    		    				final TextView perc = ((TextView)text.findViewWithTag("std_slider_percentage"));
		    		    				SeekBar seek = (SeekBar)text.findViewWithTag("std_slider_bar");
		    		    				perc.setTag("perc"+i);
		    		    				seek.setTag("seek"+i);
		    		    				text.setTag("slider"+i);
		    		    				
		    		    				perc.setText("0.00");
		    		    				
		    		    				seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
		    								@Override
		    								public void onProgressChanged(SeekBar skbr, int progress,
		    										boolean fromUser) {
		    									//Get our concrete value and print it nicely
		    									if(progress == 100)
		    										perc.setText("1.00");
		    									else if(progress < 10)
		    										perc.setText("0.0" + progress);
		    									else
		    										perc.setText("0." + progress);
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
		    		    			}
		    	            	}catch(Exception e){
		    	            		Log.e("AddSetActivity.mtx_field", e.toString());
		    	            	}
		    	                return true;
		    	            }
		    	            return false;
		    	        }

		    	    });
		    		
		    	} else {
		    		//load sliders
		    		groupName = groupName.replace("]", "").split(", ")[0].substring(1);
		    		try{
		    			int groupSize = PhoneDataBaseHelper.getInstance(context).getLinguisticTuples(groupName).size();
		    			Log.i("kdjn", ""+groupSize);
		    			for(int i = 0; i<groupSize; i++){
		    				ViewGroup text = (ViewGroup)LayoutInflater.from(getBaseContext()).inflate(R.layout.value_slider,
		    		    			sliderView, true);
		    				final TextView perc = ((TextView)text.findViewWithTag("std_slider_percentage"));
		    				SeekBar seek = (SeekBar)text.findViewWithTag("std_slider_bar");
		    				perc.setTag("perc"+i);
		    				seek.setTag("seek"+i);
		    				text.setTag("slider"+i);
		    				
		    				perc.setText("0.00");
		    				
		    				seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
								@Override
								public void onProgressChanged(SeekBar skbr, int progress,
										boolean fromUser) {
									//Get our concrete value and print it nicely
									if(progress == 100)
										perc.setText("1.00");
									else if(progress < 10)
										perc.setText("0.0" + progress);
									else
										perc.setText("0." + progress);
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
		    				
		    			}
		    		}catch(Exception e){
		    			Log.e("AddSetActivity", e.toString());
		    		}
		    	}
		    	
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // should do nothing, we dont want to be able to unselect
		    }

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_set, menu);
		return true;
	}

	public void addSet(View v){
		EditText name = (EditText)findViewById(R.id.name_field);
		String nameString = name.getText().toString().toUpperCase().replace(" ", "_");
		if(nameString != null){
			ViewGroup sliderView = (ViewGroup) findViewById(R.id.sliders);
			int i =0;
			ArrayList<Double> values = new ArrayList<Double>();
			while(true){
				TextView text = (TextView)sliderView.findViewWithTag("perc"+i++);
				if(text == null)
					break;
				values.add(Double.parseDouble(text.getText().toString()));
			}
			
			double[] vals = new double[values.size()];
			for(int j = 0; j<vals.length; j++){
				vals[j] = values.get(j);
			}
			i = 0;
			while(true){
				List<String> group = PhoneDataBaseHelper.getInstance(this).getLinguisticNames(i);
				Spinner valueSpinner = (Spinner) findViewById(R.id.group_spinner);
				
				if(group.size() == 0 || group.contains(valueSpinner.getSelectedItem().toString().replace("]", "").split(", ")[0].substring(1)))
					break;
				i++;
			}
			
			PhoneDataBaseHelper.getInstance(this).addSet(nameString, i, vals);
		}
		this.finish();
	}
}
