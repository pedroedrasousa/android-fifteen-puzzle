package com.pedroedrasousa.fifteenpuzzle;

import java.util.LinkedList;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.pedroedrasousa.engine.Camera;
import com.pedroedrasousa.engine.EngineActivity;
import com.pedroedrasousa.engine.EngineGLSurfaceView;
import com.pedroedrasousa.engine.FrameBuffer;
import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.RendererObserver;
import com.pedroedrasousa.engine.Scene;
import com.pedroedrasousa.engine.Timer;
import com.pedroedrasousa.engine.UniqueColorFactory;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.FrameRateControler;
import com.pedroedrasousa.engine.gui.GuiElement;


public class FifteenPuzzle implements Renderer, OnTouchListener, SensorEventListener {

	private static final boolean	DEBUG	= true;
	private static final String		TAG		= "FifteenPuzzle";

	private EngineActivity		mActivity;

	private EngineGLSurfaceView	mGLSurfaceView;
	private float				mDisplayDensity;

	private boolean isSurfaceCreated;

	private Timer				mTimer;
	private Font				mFont;
	private FrameRateControler	mFramRateCtl;
	private GameBoard			mBoard;

	private Vector<Tile> mTiles = new Vector<Tile>();

	private int[] mViewport			= new int[4];	// Viewport information (x0, y0, x1, y1)

	private Vec3 mGyroscopeValues	= new Vec3();


	private Scene mScene;

	private LinkedList<RendererObserver> mRendererObserverList = new LinkedList<RendererObserver>();

	private FrameBuffer	mAuxFrameBuffer;

	private String mSurfaceId = new String();	// The OpenGL surface identifier, assigned on onSurfaceCreated.


	public FifteenPuzzle() {

		if (DEBUG) {
			Log.i(TAG, "FifteenPuzzle constructor");
		}

		mDisplayDensity=1.0f;

		mTimer			= new Timer();
		mFramRateCtl	= new FrameRateControler();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		if (DEBUG) {
			Log.i(TAG, "onSurfaceCreated");
		}

		mSurfaceId = String.valueOf(config.hashCode());

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		if (!isSurfaceCreated) {
			isSurfaceCreated = true;

			mFont = new Font(this, "arial.png", 0.9f * mDisplayDensity);

			//mScene = new MainMenu(mActivity, this, mFont);
            FifteenPuzzleScene fifteenPuzzleScene = new FifteenPuzzleScene(this);
            fifteenPuzzleScene.setGameId("classic", "4x4");
            fifteenPuzzleScene.restartCurrentGame();
            mScene = fifteenPuzzleScene;
            this.registerObserver(fifteenPuzzleScene);

		}

		// Framebuffer will be recreated.
		if (mAuxFrameBuffer != null) {
			mAuxFrameBuffer.destroy();
			mAuxFrameBuffer = null;
		}

		// Notify the observers.
		for (RendererObserver observer : mRendererObserverList)
			observer.onSurfaceCreated(gl, config);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		if (DEBUG) {
			Log.i(TAG, "onSurfaceChanged");
		}

		mViewport[0] = 0;
		mViewport[1] = 0;
		mViewport[2] = width;
		mViewport[3] = height;

		mAuxFrameBuffer = new FrameBuffer(this, width, height);

		// Notify the observers
		for (RendererObserver observer : mRendererObserverList)
			observer.onSurfaceChanged(gl, width, height);
	}

	public Tile createTile(int number, int x, int y) {

		int nbrBoardSquaresX = mBoard.getWidth();
		int nbrBoardSquaresY = mBoard.getHeight();

		Tile tile = TileFactory.buildTile(number, nbrBoardSquaresX, nbrBoardSquaresY);
		tile.setPosX(x);
		tile.setPosY(y);

		mBoard.setObj(x, y, tile);
		mTiles.add(tile);

		return tile;
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		mFramRateCtl.FrameStart();
		if (mScene != null) {
			mScene.render();
			return;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mScene != null) {
			mScene.onTouch(view, event);
		}
		return true;
	}

	public void cleanGameBoard() {
		mTiles.clear();
		UniqueColorFactory.resetColor();
	}

	@Override
	public void onResume() {
		mTimer.resume();
	}

	@Override
	public void onPause() {
		mTimer.pause();
	}

	@Override
	public int getViewportWidth() {
		return mViewport[2];
	}

	@Override
	public int getViewportHeight() {
		return mViewport[3];
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_GYROSCOPE:
			mGyroscopeValues.assign(event.values);
			break;
		}
	}

	@Override
	public void onSurfaceDestroyed() {
		;
	}

	@Override
	public void onScreenOnOffToggled(boolean isScreenOn) {
		;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		;
	}




	@Override
	public void registerObserver(RendererObserver observer) {
		mRendererObserverList.add(observer);
	}

	@Override
	public void unRegisterObserver(RendererObserver observer) {
		mRendererObserverList.remove(observer);
	}

	@Override
	public void setScene(Scene scene) {
		UniqueColorFactory.resetColor();
		mScene = scene;
	}

	@Override
	public void onBackPressed() {
		if (mScene != null) {
			mScene.onBackPressed();
		}
	}

	@Override
	public void queueEvent(Runnable r) {
		mGLSurfaceView.queueEvent(r);
	}

	@Override
	public float getFrameFactor() {
		return mFramRateCtl.getFrameFactor();
	}

	@Override
	public FrameBuffer requestAuxFrameBuffer() {
		return mAuxFrameBuffer;
	}

	public void terminate() {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				mActivity.terminateWithInterstitial();
			}
		});
	}

	@Override
	public float getDisplayDensity() {
		return mDisplayDensity;
	}

	@Override
	public int[] getViewport() {
		return mViewport;
	}

	@Override
	public String getSurfaceID() {
		return mSurfaceId;
	}

	@Override
	public void setEngineGLSurfaceView(EngineGLSurfaceView glSurfaceView) {
		mGLSurfaceView = glSurfaceView;
	}

	@Override
	public void setActivity(EngineActivity activity) {
		mActivity = activity;
	}

	@Override
	public EngineActivity getActivity() {
		return mActivity;
	}

	@Override
	public FrameRateControler getFrameRateControler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addGuiElement(GuiElement guiElement) {

	}

	@Override
	public boolean disposeGuiElement(GuiElement guiElement) {
		return false;
	}

	@Override
	public Camera getCamera() {
		return null;
	}
}
