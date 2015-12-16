package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

public interface SimpleLightingTexShader extends Shader, TextureShader {
	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
