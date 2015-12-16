package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;

import android.opengl.GLES20;

/**
 * Vertex and texture coordinates, normal, binormal and tangent attributes.
 */
public class TangentSpaceMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;

	public TangentSpaceMesh(Renderer renderer, VertexData vertexData, TangentSpaceShader shaderProgram, boolean useVBOs) {
		super(renderer, vertexData, shaderProgram, useVBOs);
	}
	
	public TangentSpaceMesh(Renderer renderer, VertexData vertexData, TangentSpaceShader shaderProgram) {
		super(renderer, vertexData, shaderProgram, true);
	}
	
	public void render() {
		
		int stride = 14 * BYTES_PER_FLOAT;
			
		if (vboData != null) {
			vboData.bindVBO();
			((TangentSpaceShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			((TangentSpaceShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 3*BYTES_PER_FLOAT);
			((TangentSpaceShader)shaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, 6*BYTES_PER_FLOAT);
			((TangentSpaceShader)shaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, 9*BYTES_PER_FLOAT);
			((TangentSpaceShader)shaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, 12*BYTES_PER_FLOAT);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			vboData.unbindVBO();
		} else {
			Buffer vertexBuffer	= vertexData.getVertexBuffer();
			Buffer indexBuffer	= vertexData.getIndexBuffer();

			vertexBuffer.position(0);
			((TangentSpaceShader)shaderProgram).setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(3);
			((TangentSpaceShader)shaderProgram).setNormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(6);
			((TangentSpaceShader)shaderProgram).setTangentAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(9);
			((TangentSpaceShader)shaderProgram).setBinormalAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			vertexBuffer.position(12);
			((TangentSpaceShader)shaderProgram).setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, stride, vertexBuffer);		
	
			indexBuffer.position(0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
