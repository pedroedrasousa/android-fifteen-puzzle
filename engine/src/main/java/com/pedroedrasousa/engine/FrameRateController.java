package com.pedroedrasousa.engine;

import android.os.SystemClock;

/**
 * Engine frame rate controller.
 *
 * @author Pedro Edra Sousa
 */
public class FrameRateController {

    /**
     * Default frame rate limiter value.
     */
	private static final int DEFAULT_MAX_FPS = 60;

    /**
     * Instance when the frame started being rendered.
     */
	private long frameStartTime;

    /**
     * Instance when the frame finished being rendered.
     */
	private long frameEndTime;

	/**
	 * Time between frames, in milliseconds.
	 */
	private float frameDelta;

	/**
	 * Instance when FPS was last updated.
	 */
	private long lastFPSUpdate;

	/**
	 * Inversed frame rate limit.
	 */
	private float oneOverFpsLimit;

	/**
	 * Frames Per Second.
	 */
	private int	fpsCounter;

    /**
     * Current frame factor relative to the frame rate limit.
     */
	private float frameFactor;
	
	public FrameRateController() {
		setFrameRateLimiter(DEFAULT_MAX_FPS);
		frameFactor = 1.0f;
	}
	
	public float getFrameFactor() {
		return frameFactor;
	}
	
	public int getFPS() {
		return fpsCounter;
	}
	
	public void setFrameRateLimiter(int maxFPS) {
		assert maxFPS > 0;
		if (maxFPS > 0)
			oneOverFpsLimit = 1000.0f / maxFPS;
	}
	
	public void frameStart() {
		frameEndTime = SystemClock.uptimeMillis();
		frameDelta = frameEndTime - frameStartTime;
		frameStartTime = SystemClock.uptimeMillis();
		
		// Frame rate limiter
		if (frameDelta < oneOverFpsLimit) {
			try {
				Thread.sleep((int)oneOverFpsLimit - (int)frameDelta);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Refresh once every second.
		if (frameEndTime - lastFPSUpdate > 1000.0f) {
			lastFPSUpdate = frameEndTime;
			fpsCounter = (int)(1000.0f / frameDelta);
			
			// Get current frame factor.
			float currentFrameFactor = frameDelta / oneOverFpsLimit;
			// Limit the frame factor value.
			currentFrameFactor = Math.min(currentFrameFactor, 1.5f);
			currentFrameFactor = Math.max(currentFrameFactor, 0.5f);
			
			// Gradually adjust frame factor.
			frameFactor = 0.75f * frameFactor + 0.25f * currentFrameFactor;
		}
	}
}
