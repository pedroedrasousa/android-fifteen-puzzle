package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

public interface TextureShader extends Shader {
	void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setTexCoordsAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
