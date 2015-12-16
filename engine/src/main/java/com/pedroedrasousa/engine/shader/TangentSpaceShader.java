package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

/**
 * Vertex and texture coordinates, normal, binormal and tangent attributes.
 */
public interface TangentSpaceShader extends Shader, NormalShader, TextureShader {
	void setTangentAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	
	void setTangentAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
	void setBinormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
