package com.pedroedrasousa.engine.object3d;

import com.pedroedrasousa.engine.Camera;
import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.object3d.mesh.AbstractMesh;
import com.pedroedrasousa.engine.shader.Shader;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Model {
	
	protected AbstractMesh	mesh;
	protected Texture		baseMap;
	protected Texture		normalMap;
	private Camera			camera;
	private Renderer		renderer;
	protected float[]		modelMatrix	= new float[16];

	public Model(Renderer renderer) {
		this.renderer = renderer;
		Matrix.setIdentityM(modelMatrix, 0);
	}
	
	public Model(Renderer renderer, AbstractMesh mesh) {
		this.renderer = renderer;
		Matrix.setIdentityM(modelMatrix, 0);
		setMesh(mesh);
	}
	
	public Model(Renderer renderer, AbstractMesh mesh, Texture baseMap, Texture normalMap) {
		this.renderer = renderer;
		Matrix.setIdentityM(modelMatrix, 0);
		setMesh(mesh);
		setBaseMap(baseMap);
		setNormalMap(normalMap);
	}
		
	public void setMesh(AbstractMesh mesh) {
		this.mesh = mesh;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public void setMatrixAndRender() {
		
		float[]	modelViewMatrix	= new float[16];
		float[]	modelViewProjMatrix	= new float[16];
		
    	Matrix.multiplyMM(modelViewMatrix, 0, renderer.getCamera().getViewMatrix(), 0, modelMatrix, 0);
	    Matrix.multiplyMM(modelViewProjMatrix, 0, renderer.getCamera().getProjMAtrix(), 0, modelViewMatrix, 0);

	    mesh.getShaderProgram().uniformMatrix4fv("uMVMatrix", 1, false, modelViewMatrix, 0);
	    mesh.getShaderProgram().uniformMatrix4fv("uMVPMatrix", 1, false, modelViewProjMatrix, 0);
	    mesh.getShaderProgram().uniformMatrix4fv("uNormalMatrix", 1, false, modelViewMatrix, 0);
	    
	    render();
	}

	public void render() {
		if (baseMap != null) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, baseMap.getHandle());
		}
		if (normalMap != null) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, normalMap.getHandle());
		}
        mesh.render();
	}
	
	public void reloadMesh() {
		mesh.reload();
	}
	
	public AbstractMesh getMesh() {
		return mesh;
	}
	
	public Texture getBaseMap() {
		return baseMap;
	}

	public void setBaseMap(Texture baseMap) {
		this.baseMap = baseMap;
	}

	public Texture getNormalMap() {
		return normalMap;
	}

	public void setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;
	}
	
	public float[] getModelMatrix() {
		return modelMatrix;
	}
	
	public Vec3 getPos() {
		return new Vec3(modelMatrix[12], modelMatrix[13], modelMatrix[14]);
	}
	
	public void setPos(float x, float y, float z) {
		modelMatrix[12] = x;
		modelMatrix[13] = y;
		modelMatrix[14] = z;
	}
	
	public void setPos(Vec3 pos) {
		modelMatrix[12] = pos.x;
		modelMatrix[13] = pos.y;
		modelMatrix[14] = pos.z;
	}
	
	public void translate(float x, float y, float z) {
		Matrix.translateM(modelMatrix, 0, x, y, z);
	}
	
	public void rotate(float a, float x, float y, float z) {
		Matrix.rotateM(modelMatrix, 0, a, x, y, z);		
	}
	
	public Shader getShader() {
		return mesh.getShaderProgram();
	}
}
