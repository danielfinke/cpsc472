package ca.unbc.cpsc472.mynextphone;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class ChangeMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_menu, menu);
		return true;
	}
	
	public void addRule(View v){
		Intent i = new Intent(this, ChangeDefinitionsActivity.class);
		this.startActivity(i);
	}

}
