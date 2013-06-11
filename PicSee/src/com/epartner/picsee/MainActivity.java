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

	private static final String DEBUG_TAG = "Gestures";
	
	private GestureDetectorCompat gestureDetector;
	
	private ArrayList<String> picturesUrls = new ArrayList<String>();
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//getActionBar().hide();
		
		gestureDetector = new GestureDetectorCompat(this, this);
		
		getPicUrls();
		
		showNext();
	}
	
	@Override 
    public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}
	
	private void getPicUrls() {
		try {
			downloadContent("http://192.168.2.104:8080/pic/content.json");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		}
		
	}
	
	private void showNext() {
		downloadPicture(picturesUrls.get(index));
		index = (index + 1) % picturesUrls.size();
	}
	private void showPre() {
		if(index == 0) {
			index = picturesUrls.size() - 1;
		} else {
			index--;
		}
		downloadPicture(picturesUrls.get(index));
		
	}
	
	private void downloadPicture(final String url) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "download...");
		dialog.show();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					final Bitmap downloadBitmap = downloadBitmap(url);
					final ImageView imageView = (ImageView) findViewById(R.id.image);
					runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            InputStream input = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            StringBuilder builder = new StringBuilder();
            while ( (line = reader.readLine()) != null ) {
            	builder.append(line);
            }
            Log.d(DEBUG_TAG, builder.toString());
            JSONObject jsonObject = new JSONObject(builder.toString());
            Log.d(DEBUG_TAG, "category = " + jsonObject.getString("category"));
            Log.d(DEBUG_TAG, "domain = " + jsonObject.getString("domain"));
            Log.d(DEBUG_TAG, "class of pictures = " + jsonObject.get("pictures").getClass());
            String domain = jsonObject.getString("domain");
            
            JSONArray pics = jsonObject.getJSONArray("pictures");
            for(int i = 0; i < pics.length(); i++) {
            	JSONObject o = pics.getJSONObject(i);
            	picturesUrls.add(domain + o.getString("path")); 
            }
            index = 0;
            
            
        } else {
            throw new IOException("Download failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
		
		return null;
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
			showNext();
		} else if (distance < -50 ) {
			showPre();
		}
		
		return true;
	}
	

}
