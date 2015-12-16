package com.pedroedrasousa.engine;

import android.util.AttributeSet;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Engine OpenGL surface view.
 *
 * @author Pedro Edra Sousa
 */
public class EngineGLSurfaceView extends GLSurfaceView {

    /**
     * Touch event listener.
     */
	private OnTouchListener onTouchListener;

    /**
     * Creates a new instance of <code>EngineGLSurfaceView</code>
     *
     * @param context The Android context.
     */
	public EngineGLSurfaceView(Context context)  {
		super(context);	
	}

    /**
     * Creates a new instance of <code>EngineGLSurfaceView</code>
     *
     * @param context The Android context.
     * @param attrs An Android collection of attributes.
     */
	public EngineGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

    /**
     * Sets the renderer.
     *
     * @param renderer The renderer.
     */
	public void setRenderer(Renderer renderer)  {
		super.setRenderer(renderer);
	}

    /**
     * Sets the touch event listener.
     *
     * @param onTouchListener The touch event listener.
     */
    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    /**
     * Handles touch screen motion events.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		if (onTouchListener != null) {
            onTouchListener.onTouch(null, event);
        }
		return super.onTouchEvent(event);
	}
}
