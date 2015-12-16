package com.pedroedrasousa.engine;

import com.pedroedrasousa.engine.gui.GuiElement;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.opengl.*;

public interface Renderer extends GLSurfaceView.Renderer, RendererSubject, OnTouchListener {
	void onResume();
	void onPause();
	void onSurfaceDestroyed();
	boolean onTouch(View view, MotionEvent event);
	int getViewportWidth();
	int getViewportHeight();
	void onScreenOnOffToggled(boolean isScreenOn);
	void onBackPressed();
	void setScene(Scene scene);
	void queueEvent(Runnable r);
	float getFrameFactor();
	FrameBuffer requestAuxFrameBuffer();
	float getDisplayDensity();
	int[] getViewport();
	void setEngineGLSurfaceView(EngineGLSurfaceView glSurfaceView);
	EngineActivity getActivity();
	void setActivity(EngineActivity activity);
	void terminate();
	FrameRateController getFrameRateControler();
	void addGuiElement(GuiElement guiElement);
	boolean disposeGuiElement(GuiElement guiElement);
	Camera getCamera();
}
