package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.math.Vec3;


public class ColorShaderImpl extends AbstractShaderProg implements ColorShader {
	
	public static final String COORDS_ATTRIB_NAME	= "aVertPos";
	public static final String COLOR_UNIFORM_NAME	= "uColor";
	
	public ColorShaderImpl(Renderer renderer, int vertexResourceId, int fragmentResourceId) {
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
	
	@Override
	public void setColor(Vec3 color) {
		uniform3f(COLOR_UNIFORM_NAME, color.x, color.y, color.z);
	}
	
	public void enable() {
		super.useProgram();
		enableVertexAttribArray(COORDS_ATTRIB_NAME);
	}
	
	public void disable() {
		disableVertexAttribArray(COORDS_ATTRIB_NAME);
	}
}
