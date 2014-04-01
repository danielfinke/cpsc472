package ca.unbc.cpsc472.mynextphone;

import ca.unbc.cpsc472.mynextphone.database.PhoneDataBaseHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		PhoneDataBaseHelper.getInstance(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}
	
	public void begin(View v){
		Intent i = new Intent(this, QuestionActivity.class);
		this.startActivity(i);
	}

	public void changeDefinitions(View v){
		Intent i = new Intent(this, ChangeDefinitionsActivity.class);
		this.startActivity(i);
	}
}
