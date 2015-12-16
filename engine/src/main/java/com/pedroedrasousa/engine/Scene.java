package com.pedroedrasousa.engine;

import android.view.MotionEvent;
import android.view.View;

public interface Scene {
	void render();
	void onResume();
	void onPause();
	void onBackPressed();
	boolean onTouch(View view, MotionEvent event);
}
