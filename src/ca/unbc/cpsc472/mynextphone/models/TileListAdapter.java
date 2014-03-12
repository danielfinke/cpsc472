package ca.unbc.cpsc472.mynextphone.models;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TileListAdapter extends BaseAdapter {

	private ArrayList<QuestionAnswer> answers;
	private Context context;
	
	public TileListAdapter(Context context, ArrayList<QuestionAnswer> answers){
		this.answers = answers;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return answers.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View ret = null;

		ret = answers.get(arg0).getView(context);
		
		return ret;
	}
	
	public QuestionAnswer answerFor(int index){
		return answers.get(index);
	}

}
