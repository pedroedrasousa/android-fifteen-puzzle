package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.ColorShader;
import com.pedroedrasousa.engine.shader.NormalColorShader;
import com.pedroedrasousa.engine.shader.NormalShader;

import android.opengl.GLES20;

/**
 * Vertex coordinates, color, and normal attributes.
 */
public class SimpleLightingColorMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;
	
	private Vec3 color;

	public SimpleLightingColorMesh(Renderer renderer, VertexData vertexData, NormalColorShader shaderProgram, boolean useVBOs, Vec3 color) {
		super(renderer, vertexData, shaderProgram, useVBOs);
		this.color = color;
	}
	
	public SimpleLightingColorMesh(Renderer renderer, VertexData vertexData, NormalColorShader shaderProgram, Vec3 color) {
		super(renderer, vertexData, shaderProgram, true);
		this.color = color;
	}
	
	public void render() {
		
		int stride = 14 * BYTES_PER_FLOAT;
			
		if (vboData != null) {
			vboData.bindVBO();
			((NormalShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((NormalShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 3*BYTES_PER_FLOAT);
			((ColorShader)shaderProgram).setColor(color);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			vboData.unbindVBO();
		} else {
			Buffer vertexBuffer	= vertexData.getVertexBuffer();
			Buffer indexBuffer	= vertexData.getIndexBuffer();

			vertexBuffer.position(0);
			((NormalShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(3);
			((NormalShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			((ColorShader)shaderProgram).setColor(color);
			
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
