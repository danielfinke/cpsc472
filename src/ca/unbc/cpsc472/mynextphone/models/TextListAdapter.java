package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TextListAdapter extends BaseAdapter {

	private ArrayList<QuestionAnswer> theItems;
	private Context context;
	
	public TextListAdapter(Context c, ArrayList<QuestionAnswer> theItems){
		this.theItems = theItems;
		this.context = c;
	}
	
	@Override
	public int getCount() {
		return theItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public QuestionAnswer getAnswer(int i){
		return theItems.get(i);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		return theItems.get(arg0).getView(context);
	}

}
