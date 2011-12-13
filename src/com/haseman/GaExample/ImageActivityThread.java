/*
 * Copyright (C) 2011 Chris Haseman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haseman.GaExample;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageActivityThread extends Activity{
	private Bitmap bitmap;
	ImageDownloader id;
	
	@Override
	public void onCreate(Bundle extras){
		super.onCreate(extras);
		setContentView(R.layout.image_layout);
		setTitle("My Image!");
		
		bitmap = (Bitmap)getLastNonConfigurationInstance();
		if(bitmap != null){
			ImageView iv = (ImageView)findViewById(R.id.remote_image);
			iv.setImageBitmap(bitmap);
			return;
		} else {
		    id = new ImageDownloader();
		    id.execute(getIntent().getExtras().getString("url"));
        }
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(id!=null){
			id.cancel(true);
			if(id.pd != null)
				id.pd.dismiss();
		}
	}
	
	private class ImageDownloader extends AsyncTask<String, Integer, Bitmap>{

		ProgressDialog pd;
		
        @Override
		protected void onPreExecute(){
			//Setup is done here
			pd = ProgressDialog.show(ImageActivityThread.this, "loading image", null);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				Thread.sleep(1000);
				URL url = new URL(params[0]);
				HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
				if(httpCon.getResponseCode() != 200)
					throw new Exception("Failed to connect");
				InputStream is = httpCon.getInputStream();

				return BitmapFactory.decodeStream(is);
			}catch(Exception e){
				Toast.makeText(ImageActivityThread.this, e.getMessage(), Toast.LENGTH_LONG).show();
				Log.e("Image","Failed to load image",e);
			}
			return null;
		} 
		
		@Override
		protected void onPostExecute(Bitmap img){
			bitmap = img;
			ImageView iv = (ImageView)findViewById(R.id.remote_image);
			if(iv!=null && img!=null){
				iv.setImageBitmap(img);
			}
			pd.dismiss();
			pd=null;
		}
		
        @Override
		protected void onCancelled(){
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance(){
		super.onRetainNonConfigurationInstance();
		if(bitmap != null)
			return bitmap;
		return null;
	}

}
