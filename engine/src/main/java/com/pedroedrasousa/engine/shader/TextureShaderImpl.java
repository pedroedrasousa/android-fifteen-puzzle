package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;


public class TextureShaderImpl extends AbstractShaderProg implements TextureShader {

	public static final String COORDS_ATTRIB_NAME		= "aVertPos";
	public static final String TEXCOORDS_ATTRIB_NAME	= "aTexCoords";
	
	public TextureShaderImpl(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
		super(renderer, vertexResourceId, fragmentResourceId);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(COORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(TEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(COORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(TEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(COORDS_ATTRIB_NAME);
	    enableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(COORDS_ATTRIB_NAME);
	    disableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
}
