package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

public interface NormalColorShader extends Shader, NormalShader, ColorShader {
	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setNormalAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
