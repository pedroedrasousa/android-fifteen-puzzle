package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.SimpleLightingTexShader;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;

import android.opengl.GLES20;

/**
 * Vertex coordinates, texture coordinates and normal attributes.
 */
public class SimpleLightingMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;

	public SimpleLightingMesh(Renderer renderer, VertexData vertexData, SimpleLightingTexShader shaderProgram, boolean useVBOs) {
		super(renderer, vertexData, shaderProgram, useVBOs);
	}
	
	public SimpleLightingMesh(Renderer renderer, VertexData vertexData, SimpleLightingTexShader shaderProgram) {
		super(renderer, vertexData, shaderProgram, true);
	}
	
	public void render() {
		
		int stride = 14 * BYTES_PER_FLOAT;
			
		if (vboData != null) {
			vboData.bindVBO();
			((SimpleLightingTexShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((SimpleLightingTexShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 3*BYTES_PER_FLOAT);
//			((SimpleLightingShader)mShaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, 6*BYTES_PER_FLOAT);
//			((TangentSpaceShader)mShaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 9*BYTES_PER_FLOAT);
			((SimpleLightingTexShader)shaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, 12*BYTES_PER_FLOAT);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			vboData.unbindVBO();
		} else {
			Buffer vertexBuffer	= vertexData.getVertexBuffer();
			Buffer indexBuffer	= vertexData.getIndexBuffer();

			vertexBuffer.position(0);
			((SimpleLightingTexShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(3);
			((SimpleLightingTexShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(6);
//			((TangentSpaceShader)mShaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
//			vertexBuffer.position(9);
//			((TangentSpaceShader)mShaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
//			vertexBuffer.position(12);
			((SimpleLightingTexShader)shaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, vertexBuffer);		
	
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
