package com.pedroedrasousa.engine.math;

import android.opengl.Matrix;


public class Vec4 {
	
	public final static Vec4 ZERO = new Vec4(0.0f, 0.0f, 0.0f, 0.0f);
	public final static Vec4 ONE = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
	
	public float x, y, z, w;
	
	public Vec4() {
		x = y = z = 0.0f;
	}
	
	public Vec4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
		
	public Vec4(float f) {
		this.x = f;
		this.y = f;
		this.z = f;
		this.w = f;
	}
	
	public Vec4(float[] v) {
		assign(v);
	}
	
	public Vec4(Vec4 v) {
		assign(v);
	}
	
	public Vec4(Vec3 a, float w) {
		assign(a.x, a.y, a.z, w);
	}
	
	public Vec4(String str) {
		String s = str.replaceAll("\\s", "");	// Remove whitespaces.
		
		if (s.startsWith("#")) {
			Vec3 vec3Color = Vec3.hex2Vec3(str);
			assign(vec3Color, 1.0f);
		} else {
			String[] tokens = s.split(",");
			x = Float.valueOf(tokens[0]);
			y = Float.valueOf(tokens[1]);
			z = Float.valueOf(tokens[2]);
			if (tokens.length == 3) {
				w = Float.valueOf(tokens[3]);
			} else {
				w = 1.0f;
			}
			
		}
	}
	
	public void assign(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public void assign(float a) {
		this.x = a;
		this.y = a;
		this.z = a;
		this.w = a;
	}
	
	public void assign(float[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
		this.w = v[3];
	}
	
	public void assign(Vec4 a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
		this.w = a.w;
	}
	
	public void assign(Vec3 v, float f) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.w = f;
	}
	
	public void setZero() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		this.w = 0.0f;
	}
	
	public void add(Vec4 a) {
		this.x += a.x;
		this.y += a.y;
		this.z += a.z;
		this.w += a.w;
	}
	
	public void add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
	}
	
	public void add(float a) {
		this.x += a;
		this.y += a;
		this.z += a;
		this.w += a;
	}
	
	public void sub(Vec4 a) {
		this.x -= a.x;
		this.y -= a.y;
		this.z -= a.z;
		this.w -= a.w;
	}
	
	public void sub(float a) {
		this.x -= a;
		this.y -= a;
		this.z -= a;
		this.w -= a;
	}
	
	public void scale(float factor) {
		this.x *= factor;
		this.y *= factor;
		this.z *= factor;
		this.w *= factor;
	}
	
	public void transform(float[] m) {
		float[] rhsVec		= new float[] {x, y, z, w};
		float[] resultVec	= new float[4];
		Matrix.multiplyMV(resultVec, 0, m, 0, rhsVec, 0);
		assign(resultVec);
	}
	
	public void rotate(float[] m) {
		float[] rhsVec		= new float[] {x, y, z, w};
		float[] resultVec	= new float[4];
		Matrix.multiplyMV(resultVec, 0, m, 0, rhsVec, 0);
		assign(resultVec);
	}
	
	@Override
	public String toString() {
	    return x + ", " + y + ", " + z + ", " + w;
	}
	
	@Override
	public boolean equals(Object obj) {
	   if (obj == null) {
	      return false;
	   }

	   if (this.getClass() != obj.getClass()) {
	      return false;
	   }

	   if (this.x != ((Vec4)obj).x || this.y != ((Vec4)obj).y || this.z != ((Vec4)obj).z || this.z != ((Vec4)obj).w) {
	      return false;
	   }

	   return true;
	}
	
	public boolean equals(Object obj, float delta) {
	   if (obj == null) {
	      return false;
	   }

	   if (this.getClass() != obj.getClass()) {
	      return false;
	   }

	   if (	!(x < ((Vec4)obj).x + delta && x > ((Vec4)obj).x - delta) ||
			!(y < ((Vec4)obj).y + delta && y > ((Vec4)obj).y - delta) ||
			!(z < ((Vec4)obj).z + delta && z > ((Vec4)obj).z - delta) ||
			!(w < ((Vec4)obj).w + delta && w > ((Vec4)obj).w - delta) ) {
	      return false;
	   }

	   return true;
	}
	
	public static Vec4 add(Vec4 a, Vec4 b) {
		return new Vec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
	}
	
	public static Vec4 sub(Vec4 a, Vec4 b) {
		return new Vec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
	}
	
	public static Vec4 mul(Vec4 a, float f) {
		return new Vec4(a.x * f, a.y * f, a.z * f, a.w * f);
	}
}
