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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageRawThread extends Activity{
	
	public void onCreate(Bundle extras){
		super.onCreate(extras);
		setContentView(R.layout.image_layout);
		new Thread(){
			public void run(){
				try{
					URL url = new URL(getIntent().getExtras().getString("url"));
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					if(httpCon.getResponseCode() != 200)
						throw new Exception("Failed to connect");
					InputStream is = httpCon.getInputStream();
					
					final Bitmap bt = BitmapFactory.decodeStream(is);
					
					ImageRawThread.this.runOnUiThread(new Runnable(){
						public void run(){
							ImageView iv = (ImageView)findViewById(R.id.remote_image);
							iv.setImageBitmap(bt);
						}
					});
				}catch(Exception e){
					
				}
			}
		}.start();
		
	}

}
