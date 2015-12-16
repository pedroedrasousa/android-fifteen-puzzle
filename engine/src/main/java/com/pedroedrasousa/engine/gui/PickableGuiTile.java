package com.pedroedrasousa.engine.gui;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.FrameBuffer;
import com.pedroedrasousa.engine.UniqueColorFactory;
import com.pedroedrasousa.engine.Utils;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;

public class PickableGuiTile extends GuiTile {

	private static final float COLOR_ERROR_DELTA	= 0.05f;	// Allowed error delta when comparing color values.

	private Vec3 mMaskColor;

	private boolean mTouchDown;
	private boolean mTouchUp;
	private int mTouchedCoordsX;
	private int mTouchedCoordsY;

	@SuppressWarnings("rawtypes")
	private PickableGuiTile(Builder builder) {
		super(builder);
		init();
	}

	protected void init() {
		super.init();
		mMaskColor = UniqueColorFactory.buildUniqueColor();
	}

	public void renderSelf() {

		FrameBuffer auxFrameBuffer = renderer.requestAuxFrameBuffer();

		Vec4 color = new Vec4(mBaseColor);

		if (mAnimationProgress < 1.0f) {
			mAnimationProgress += 0.07f * renderer.getFrameFactor();
			mAlphaFactor = mAnimationProgress;
		}
		else {
			mAlphaFactor = 1.0f;
		}

		Matrix.setIdentityM(mMVMatrix, 0);
		Matrix.translateM(mMVMatrix, 0, getAbsolutePos().x + (getAbsoluteSize().x - (getAbsoluteSize().x * mAnimationProgress)) * 0.5f, getAbsolutePos().y, 0);
		Matrix.scaleM(mMVMatrix, 0, getAbsoluteSize().x * mAnimationProgress, getAbsoluteSize().y * mAnimationProgress, 1.0f);

		auxFrameBuffer.bind();

        mMaskShaderProg.enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle());
        mMaskShaderProg.uniform1i("uTexture", 0);
        mMaskShaderProg.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        mMaskShaderProg.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        mMaskShaderProg.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
        mMaskShaderProg.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);
        mMaskShaderProg.uniform3f("uColor", mMaskColor.x, mMaskColor.y, mMaskColor.z);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mMaskShaderProg.disable();

        if (mTouchDown) {
        	mTouchDown = false;
        	pickedColor = Utils.getPixelColor(mTouchedCoordsX, renderer.getViewportHeight() - mTouchedCoordsY);
        }

        if (mTouchUp) {
        	mTouchUp = false;
        	pickedColor = Utils.getPixelColor(mTouchedCoordsX, renderer.getViewportHeight() - mTouchedCoordsY);
        	if (pickedColor.equals(mMaskColor, COLOR_ERROR_DELTA))
	        	if (mOnClickListener != null) {
	        		mOnClickListener.onClick(mExtraData);
	        	}
        	pickedColor.assign(1.0f, 1.0f, 1.0f);
        }

        auxFrameBuffer.unbind();

        shaderProgram.enable();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle());
        shaderProgram.uniform1i("uTexture", 0);

        if (pickedColor.equals(mMaskColor, UniqueColorFactory.COLOR_ERROR_DELTA)) {
        	// Highlight
        	if (color.x == -1.0f) {
        		color.assign(0.25f, 0.25f, 0.25f, 0.25f);
        	}
        	shaderProgram.uniform4f("uColor", color.x + 0.5f, color.y + 0.5f, color.z + 0.5f, color.w);
        } else {
        	shaderProgram.uniform4f("uColor", color.x, color.y, color.z, color.w);
        }

        shaderProgram.uniform1f("uAlphaFactor", mAlphaFactor - 0.2f);

        mVertexBuffer.position(0);
        mTexCoordBuffer.position(0);
        shaderProgram.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        shaderProgram.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        shaderProgram.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
        shaderProgram.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		shaderProgram.disable();
	}

	@SuppressWarnings("rawtypes")
	public static class Builder<T extends Builder> extends GuiTile.Builder<Builder> {
		public PickableGuiTile create() {
			return new PickableGuiTile(this);
		}
	}

	protected void onTouchSelf(View view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			mTouchUp = true;
		} else {
			mTouchDown = true;
		}

		mTouchedCoordsX = (int)event.getX();;
		mTouchedCoordsY = (int)event.getY();
	}
}
