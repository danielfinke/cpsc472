package ca.unbc.cpsc472.mynextphone;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ChangeDefinitionsActivity extends Activity {
	
	private int numIfs = 0;
	private int numThens = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_change_definitions);
		// Show the Up button in the action bar.
		setupActionBar();
		
		
		
		loadIfStatement();
		loadThenStatement();
	}
	
	private void loadIfStatement(){
		loadStatement(R.id.if_body);
	}
	
	private void loadThenStatement(){
		loadStatement(R.id.then_body);
	}
	
	private void loadStatement(int body){
		final PhoneDataBaseHelper helper = PhoneDataBaseHelper.getInstance(this);
		ViewGroup parent = (ViewGroup) findViewById(body);
		View statement = LayoutInflater.from(getBaseContext()).inflate(R.layout.value_is_set,
                parent, true);
		Spinner valueSpinner = (Spinner)statement.findViewWithTag("std_value_spinner");
		final Spinner setSpinner = (Spinner)statement.findViewWithTag("std_set_spinner");
		final Context me = this;
		
		valueSpinner.setTag("value_spinner");
		setSpinner.setTag("complete_value_spinner");
		
		valueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	
		    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, android.R.layout.simple_spinner_item,
						helper.getLinguisticNames(helper.getSetGroupingByValueName((String)parentView.getItemAtPosition(position))));
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				setSpinner.setAdapter(adapter);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // should do nothing, we dont want to be able to unselect
		    }

		});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				helper.getValueNames());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		valueSpinner.setAdapter(adapter);
	}
	
	public void addAnother(View v){
		this.loadStatement(((View)v.getParent().getParent()).getId());
		((ViewGroup)v.getParent()).removeView(v);
	}
	
	public void addRule(View v){
		//TODO: add the rule to the database go back.
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_definitions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
