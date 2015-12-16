package com.pedroedrasousa.engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public interface RendererObserver {
	void onSurfaceCreated(GL10 gl, EGLConfig config);
	void onSurfaceChanged(GL10 gl, int width, int height);
}
