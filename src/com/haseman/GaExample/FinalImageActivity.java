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

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class FinalImageActivity extends Activity{

	ProgressDialog pd;

    @Override
	public void onCreate(Bundle extras){
		super.onCreate(extras);
		setContentView(R.layout.image_layout);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImageIntentService.TRANSACTION_DONE);
		registerReceiver(imageReceiver, intentFilter);
		
		Intent i = new Intent(this, ImageIntentService.class);
		i.putExtra("url", getIntent().getExtras().getString("url"));
		startService(i);	
		
		pd = ProgressDialog.show(this, "Fetching Imge", "Go intent service go!"); 
	}
	
    @Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(imageReceiver);
	}
	
	private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String location = intent.getExtras().getString("location");
			if(location == null || location.length() ==0){
				Toast.makeText(context, "Failed to download image", Toast.LENGTH_LONG).show();
			}
			File imageFile = new File(location);
			if(!imageFile.exists()){
				pd.dismiss();
				Toast.makeText(context, "Unable to Download file :-(", Toast.LENGTH_LONG);
				return;
			}
			
			Bitmap b = BitmapFactory.decodeFile(location);
			ImageView iv = (ImageView)findViewById(R.id.remote_image);
			iv.setImageBitmap(b);
			pd.dismiss();
		}
	};
}
