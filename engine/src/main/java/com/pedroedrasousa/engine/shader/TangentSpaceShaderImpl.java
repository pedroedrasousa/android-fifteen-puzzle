package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;

/**
 * Vertex and texture coordinates, normal, binormal and tangent attributes.
 */
public class TangentSpaceShaderImpl extends AbstractShaderProg implements TangentSpaceShader {

	public static final String COORDS_ATTRIB_NAME		= "aVertPos";
	public static final String NORMAL_ATTRIB_NAME		= "aNormal";
	public static final String TANGENT_ATTRIB_NAME		= "aTangent";
	public static final String BINORMAL_ATTRIB_NAME		= "aBinormal";
	public static final String TEXCOORDS_ATTRIB_NAME	= "aTexCoords";
	
	public TangentSpaceShaderImpl(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
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
	public void setTangentAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(TANGENT_ATTRIB_NAME, size, type, normalized, stride, offset);
	}

	@Override
	public void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, int offset) {
		vertexAttribPointer(BINORMAL_ATTRIB_NAME, size, type, normalized, stride, offset);
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
	public void setTangentAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(TANGENT_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(BINORMAL_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}

	@Override
	public void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr) {
		vertexAttribPointer(TEXCOORDS_ATTRIB_NAME, size, type, normalized, stride, ptr);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(COORDS_ATTRIB_NAME);
	    enableVertexAttribArray(NORMAL_ATTRIB_NAME);
	    enableVertexAttribArray(TANGENT_ATTRIB_NAME);
	    enableVertexAttribArray(BINORMAL_ATTRIB_NAME);
	    enableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(COORDS_ATTRIB_NAME);
	    disableVertexAttribArray(NORMAL_ATTRIB_NAME);
	    disableVertexAttribArray(TANGENT_ATTRIB_NAME);	    
	    disableVertexAttribArray(BINORMAL_ATTRIB_NAME);
	    disableVertexAttribArray(TEXCOORDS_ATTRIB_NAME);
	}
}
