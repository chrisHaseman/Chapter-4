package com.haseman.GaExample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class ImageIntentService extends IntentService{

	private static final String CACHE_FOLDER = "/myApp/image_cache/";
	
	public static final String TRANSACTION_DONE = "com.haseman.TRASACTION_DONE";
	
	File cacheDir;
	
	public ImageIntentService() {
		super("ImageIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String remoteUrl = intent.getExtras().getString("url");
		String location;
		String filename = 
			remoteUrl.substring(remoteUrl.lastIndexOf("/")+1);
		File tmp = new File(cacheDir.getPath() +"/"+filename);
		if(tmp.exists()){
			location = tmp.getAbsolutePath();
			notifiyFinished(location, remoteUrl);
			stopSelf();
			return;
		}
		//If we can't create the file here, try the tmp location
		try{
			URL url = new URL(remoteUrl);
			HttpURLConnection httpCon = 
				(HttpURLConnection)url.openConnection();
			if(httpCon.getResponseCode() != 200)
				throw new Exception("Failed to connect");
			InputStream is = httpCon.getInputStream();
			FileOutputStream fos = new FileOutputStream(tmp);
			writeStream(is, fos);
			fos.flush(); fos.close();
			is.close();
			location = tmp.getAbsolutePath();
			notifiyFinished(location, remoteUrl);
			
		}catch(Exception e){
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
	
	public void onCreate(){
		super.onCreate();
		String tmpLocation = Environment.getExternalStorageDirectory().getPath() + CACHE_FOLDER;
		cacheDir = new File(tmpLocation);
		if(!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		
	}

	private void notifiyFinished(String location, String remoteUrl){
		Intent i = new Intent(TRANSACTION_DONE);
		i.putExtra("location", location);
		i.putExtra("url", remoteUrl);
		ImageIntentService.this.sendBroadcast(i);
	}
}
