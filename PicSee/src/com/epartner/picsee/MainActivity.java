package com.epartner.picsee;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().hide();
		
		
		
		downloadPicture("http://10.170.78.219/pic/pic02.jpg");
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
	
	private Bitmap downloadBitmap(String url) throws IOException {
        HttpUriRequest request = new HttpGet(url.toString());
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
 
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
	
	

}
