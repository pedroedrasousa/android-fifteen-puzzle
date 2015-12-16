package com.pedroedrasousa.engine.shader;

import java.nio.Buffer;

import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;

public interface Shader {
	void enable();
	void disable();
	
	int getProgramHandle();
	int getUniformLocation(String name);
	int getAttribLocation(String name);
	void uniform1i(String name, int x);
	void uniform1f(String name, float x);
	void uniform2f(String name, float x, float y);
	void uniform2f(String name, Vec2 v);
	void uniform3f(String name, float x, float y, float z);
	void uniform3f(String name, Vec3 v);
	void uniformMatrix3fv(String name, int count, boolean transpose, float[] value, int offset);
	void uniform4f(String name, Vec4 v);
	void uniform4f(String name, float x, float y, float z, float w);
	void uniformMatrix4fv(String name, int count, boolean transpose, float[] value, int offset);
	void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, int offset);
	void vertexAttribPointer(String name, int size, int type, boolean normalized, int stride, Buffer ptr);
	void enableVertexAttribArray(String name);
	void disableVertexAttribArray(String name);
	void useProgram();
	
	void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, int offset);
	void setVertexPosAttribPointer(int size, int type, boolean normalized, int stride, Buffer ptr);
}
