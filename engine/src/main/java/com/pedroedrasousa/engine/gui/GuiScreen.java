package com.pedroedrasousa.engine.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.R;
import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.Utils;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.shader.TextureShaderImpl;

public class GuiScreen extends GuiElement {

	protected Texture		mTexture;
	protected Texture		mTexture2;
	protected Texture		mTextureMask;

	private FloatBuffer		mVertexBuffer;		// Every char will use the same vertex coordinates
	private FloatBuffer		mTexCoordBuffer;	// Texture coordinates vary per char

	// Shader and attribute handlers
	private TextureShaderImpl	mShaderProgram;
	private TextureShaderImpl	mMaskShaderProg;



	private float[]			mMVPMatrix = new float[16];
	private float[]			mMVMatrix  = new float[16];

	private boolean			mTouchDown;
	private boolean			mTouchUp;
	private Vec3 pickedColor = new Vec3(Vec3.ONE);

	private OnClickListener mOnClickListener;

	public void setTexture(Texture texture) {
		mTexture = texture;
	}

	public void setTexture2(Texture texture) {
		mTexture2 = texture;
	}

	public void setTextureMask(Texture texture) {
		mTextureMask = texture;
	}

	public GuiScreen(Context context, Renderer renderer, int fontResourceID) {

		mShaderProgram = new TextureShaderImpl(renderer, R.raw.gui_screen_vert, R.raw.gui_screen_frag);
        mMaskShaderProg = new TextureShaderImpl(renderer, R.raw.simple_texture_vert, R.raw.simple_texture_frag);

		float[] vertices = new float[8];

		vertices[0] = 0.0f;	vertices[1] = 1.0f;
		vertices[2] = 1.0f;	vertices[3] = 1.0f;
		vertices[4] = 0.0f;	vertices[5] = 0.0f;
		vertices[6] = 1.0f;	vertices[7] = 0.0f;

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);


		float[] texture = new float[8];




		texture[0] = 0.0f;	texture[1] = 1.0f;
		texture[2] = 1.0f;	texture[3] = 1.0f;
		texture[4] = 0.0f;	texture[5] = 0.0f;
		texture[6] = 1.0f;	texture[7] = 0.0f;

		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mTexCoordBuffer = byteBuf.asFloatBuffer();
		mTexCoordBuffer.put(texture);
		mTexCoordBuffer.position(0);
	}

	public void enable(float w, float h) {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		Matrix.orthoM(mMVPMatrix, 0, 0, w, h, 0, 0, 100);
	}

	public void disable() {
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	public void Print() {

		Matrix.setIdentityM(mMVMatrix, 0);
		Matrix.translateM(mMVMatrix, 0, getAbsolutePos().x, getAbsolutePos().y, 0);
		Matrix.scaleM(mMVMatrix, 0, getAbsoluteSize().x, getAbsoluteSize().y, 1.0f);

        mMaskShaderProg.enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureMask.getHandle());
        GLES20.glEnableVertexAttribArray(mMaskShaderProg.getAttribLocation("aVertPos"));
        mMaskShaderProg.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        mMaskShaderProg.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        mMaskShaderProg.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
        mMaskShaderProg.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mMaskShaderProg.disable();

        if (mTouchDown) {
        	mTouchDown = false;
        	pickedColor = Utils.getPixelColor(mTouchedCoordsX, renderer.getViewportHeight() - mTouchedCoordsY);
        }

        if (mTouchUp) {
        	mTouchUp = false;
        	pickedColor = Utils.getPixelColor(mTouchedCoordsX, renderer.getViewportHeight() - mTouchedCoordsY);
        	if (mOnClickListener != null)
        		mOnClickListener.onClick(pickedColor);
        	pickedColor.assign(1.0f, 1.0f, 1.0f);
        }

        mShaderProgram.enable();

        mShaderProgram.uniform3f("pickedColor", pickedColor.x, pickedColor.y, pickedColor.z);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.getHandle());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture2.getHandle());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureMask.getHandle());
        mShaderProgram.uniform1i("uTexture", 0);
        mShaderProgram.uniform1i("uTexture2", 1);
        mShaderProgram.uniform1i("uTextureMask", 2);

        mVertexBuffer.position(0);
        mTexCoordBuffer.position(0);
        mShaderProgram.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        mShaderProgram.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        mShaderProgram.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
        mShaderProgram.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		mShaderProgram.disable();
	}

	public void setSize(float width) {
		size = new Vec2(width, width * (renderer.getViewportWidth() / renderer.getViewportHeight()) / (mTexture.getOriginalWidth() / mTexture.getOriginalHeight()));
	}

	private int mTouchedCoordsX;
	private int mTouchedCoordsY;

	public void onTouchSelf(int x, int y) {
		mTouchDown = true;
		mTouchedCoordsX = x;
		mTouchedCoordsY = y;
	}

	public void onTouchUpSelf(int x, int y) {
		mTouchUp = true;
		mTouchedCoordsX = x;
		mTouchedCoordsY = y;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}

	public static interface OnClickListener {
		public abstract void onClick(Vec3 color);
	}

	@Override
	public void renderSelf() {
		enable(1.0f, 1.0f);
		Print();
		disable();
	}

	@Override
	protected void onTouchSelf(View view, MotionEvent event) {
		;
	}
}
