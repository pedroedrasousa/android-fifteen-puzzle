package com.pedroedrasousa.engine.gui;

import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec4;


public class GuiLabel extends GuiElement {
	
	private Font	font;
	private Vec4	color	= new Vec4(1.0f);
	private String	text	= new String("<undefined>");
	
	private Vec2	scaleFactor	= new Vec2(1.0f);
	
	private float	newLineSpaceFactor = 0.8f;
	
	private GuiLabel(Builder builder) {
		super(builder);
		
		if (builder.text != null)
			setText(builder.text);
		
		if (builder.color != null)
			this.color = builder.color;
		
		setFont(builder.font);
		setScaleFactor(builder.scaleFactorX, builder.scaleFactorY);
		setNewLineSpaceFactor(builder.newLineSpaceFactor);
	}
	
	public void setScaleFactor(float x, float y) {
		this.scaleFactor = new Vec2(x, y);
	}
	
	public void setFont(Font font) {
		this.font = font;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setNewLineSpaceFactor(float newLineSpaceFactor) {
		this.newLineSpaceFactor = newLineSpaceFactor;
	}
	
	@Override
	public void renderSelf() {
		font.enable(1.0f, 1.0f);
		font.setScaleFactor(scaleFactor.x, scaleFactor.y);
		font.setNewLineSpaceFactor(newLineSpaceFactor);
		font.print(getAbsolutePos().x, getAbsolutePos().y, text, color);
		font.setScaleFactor(1.0f);
		font.resetNewLineSpaceFactor();
		font.disable();
	}
	
	public static class Builder extends GuiElement.Builder<Builder> {

		private Font	font;
		private Vec4	color;
		private String	text;
		private float	newLineSpaceFactor	= 0.8f;
		
		private float	scaleFactorX;
		private float	scaleFactorY;
		
		public Builder setSize(float width) {
			this.width	= width;
			this.height	= -1;
			return (Builder) this;
		}
		
		public Builder setColor(Vec4 color) {
			this.color = color;
			return this;
		}
		
		public Builder setExtraData(Object extraData) {
			this.extraData = extraData;
			return this;
		}
		
		public Builder setFont(Font font) {
			this.font = font;
			return this;
		}
		
		public Builder setScaleFactor(float x, float y) {
			this.scaleFactorX = x;
			this.scaleFactorY = y;
			return this;
		}
		
		public Builder setScaleFactor(Vec2 factor) {
			this.scaleFactorX = factor.x;
			this.scaleFactorY = factor.y;
			return this;
		}
		
		public Builder setScaleFactor(float x) {
			this.scaleFactorX = x;
			this.scaleFactorY = -1.0f;
			return this;
		}
		
		public Builder setText(String text) {
			this.text = text;
			return this;
		}
		
		public Builder appendText(String text) {
			this.text += text;
			return this;
		}
		
		public Builder setNewLineSpaceFactor(float newLineSpaceFactor) {
			this.newLineSpaceFactor = newLineSpaceFactor;
			return this;
		}
		
		public GuiLabel create() {
			return new GuiLabel(this);
		}
	}

	@Override
	protected void onTouchSelf(View view, MotionEvent event) {
		;
	}
}
