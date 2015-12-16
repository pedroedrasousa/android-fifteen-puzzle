package com.pedroedrasousa.engine;

import android.opengl.Matrix;

import com.pedroedrasousa.engine.math.Vec3;

/**
 * Represents the camera in 3D space.
 *
 * @author Pedro Edra Sousa
 */
public class Camera {

    /**
     * Specifies the position of the camera eye point.
     */
	private Vec3 eye = new Vec3(0.0f, 0.0f, -10.0f);

    /**
     * Specifies the position of the camera reference point.
     */
	private Vec3 center = new Vec3(0.0f, 0.0f, 0.0f);

    /**
     * Specifies the direction of the camera up vector.
     */
	private Vec3 up = new Vec3(0.0f, 1.0f, 0.0f);

    /**
     * Projection matrix.
     */
	private float[] projMatrix = new float[16];
	
	public Camera() {
		;
	}
	
	public Camera(Vec3 eye, Vec3 center, Vec3 up) {
		this.eye	= eye;
		this.center	= center;
		this.up		= up;
	}
	
	public void set(Vec3 eye, Vec3 center, Vec3 up) {
		this.eye	= eye;
		this.center	= center;
		this.up		= up;
	}
	
	public float[] getViewMatrix() {
		
		float[] viewMatrix	= new float[16];
		
		Matrix.setLookAtM(	viewMatrix, 0,	eye.x,    eye.y,    eye.z,
											center.x, center.y, center.z,
											up.x,     up.y,     up.z);
		
		return viewMatrix;
	}
	
	public void setProjection(int width, int height, float near, float far, float fov) {
		
	    // Create the perspective projection matrix
	    // Width will vary as per aspect ratio
		float screenRatio  = (float) width /  Math.max(height, 1);

	    float top		= (float)Math.tan((float)(fov * (float)Math.PI / 360.0f)) * near;
	    float bottom	= -top;
	    float left		= screenRatio * bottom;
	    float right		= screenRatio * top;

	    Matrix.frustumM(projMatrix, 0, left, right, bottom, top, near, far);
	}
	
	public float[] getProjMAtrix() {
		return projMatrix;
	}
	
	public Vec3 getEye() {
		return eye;
	}
	
	public void setEye(Vec3 eye) {
		this.eye = eye;
	}
	
	public Vec3 getCenter() {
		return center;
	}
	
	public void setCenter(Vec3 center) {
		this.center = center;
	}
	
	public Vec3 getUp() {
		return up;
	}
	
	public void setUp(Vec3 up) {
		this.up = up;
	}
}
