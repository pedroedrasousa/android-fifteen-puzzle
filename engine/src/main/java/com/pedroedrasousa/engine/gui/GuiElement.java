package com.pedroedrasousa.engine.gui;

import java.util.LinkedList;

import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.math.Vec2;

public abstract class GuiElement {

	protected Renderer		renderer;
	protected GuiElement	parent;

	// Orthogonal projection matrix attributes.
	protected float		orthoWidth;
	protected float		orthoHeight;
	protected float		orthoRatio;

	protected Vec2		pos;
	protected Vec2		size;
	protected boolean	isSizeParentRelativeX	= true;
	protected boolean	isSizeParentRelativeY	= true;
	protected boolean	isPosParentRelativeX	= true;
	protected boolean	isPosParentRelativeY	= true;


	protected Object		extraData;

	protected LinkedList<GuiElement> children = new LinkedList<GuiElement>();

	public GuiElement() {
		;
	}

	@SuppressWarnings("rawtypes")
	protected GuiElement(Builder builder) {
		this.setPos(builder.xPos, builder.yPos);
		this.setSize(builder.width, builder.height);
		this.setOrthoSize(builder.orthoWidth, builder.orthoHeight);
		this.setRenderer(builder.renderer);
		this.setIsPosParentRelative(builder.isPosParentRelativeX(), builder.isPosParentRelativeY());
		this.setIsSizeParentRelative(builder.isSizeParentRelativeX(), builder.isSizeParentRelativeY());
		if (builder.parent != null) {
			setParent(builder.parent);
		}
	}

	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	public void setOrthoSize(float width, float height) {
		orthoWidth		= width;
		orthoHeight	= height;
		orthoRatio		= (float)width / (float)height;
	}

	public void setPos(float xPos, float yPos) {
		pos = new Vec2(xPos, yPos);
	}

	public void setSize(float width, float height) {
		size = new Vec2(width, height);
	}

	public void setIsPosParentRelative(boolean x, boolean y) {
		isPosParentRelativeX = x;
		isPosParentRelativeY = y;
	}

	public void setIsSizeParentRelative(boolean x, boolean y) {
		isSizeParentRelativeX = x;
		isSizeParentRelativeY = y;
	}

	public void setParent(GuiElement parent) {
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
	}

	/**
	 * @return Position relative to the whole viewport.
	 */
	public Vec2 getAbsolutePos() {
		if (parent == null)
			return pos;

		Vec2 pos = new Vec2();

		if (this.isPosParentRelativeX) {
			pos.setX(parent.pos.x + this.pos.x * parent.size.x);
		} else {
			pos.setX(this.pos.x);
		}

		if (this.isPosParentRelativeY) {
			pos.setY(parent.pos.y + this.pos.y * parent.size.y);
		} else {
			pos.setY(this.pos.y);
		}

		return pos;
	}

	/**
	 * @return Size relative to the whole viewport.
	 */
	public Vec2 getAbsoluteSize() {
		if (parent == null)
			return size;

		Vec2 size = new Vec2();

		if (this.isSizeParentRelativeX) {
			size.setX(this.size.x * parent.size.x);
		} else {
			size.setX(this.size.x);
		}

		if (this.isSizeParentRelativeY) {
			size.setY(this.size.y * parent.size.y);
		} else {
			size.setY(this.size.y);
		}

		return size;
	}

	public void addChild(GuiElement child) {
		children.add(child);
	}

	protected abstract void onTouchSelf(View view, MotionEvent event);
	protected abstract void renderSelf();

	//@Override
	public void onTouch(View view, MotionEvent event) {
		onTouchSelf(view, event);
		for (GuiElement guiElement : children)
			guiElement.onTouch(view, event);
	}

	public void render() {
		renderSelf();
		for (GuiElement guiElement : children)
			guiElement.render();
	}

	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class Builder<T extends Builder> {

		protected Renderer			renderer;
		protected GuiElement		parent;
		protected float				orthoWidth;
		protected float				orthoHeight;
		protected float				xPos;
		protected float				yPos;
		protected float				width;
		protected float				height;
		protected boolean			isPosParentRelativeX	= true;
		protected boolean			isPosParentRelativeY	= true;
		protected boolean			isSizeParentRelativeX	= true;
		protected boolean			isSizeParentRelativeY	= true;
		protected Object			extraData;
		protected OnClickListener	onClickListener;

		public boolean isPosParentRelativeX() {
			return isPosParentRelativeX;
		}

		public boolean isPosParentRelativeY() {
			return isPosParentRelativeY;
		}

		public boolean isSizeParentRelativeX() {
			return isSizeParentRelativeX;
		}

		public boolean isSizeParentRelativeY() {
			return isSizeParentRelativeY;
		}

		public T setIsPosParentRelative(boolean x, boolean y) {
			isPosParentRelativeX = x;
			isPosParentRelativeY = y;
			return (T) this;
		}

		public T setIsSizeParentRelative(boolean x, boolean y) {
			isSizeParentRelativeX = x;
			isSizeParentRelativeY = y;
			return (T) this;
		}

		public T setIsPosParentRelative(boolean isPosParentRelative) {
			setIsPosParentRelative(isPosParentRelative, isPosParentRelative);
			return (T) this;
		}

		public T setIsSizeParentRelative(boolean isSizeParentRelative) {
			setIsSizeParentRelative(isSizeParentRelative, isSizeParentRelative);
			return (T) this;
		}

		public T setParent(GuiElement parent) {
			this.parent = parent;
			return (T) this;
		}

		public T setPos(float xPos, float yPos) {
			this.xPos = xPos;
			this.yPos = yPos;
			return (T) this;
		}

		public T setPos(Vec2 pos) {
			this.xPos = pos.x;
			this.yPos = pos.y;
			return (T) this;
		}

		public T setSize(float width, float height) {
			this.width	= width;
			this.height	= height;
			return (T) this;
		}

		public T setSize(Vec2 size) {
			this.width	= size.x;
			this.height	= size.y;
			return (T) this;
		}

		public T setRenderer(Renderer renderer) {
			this.renderer = renderer;
			return (T) this;
		}

		public T setOrthoSize(float width, float height) {
			this.orthoWidth		= width;
			this.orthoHeight	= height;
			return (T) this;
		}

		public T setExtraData(Object extraData) {
			this.extraData = extraData;
			return (T) this;
		}

		public T setOnClickListener(OnClickListener onClickListener) {
			this.onClickListener = onClickListener;
			return (T) this;
		}
	}

	public static interface OnClickListener {
		public abstract void onClick(Object extraData);
	}
}
