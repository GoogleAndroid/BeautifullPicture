package com.epartner.picsee;

import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class Splash extends Activity {
	
	private final long WAITING_TIME = 5000;
	
	private int[] splashImages = {
		R.drawable.splash01,
		R.drawable.splash02,
		R.drawable.splash03,
		R.drawable.splash04
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		
		ImageView splash = (ImageView) findViewById(R.id.splash);			
		Random r = new Random(new Date().getTime());
		int index = r.nextInt(splashImages.length);				
		splash.setImageResource(splashImages[index]);
		
		//load daily update pictures
		new Thread(new Runnable() {
			
			@Override
			public void run() {
								
				
				long start = new Date().getTime();
				
				long now = new Date().getTime();
				long needWait = WAITING_TIME - (now - start);
				try {
					Thread.sleep(needWait);
				} catch (InterruptedException ignore) {} 
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Intent intent = new Intent(Splash.this, MainActivity.class);
						startActivity(intent);	
					}
				});
			}
		}).start();
	}
}
