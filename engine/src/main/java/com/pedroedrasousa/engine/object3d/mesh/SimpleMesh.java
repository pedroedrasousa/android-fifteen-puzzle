package com.pedroedrasousa.engine.object3d.mesh;

import java.nio.Buffer;

import android.opengl.GLES20;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.Shader;

/**
 * Only vertex coordinates attributes.
 */
public class SimpleMesh extends AbstractMesh {
	
	public static final int BYTES_PER_FLOAT = 4;
	
	public SimpleMesh(Renderer renderer, VertexData vertexData, Shader shaderProgram, boolean useVBOs) {
		super(renderer, vertexData, shaderProgram, useVBOs);
	}
	
	public SimpleMesh(Renderer renderer, VertexData vertexData, Shader shaderProgram) {
		super(renderer, vertexData, shaderProgram);
	}
	
	public void render() {
		
		int stride = 3 * BYTES_PER_FLOAT;
		
		if (vboData != null) {
			vboData.bindVBO();
			shaderProgram.setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, 0);
			GLES20.glDrawElements(renderMode, vertexData.getNbrIndices(), GLES20.GL_UNSIGNED_SHORT, 0);
			vboData.unbindVBO();
		} else {
			Buffer vertexBuffer	= vertexData.getVertexBuffer();
			Buffer indexBuffer	= vertexData.getIndexBuffer();
			vertexBuffer.position(0);
			shaderProgram.setVertexPosAttribPointer(3, GLES20.GL_FLOAT, false, stride, vertexBuffer);
			indexBuffer.position(0);
			GLES20.glDrawElements(renderMode, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
	}
}
