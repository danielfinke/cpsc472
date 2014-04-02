package ca.unbc.cpsc472.mynextphone.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class PhotoDownloadTask extends AsyncTask<String, Void, Bitmap> {

	private ImageView imgView;
	
	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bmImg = null;
		try{
			URL url = new URL(params[0]);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();   
	        conn.setDoInput(true);   
	        conn.connect();     
	        InputStream is = conn.getInputStream();
	        bmImg = BitmapFactory.decodeStream(is);
		} catch (MalformedURLException mue){
			Log.i("Andrew", "Cue necessary expletives here");
		} catch (IOException mue){
			Log.i("Andrew", "Cue necessary expletives here");
		} 
		
		return bmImg;
	}
	
	protected void onPostExecute(Bitmap bmImg){
		imgView.setImageBitmap(bmImg);
	}
	
	public void setImageView(ImageView imgView){
		this.imgView = imgView;
	}

	
	
}
