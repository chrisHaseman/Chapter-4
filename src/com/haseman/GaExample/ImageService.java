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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class ImageService extends Service{

	public static final String TRANSACTION_DONE = "com.haseman.gaExample.TRASACTION_DONE";
	
	volatile String activeUrl=null;
	File tempDir;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		
		String url = intent.getExtras().getString("url");
		
		if(activeUrl==null || !url.equals(activeUrl) ){
			new Thread(new ImageDownloader(url, intent.getExtras())).start();
		}
		return Service.START_NOT_STICKY;
	}
	private class ImageDownloader implements Runnable{
		
		String remoteUrl;
		String location;
		Bundle extras;
		
		public ImageDownloader(String url, Bundle intentBundle){
			this.remoteUrl = url;
			extras = intentBundle;
		}
		
		public void run(){
			//Check if it's already there
			String filename = remoteUrl.substring(remoteUrl.lastIndexOf("/")+1);
			File tmp = new File(tempDir.getPath() +"/"+filename);
			if(tmp.exists()){
				location = tmp.getAbsolutePath();
				notifiyFinished();
				stopSelf();
				return;
			}
			//If we can't create the file here, try the tmp location
			try{
				activeUrl = remoteUrl;
				URL url = new URL(remoteUrl);
				HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
				if(httpCon.getResponseCode() != 200)
					throw new Exception("Failed to connect");
				InputStream is = httpCon.getInputStream();
				FileOutputStream fos = new FileOutputStream(tmp);
				writeStream(is, fos);
				fos.flush(); fos.close();
				is.close();
				location = tmp.getAbsolutePath();
				notifiyFinished();
				
			}catch(Exception e){
				location = "";
				notifiyFinished();
				Log.e("Service","Failed!",e);
			}
		}
		
		private void writeStream(InputStream is, OutputStream fos) throws Exception{
			byte buffer[] = new byte[80000];
			int read = is.read(buffer);
			while(read != -1){
				fos.write(buffer, 0, read);
				read = is.read(buffer);
			}
		}
		
		private void notifiyFinished(){
			activeUrl = "";
			Intent i = new Intent(TRANSACTION_DONE);
			i.putExtra("location", location);
			i.putExtra("startData", extras);
			ImageService.this.sendBroadcast(i);
			ImageService.this.stopSelf();
		}
	}
	public void onCreate(){
		super.onCreate();
		String tmpLocation = Environment.getExternalStorageDirectory().getPath() + "/gaTmp/";
		tempDir = new File(tmpLocation);
		if(!tempDir.exists()){
			tempDir.mkdir();
		}
		
	}

}
