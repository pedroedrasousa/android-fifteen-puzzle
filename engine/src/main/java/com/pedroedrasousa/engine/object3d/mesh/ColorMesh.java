package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import android.opengl.GLES20;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.ColorShader;


public class ColorMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;
	
	private Vec3 color;
	
	public ColorMesh(Renderer renderer, VertexData vertexData, ColorShader shaderProgram, boolean useVBOs, Vec3 color) {
		super(renderer, vertexData, shaderProgram, useVBOs);
		setColor(color);
	}

	public ColorMesh(Renderer renderer, VertexData vertexData, ColorShader shaderProgram, Vec3 color) {
		this(renderer, vertexData, shaderProgram, true, color);
	}

	public Vec3 getColor() {
		return color;
	}

	public void setColor(Vec3 color) {
		this.color = new Vec3(color);
	}

	public void render() {
		
		int stride = 3 * BYTES_PER_FLOAT;
		
		if (vboData != null) {
			vboData.bindVBO();
			((ColorShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((ColorShader)shaderProgram).setColor(color);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			vboData.unbindVBO();
		} else {
			Buffer vertexBuffer	= vertexData.getVertexBuffer();
			Buffer indexBuffer	= vertexData.getIndexBuffer();
			vertexBuffer.position(0);
			((ColorShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			((ColorShader)shaderProgram).setColor(color);
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
