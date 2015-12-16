package com.pedroedrasousa.engine.object3d;

import java.nio.Buffer;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.RendererObserver;
import static com.pedroedrasousa.engine.EngineGlobal.DEBUG;

public class VBOData implements RendererObserver {
	
	private static final String	TAG	= "VBOData";
	
	public static final int BYTES_PER_FLOAT = 4;
	public static final int BYTES_PER_SHORT = 2;
	
	protected VertexData mVertexData;
	protected int mHandle[] = new int[2];
	
	private static HashMap<String, VBOHandlers> mCache = new HashMap<String, VBOHandlers>();

	private String mValidSurfaceID;	// The active EGLConfig when the data was loaded to OpenGL. Used to know when to reload data during onSurfaceCreated.
	
	private Renderer mRenderer;
	
	public VBOData(Renderer renderer, VertexData vertexData) {
		mRenderer = renderer;
		mRenderer.registerObserver(this);
		setVertexData(vertexData);
		loadVBOVertexData();
	}
	
	public void setVertexData(VertexData vertexData) {
		mVertexData = vertexData;
	}

	public VertexData getVertexData() {
		return mVertexData;
	}
	
	/**
	 * Must be invoked every time after OpenGL context is destroyed.
	 */
	public void loadVBOVertexData() {

		if (mCache.get(constructID()) != null) {
			if (DEBUG) {
				Log.i(TAG, "Reusing VBO handle " + constructID());
			}
			mHandle[0] = mCache.get(constructID()).vertices;
			mHandle[1] = mCache.get(constructID()).indices;
			return;
		} else {
			if (DEBUG) {
				Log.i(TAG, "Loading VBO " + constructID());
			}
		}
		
		Buffer vertexBuffer	= mVertexData.getVertexBuffer();
		Buffer indexBuffer	= mVertexData.getIndexBuffer();
		
		// Generate two buffer objects, one for the vertex data, other for the indices.
		GLES20.glGenBuffers(2, mHandle, 0);
		
		vertexBuffer.position(0);
		// Create a new data store for the vertex data buffer.
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mHandle[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		indexBuffer.position(0);
		// Create a new data store for the index buffer.
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mHandle[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT, indexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		mCache.put(constructID(), new VBOHandlers(mHandle[1], mHandle[0]));
	}
	
	public void bindVBO() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mHandle[0]);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mHandle[1]);
	}
	
	public void unbindVBO() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public int getVerticesVBO()	{ return mHandle[0];	}
	public int getIndicesVBO()	{ return mHandle[1];	}
	
	public class VBOHandlers {
		public int indices;
		public int vertices;
		public VBOHandlers(int indices, int vertices) {
			this.indices	= indices;
			this.vertices	= vertices;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (mRenderer.getSurfaceID() != mValidSurfaceID) {
			mCache.remove(mVertexData.getId() + "@" + mValidSurfaceID);
			mValidSurfaceID = mRenderer.getSurfaceID();
		}
		
		loadVBOVertexData();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		;
	}
	
	private String constructID() {
		return mVertexData.getId() + "@" + mRenderer.getSurfaceID();
	}
}
