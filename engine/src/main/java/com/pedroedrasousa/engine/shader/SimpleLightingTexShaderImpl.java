package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;

/**
 * Vertex coordinates, texture coordinates and normal attributes.
 */
public class SimpleLightingTexShaderImpl extends AbstractShaderProg implements SimpleLightingTexShader {

	public static final String COORDS_ATTRIB_NAME		= "aVertPos";
	public static final String NORMAL_ATTRIB_NAME		= "aNormal";
	public static final String TEXCOORDS_ATTRIB_NAME	= "aTexCoords";
	
	public SimpleLightingTexShaderImpl(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
		super(renderer, vertexResourceId, fragmentResourceId);
	}

	@Override
	public void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(COORDS_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setNormalAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(NORMAL_ATTRIB_NAME, size, type, normalized, stride, offset);
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
	public void setNormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(NORMAL_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(TEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(COORDS_ATTRIB_NAME);
	    enableVertexAttribArray(NORMAL_ATTRIB_NAME);
	    enableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(COORDS_ATTRIB_NAME);
	    disableVertexAttribArray(NORMAL_ATTRIB_NAME);
	    disableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
}
