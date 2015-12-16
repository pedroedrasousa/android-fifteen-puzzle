package com.pedroedrasousa.engine.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.R;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.shader.TextureShaderImpl;


public class GuiTile extends GuiElement {

	protected Texture			texture;

	protected FloatBuffer		mVertexBuffer;		// Every char will use the same vertex coordinates
	protected FloatBuffer		mTexCoordBuffer;	// Texture coordinates vary per char

	protected TextureShaderImpl	shaderProgram;
	protected TextureShaderImpl	mMaskShaderProg;

	protected float[]			mMVPMatrix = new float[16];
	protected float[]			mMVMatrix  = new float[16];

	protected Vec3 pickedColor = new Vec3();

	protected OnClickListener mOnClickListener;

	protected Object mExtraData;

	protected Vec4 mBaseColor;
	protected float mAlphaFactor;

	protected float mAnimationProgress = 1.0f;

	protected Vec2 texCoordsOffset = new Vec2();

	public Vec2 getTexCoordsOffset() {
		// TODO: Dirty way of clamping tex coords:
		if (texCoordsOffset.x > 1.0f) {
			texCoordsOffset.x -= 1.0f;
		} else if (texCoordsOffset.x < 0.0f) {
			texCoordsOffset.x += 1.0f;
		}

		if (texCoordsOffset.y > 1.0f) {
			texCoordsOffset.y -= 1.0f;
		} else if (texCoordsOffset.y < 0.0f) {
			texCoordsOffset.y += 1.0f;
		}

		return texCoordsOffset;
	}

	public void setTexCoordsOffset(Vec2 posOffset) {
		this.texCoordsOffset = posOffset;
	}

	public void setPosOffset(int x, int y) {
		this.texCoordsOffset.x = x;
		this.texCoordsOffset.y = y;
	}

	@SuppressWarnings("rawtypes")
	protected GuiTile(Builder builder) {
		super(builder);

		mBaseColor			= builder.baseColor;
		mAlphaFactor		= builder.alphaFactor;
		texture				= builder.texture;
		mOnClickListener	= builder.onClickListener;
		mExtraData			= builder.extraData;

		if (size.y == -1) {
			setSize(size.x);
		}

		init();
	}

	protected void init() {

		Matrix.orthoM(mMVPMatrix, 0, 0, orthoWidth, orthoHeight, 0, 0, 100);

		shaderProgram	= new TextureShaderImpl(renderer, R.raw.gui_button_vert, R.raw.gui_button_frag);
        mMaskShaderProg	= new TextureShaderImpl(renderer, R.raw.gui_color_mask_vert, R.raw.gui_color_mask_frag);

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

	public void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}

	public void setBaseColor(Vec4 color) {
		mBaseColor = color;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void zoomInAnimation() {
		mAnimationProgress = 0.0f;
	}

	public void renderSelf() {

		Vec4 color = new Vec4(mBaseColor);

		if (mAnimationProgress < 1.0f) {
			mAnimationProgress += 0.05f * renderer.getFrameFactor();
			color.scale(mAnimationProgress);
		}
		else {
			mAnimationProgress = 1.0f;
		}

		Matrix.setIdentityM(mMVMatrix, 0);
		Matrix.translateM(mMVMatrix, 0,
				getAbsolutePos().x + (getAbsoluteSize().x - (getAbsoluteSize().x * mAnimationProgress)) * 0.5f,
				getAbsolutePos().y + (getAbsoluteSize().y - (getAbsoluteSize().y * mAnimationProgress)) * 0.5f, 0);
		Matrix.scaleM(mMVMatrix, 0, getAbsoluteSize().x * mAnimationProgress, getAbsoluteSize().y * mAnimationProgress, 1.0f);

        shaderProgram.enable();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle());
        shaderProgram.uniform1i("uTexture", 0);

        shaderProgram.uniform4f("uColor", color.x, color.y, color.z, color.w);
        shaderProgram.uniform1f("uAlphaFactor", mAlphaFactor);

        mVertexBuffer.position(0);
        mTexCoordBuffer.position(0);
        shaderProgram.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        shaderProgram.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        shaderProgram.uniformMatrix4fv("uMVMatrix", 1, false, mMVMatrix, 0);
        shaderProgram.uniformMatrix4fv("uMVPMatrix", 1, false, mMVPMatrix, 0);
        shaderProgram.uniform2f("uTexCoordsOffset", texCoordsOffset);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		shaderProgram.disable();
	}

	/**
	 * Image aspect ratio will be kept.
	 */
	public void setSize(float width) {
		size = new Vec2(width, width * (renderer.getViewportWidth() / renderer.getViewportHeight()) / (texture.getOriginalWidth() / texture.getOriginalHeight()));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class Builder<T extends Builder> extends GuiElement.Builder<Builder> {

		protected Texture	texture;
		protected Vec4		baseColor	= new Vec4(1.0f);
		protected float		alphaFactor	= 1.0f;

		public T setSize(float width) {
			this.width	= width;
			height	= -1;
			return (T) this;
		}

		public T setBaseColor(Vec4 baseColor) {
			this.baseColor = baseColor;
			return (T) this;
		}

		public T setBaseColor(float r, float g, float b) {
			this.baseColor = new Vec4(r, g, b, 1.0f);
			return (T) this;
		}

		public T setAlphaFactor(float alphaFactor) {
			this.alphaFactor = alphaFactor;
			return (T) this;
		}

		public T setTexture(Texture texture) {
			this.texture = texture;
			return (T) this;
		}

		public GuiTile create() {
			return new GuiTile(this);
		}
	}

	@Override
	protected void onTouchSelf(View view, MotionEvent event) {
		;
	}
}
