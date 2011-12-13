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
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

public class ImageActivityHandler extends Activity implements Callback{
	
	private Handler mainHandler;
	private ImageHandler mLoadingHandler;
	private Worker mWorker;
	
    @Override
	public void onCreate(Bundle extras){
		
		super.onCreate(extras);
		setContentView(R.layout.image_layout);
		
		mainHandler = new Handler(this);
		
		mWorker = new Worker("Image Loader");
		mLoadingHandler = new ImageHandler(mWorker.getLooper());
		
		Message msg = mLoadingHandler.obtainMessage();
		msg.obj = getIntent().getExtras().getString("url");
		mLoadingHandler.sendMessage(msg);
	}
	
    @Override
	public void onDestroy(){
		super.onDestroy();
		mWorker.quit();
		mLoadingHandler = null;
	}
	
	//=========Handler Code================
    @Override
	public boolean handleMessage(Message msg) {
		ImageView iv = (ImageView)findViewById(R.id.remote_image);
		iv.setImageBitmap((Bitmap)msg.obj);
		return true;
	}
	
	//========= Custom Handler Code ===========
	private class ImageHandler extends Handler{
		public ImageHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {
        	try{
        		String url_string = (String)msg.obj;
        		URL url = new URL(url_string);
        		HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
        		if(httpCon.getResponseCode() != 200)
        			throw new Exception("Failed to connect");
        		InputStream is = httpCon.getInputStream();
			
        		Bitmap bt = BitmapFactory.decodeStream(is);
        		mainHandler.sendMessage(mainHandler.obtainMessage(0, (Object)bt));
        	}catch(Exception e){
        		
        	}
        	
        }
	}
	
	//======== Custom Worker ==========
	private static class Worker implements Runnable {
        private final Object mLock = new Object();
        private Looper mLooper;
        
        /**
         * Creates a worker thread with the given name. The thread
         * then runs a {@link android.os.Looper}.
         * @param name A name for the new thread
         */
        Worker(String name) {
            Thread t = new Thread(null, this, name);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            synchronized (mLock) {
                while (mLooper == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        public Looper getLooper() {
            return mLooper;
        }
        
        public void run() {
            synchronized (mLock) {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLock.notifyAll();
            }
            Looper.loop();
        }
        
        public void quit() {
            mLooper.quit();
        }
    }
}
