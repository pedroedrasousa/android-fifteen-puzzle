package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;


public class SimpleShaderProg extends AbstractShaderProg implements Shader {
	
	public static final String COORDS_ATTRIB_NAME = "aVertPos";
	
	public SimpleShaderProg(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
		super(renderer, vertexResourceId, fragmentResourceId);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(COORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(COORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(COORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(COORDS_ATTRIB_NAME);
	}
}
