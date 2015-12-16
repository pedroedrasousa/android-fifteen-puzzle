package com.pedroedrasousa.engine.shader;

import static com.pedroedrasousa.engine.EngineGlobal.DEBUG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Hashtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.RendererObserver;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;

import android.content.Context;
import android.opengl.GLES20;


public abstract class AbstractShaderProg implements RendererObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractShaderProg.class.getSimpleName());
	
	private Renderer renderer;
	
	private int programHandle;
	private int vertexProgramHandle;
	private int fragmentProgramHandle;
	
	private String vertexShdResName = new String("N/A");
	private String fragmentShdResName = new String("N/A");
	
	private int vertexResourceId;
	private int fragmentResourceId;
	
	private Hashtable<String, Integer> uniformLocations = new Hashtable<>();
	private Hashtable<String, Integer> atribLocations = new Hashtable<>();
	
	// Cache to reuse the shader handles based on the filename.
	private static HashMap<String, Integer> mCache = new HashMap<>();
	
	private String validSurfaceID;	// The active EGLConfig when the data was loaded to OpenGL. Used to know when to reload data during onSurfaceCreated.

	
	public AbstractShaderProg(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
		this.renderer = renderer;
		createProgFromFile(renderer.getActivity(), vertexResourceId, fragmentResourceId);
		this.renderer.registerObserver(this);
	}
	
	public void reload() {
		createProgFromFile(renderer.getActivity(), vertexResourceId, fragmentResourceId);
	}
	
	private String readTextFile(final Context context, final int resourceId) {
		
		final InputStream inputStream				= context.getResources().openRawResource(resourceId);
		final InputStreamReader inputStreamReader	= new InputStreamReader(inputStream);
		final BufferedReader bufferedReader			= new BufferedReader(inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try {
			while ( (nextLine = bufferedReader.readLine()) != null ) {
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return body.toString();
	}

	public void createProgFromFile(Context context, int vertexResourceId, int fragmentResourceId) {
				
		String source;
		
		this.vertexResourceId = vertexResourceId;
		this.fragmentResourceId = fragmentResourceId;
				
		// Store the resource name where the shader programs where loaded from
		vertexShdResName = context.getResources().getResourceEntryName(vertexResourceId);
		fragmentShdResName = context.getResources().getResourceEntryName(fragmentResourceId);
		
		// Check if shader is already in cache.
		if (mCache.get(constructID()) != null) {
			if (DEBUG) {
				if (DEBUG) logger.debug("Reusing shader handle [{}]", constructID());
			}
			programHandle = mCache.get(constructID());
			return;
		} else {
			if (DEBUG) if (DEBUG) logger.debug("Loading shader [{}]", constructID());
		}

		source = readTextFile(context, vertexResourceId);
		compileVertexProgram(source);
		
		source = readTextFile(context, fragmentResourceId);
		compileFragmentProgram(source);
		
		link();
		
		mCache.put(constructID(), programHandle);
	}

	public int compile(String souce, int type) {
		
		String log = null;
		int shaderHandle = GLES20.glCreateShader(type);
		
		if (shaderHandle != 0) {
		    GLES20.glShaderSource(shaderHandle, souce);
		    GLES20.glCompileShader(shaderHandle);
		    final int[] compileStatus = new int[1];
		    GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		    
		    if (compileStatus[0] == 0) {
		    	// The compilation failed
		    	log = GLES20.glGetShaderInfoLog(shaderHandle);
		        GLES20.glDeleteShader(shaderHandle);
		        shaderHandle = 0;
		    }
		}
		 
		if (shaderHandle == 0) {
			String shaderType = new String((type == GLES20.GL_VERTEX_SHADER) ? "vertex" : "fragment");
			throw new RuntimeException("Error compiling " + shaderType + " shader program. Vertex program resource: " + vertexShdResName + " Vertex program resource: " + fragmentShdResName + " Log: \n"  + log);
		}
		
		return shaderHandle;
	}
	
	public void compileVertexProgram(String souce) {
		vertexProgramHandle = compile(souce, GLES20.GL_VERTEX_SHADER);
	}
	
	public void compileFragmentProgram(String souce) {
		fragmentProgramHandle = compile(souce, GLES20.GL_FRAGMENT_SHADER);
	}
	
	public void link() {
		
		String log = null;
		
		programHandle = GLES20.glCreateProgram();
		
		GLES20.glAttachShader(programHandle, vertexProgramHandle);
		GLES20.glAttachShader(programHandle, fragmentProgramHandle);
		GLES20.glLinkProgram(programHandle);

		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

		if (linkStatus[0] == 0) {
			// The linking failed
			log = GLES20.glGetProgramInfoLog(programHandle);
			GLES20.glDeleteProgram(programHandle);
			programHandle = 0;
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error linking shader program. Vertex program resource: " + vertexShdResName + " Vertex program resource: " + fragmentShdResName + " Log:"  + log);
		}
	}
	
	public int getProgramHandle() {
		return programHandle;
	}
	
	public int getUniformLocation(String name) {
		
		Integer location;
		
		location = uniformLocations.get(name);
		
		if (location == null) {
			// Location not been cashed yet
			location = GLES20.glGetUniformLocation(programHandle, name);
			uniformLocations.put(name, location);
		}
		
		if (location == -1) {
			throw new RuntimeException("Error getting shader uniform location. Vertex program resource: " + vertexShdResName + " Vertex program resource: " + fragmentShdResName + " Name: " + name);
		}
		
		return location;
	}
	
	public int getAttribLocation(String name) {
		Integer location;
		
		location = atribLocations.get(name);
		
		if (location == null) {
			// Location not been cashed yet
			location = GLES20.glGetAttribLocation(programHandle, name);
			atribLocations.put(name, location);
		}
		
		if (location == -1) {
			throw new RuntimeException("Error getting shader attribute location. Vertex program resource: " + vertexShdResName + " Fragment program resource: " + fragmentShdResName + " Name: " + name);
		}
		
		return location;
	}
	
	public void uniform1i(String name, int x) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform1i(location, x);
	}
	
	public void uniform1f(String name, float x) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform1f(location, x);
	}
	
	public void uniform2f(String name, float x, float y) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform2f(location, x, y);
	}
	
	public void uniform2f(String name, Vec2 v) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform2f(location, v.x, v.y);
	}
	
	public void uniform3f(String name, float x, float y, float z) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform3f(location, x, y, z);
	}
	
	public void uniform3f(String name, Vec3 v) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform3f(location, v.x, v.y, v.z);
	}
	
	public void uniformMatrix3fv(String name, int count, boolean transpose, float[] value, int offset) {
		Integer location = getUniformLocation(name);
		GLES20.glUniformMatrix3fv(location, count, transpose, value, offset);
	}
	
	public void uniform4f(String name, Vec4 v) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform4f(location, v.x, v.y, v.z, v.w);
	}
	
	public void uniform4f(String name, float x, float y, float z, float w) {
		Integer location = getUniformLocation(name);
		GLES20.glUniform4f(location, x, y, z, w);
	}
	
	public void uniformMatrix4fv(String name, int count, boolean transpose, float[] value, int offset) {
		Integer location = getUniformLocation(name);
		GLES20.glUniformMatrix4fv(location, count, transpose, value, offset);
	}

	public void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, int offset) {
		Integer location = getAttribLocation(name);
		GLES20.glVertexAttribPointer(location, size, type, normalized, stride, offset);
	}
	
	public void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, Buffer ptr) {
		Integer location = getAttribLocation(name);
		GLES20.glVertexAttribPointer(location, size, type, normalized, stride, ptr);
	}
	
	public void enableVertexAttribArray(String name) {
		GLES20.glEnableVertexAttribArray(getAttribLocation(name));
	}
	
	public void disableVertexAttribArray(String name) {
		GLES20.glDisableVertexAttribArray(getAttribLocation(name));
	}
	
	public void useProgram() {
		GLES20.glUseProgram(programHandle);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		if (renderer.getSurfaceID() != validSurfaceID) {
			mCache.remove(vertexShdResName + "_" + fragmentShdResName + "@" + validSurfaceID);
			validSurfaceID = renderer.getSurfaceID();
		}
		
		reload();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		;
	}
	
	public abstract void enable();
	public abstract void disable();
	
	private String constructID() {
		return vertexShdResName + "_" + fragmentShdResName + "@" + renderer.getSurfaceID();
	}
}
