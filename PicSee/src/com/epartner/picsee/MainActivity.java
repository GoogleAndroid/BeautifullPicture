package com.epartner.picsee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener{

	private static final String DEBUG_TAG = "MainActivity DEBUG";
	private static final String ERROR_TAG = "MainActivity ERROR";
	
	private GestureDetectorCompat gestureDetector;
	
	private ArrayList<String> picturesUrls = new ArrayList<String>();
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().hide();
		
		gestureDetector = new GestureDetectorCompat(this, this);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				getPicUrls();
				
				runOnUiThread(new Runnable() {
					public void run() {
						showNextPic();
					}
				});
			}
		}).start();
	}
	
	@Override 
    public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}
	
	// synchronized use to ensure the data updated in this method is visibility in other thread.
	private synchronized void getPicUrls() { 
		ArrayList<String> urls = null;
		try {
			urls = downloadContent("http://192.168.2.104:8080/pic/content.json");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(ERROR_TAG, "down load content failed, caused by:" + e);
		}
		if (urls == null || urls.size() == 0) {
			// TODO: load local resource
			urls = new ArrayList<String>();
		} 
		picturesUrls = urls;
		index = -1;	
	}
	
	private synchronized String getNextUrl() {
		index = (index + 1) % picturesUrls.size();
		String result = picturesUrls.get(index);
		return result;
	}
	
	private synchronized String getPreviousUrl() {
		if ( index == 0 ) {
			index = picturesUrls.size() - 1;
		} else {
			index--;
		}
		return picturesUrls.get(index);
		
	}
	
	private void showNextPic() {
		if (picturesUrls.size() == 0) {
			Log.d(ERROR_TAG, "no pic is loaded.");
			//
			return;
		}
		downloadPicture(getNextUrl());
	}
	private void showPre() {
		if(picturesUrls.size() == 0) {
			// TODO: need decide how to handle this condition
			return;
		}
		downloadPicture(getPreviousUrl());
		
	}
	
	private void downloadPicture(final String url) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "download...");
		dialog.show();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					final Bitmap downloadBitmap = downloadBitmap(url);
					runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	ImageView imageView = (ImageView) findViewById(R.id.image);
                            imageView.setImageBitmap(downloadBitmap);
                        }
                    });
				} catch (IOException  e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					dialog.dismiss();
				}
			}
		}).start();
	}
	
	private HttpResponse download(String url) throws IOException {
		HttpUriRequest req = new HttpGet(url);
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpResponse resp = httpClient.execute(req);
		
		return resp;
	}
	
	private ArrayList<String> downloadContent(String url) throws IOException, JSONException {
		
		HttpResponse response = download(url);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        
        if( statusCode != 200) {
        	Log.d(DEBUG_TAG, "get content json failed. status code is " + statusCode);
            throw new IOException("Download content json failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
        ArrayList<String> resultList = new ArrayList<String>();
        
        String cotent = getContent(response);
        
        JSONObject jsonObject = new JSONObject(cotent);
        
        String domain = jsonObject.getString("domain");
        JSONArray pics = jsonObject.getJSONArray("pictures");
        for(int i = 0; i < pics.length(); i++) {
        	JSONObject o = pics.getJSONObject(i);
        	resultList.add(domain + o.getString("path")); 
        }
		
		return resultList;
	}

	private String getContent(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
        InputStream input = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        StringBuilder builder = new StringBuilder();
        while ( (line = reader.readLine()) != null ) {
        	builder.append(line);
        }
        Log.d(DEBUG_TAG, builder.toString());
		return builder.toString();
	}
	
	private Bitmap downloadBitmap(String url) throws IOException {
       
		HttpResponse response = download(url);
		
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
 
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                    bytes.length);
            return bitmap;
        } else {
            throw new IOException("Download failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
		float distance = e1.getX() - e2.getX();
		Log.d(DEBUG_TAG, "fling distance is " + distance );
		if( distance > 50 ) {
			showNextPic();
		} else if (distance < -50 ) {
			showPre();
		}
		
		return true;
	}
	

}
