package com.pedroedrasousa.fifteenpuzzle;

import java.util.LinkedList;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.EngineActivity;
import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.Scene;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.gui.GuiElement;
import com.pedroedrasousa.engine.gui.GuiLabel;
import com.pedroedrasousa.engine.gui.GuiTile;
import com.pedroedrasousa.engine.gui.PickableGuiTile;

@SuppressWarnings("rawtypes")
public class MainMenu implements Scene {

	private static final String SCREEN_LABEL = "Main Menu";

	private final EngineActivity	mActivity;
	private FifteenPuzzle	mRenderer;
	private Font			mFont;

	private GuiTile guiTile;
	private PickableGuiTile mBtnClassic;

	private PickableGuiTile mBtnChallenge;
	private PickableGuiTile mBtnAbout;

	private LinkedList<GuiElement> mDialogBoxElements = new LinkedList<GuiElement>();

	public MainMenu(EngineActivity activity, FifteenPuzzle renderer, Font font) {

		Vec4 btnClassicColor	= new Vec4("#D65000");
		Vec4 btnChallengeColor	= new Vec4("#D65000");
		Vec4 btnAboutColor		= new Vec4("#D65000");

		Vec2 btnSize			= new Vec2(0.8f, 0.12f);

		mFont		= font;
		mActivity	= activity;
		mRenderer	= renderer;

		Vec4 bgColor	= new Vec4(mRenderer.getActivity().getString(R.string.gui_bg_color));

		Texture btnClassicTexture	= new Texture(mRenderer, "menu_btn_classic.png");
		Texture btnChallengeTexture = new Texture(mRenderer, "menu_btn_challenges.png");
		Texture btnAboutTexture		= new Texture(mRenderer, "menu_btn_about.png");
		Texture bgTexture			= new Texture(mRenderer, "bg1.jpg");

		GuiTile.OnClickListener onClassicClickLsnr = new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {

				Runnable r = new Runnable() {
					public void run() {
						mRenderer.setScene( new ClassicMenu(mActivity, mRenderer, mFont) );
					}
				};
				mRenderer.queueEvent(r);
			}
		};

		// Background
		GuiTile.Builder guiTileBuilder = new GuiTile.Builder();
		guiTileBuilder.setRenderer(mRenderer);
		guiTileBuilder.setOrthoSize(1.0f, 1.0f);
		guiTileBuilder.setBaseColor(bgColor);
		guiTileBuilder.setPos(0.0f, 0.f);
		guiTileBuilder.setSize(1.0f, 1.0f);
		guiTileBuilder.setTexture(bgTexture);
		guiTile = guiTileBuilder.create();

		// Title
		guiTileBuilder.setPos(0.025f, 0.1f);
		guiTileBuilder.setSize(0.95f, 0.23f);
		guiTileBuilder.setBaseColor(new Vec4(-1.0f));
		guiTileBuilder.setTexture( new Texture(mRenderer, "title.png") );
		guiTileBuilder.setParent(guiTile);
		guiTileBuilder.create();

		PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
		builder.setRenderer(mRenderer);
		builder.setOrthoSize(1.0f, 1.0f);

		// Classic button.
		builder.setPos(0.1f, 0.4f);
		builder.setSize(btnSize);
		builder.setBaseColor(btnClassicColor);
		builder.setTexture(btnClassicTexture);
		builder.setOnClickListener(onClassicClickLsnr);
		builder.setRenderer(mRenderer);
		mBtnClassic = builder.create();
		mBtnClassic.zoomInAnimation();

		GuiTile.OnClickListener onChallengeClickLsnr = new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {

				Runnable r = new Runnable() {
					public void run() {
						mRenderer.setScene( new ChallengeMenu(mActivity, mRenderer, mFont) );
					}
				};
				mRenderer.queueEvent(r);
			}
		};

		// Challenge button
		builder.setPos(0.1f, 0.55f);
		builder.setSize(btnSize);
		builder.setBaseColor(btnChallengeColor);
		builder.setTexture(btnChallengeTexture);
		builder.setOnClickListener(onChallengeClickLsnr);
		mBtnChallenge = builder.create();
		mBtnChallenge.zoomInAnimation();

		// About button
		builder.setPos(0.1f, 0.7f);
		builder.setSize(btnSize);
		builder.setBaseColor(btnAboutColor);
		builder.setTexture(btnAboutTexture);
		builder.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {

				Runnable r = new Runnable() {
					public void run() {
						showAboutDialog();
					}
				};
				mRenderer.queueEvent(r);
			}
		});
		mBtnAbout = builder.create();
		mBtnAbout.zoomInAnimation();
	}

	@Override
	public void render() {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		guiTile.render();
		mBtnClassic.render();
		mBtnChallenge.render();
		mBtnAbout.render();

		for (GuiElement e: mDialogBoxElements) {
			e.render();
		}

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	@Override
	public void onBackPressed() {
		if (mDialogBoxElements.size() == 0) {
			mRenderer.terminate();
		} else {
			mDialogBoxElements.clear();
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		if (mDialogBoxElements == null || mDialogBoxElements.size() == 0) {
			guiTile.onTouch(view, event);
			mBtnClassic.onTouch(view, event);
			mBtnChallenge.onTouch(view, event);
			mBtnAbout.onTouch(view, event);
		} else {
			for (GuiElement e : mDialogBoxElements) {
				e.onTouch(view, event);
			}
		}

		return false;
	}

	@Override
	public void onResume() {
		;
	}

	@Override
	public void onPause() {
		;
	}

	public void showAboutDialog() {

		Vec4 btnColor 			= new Vec4(mRenderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 			= new Vec2(mRenderer.getActivity().getString(R.string.gui_btn_size));
		Vec4 boxColor 			= new Vec4(mRenderer.getActivity().getString(R.string.gui_box_color));
		Vec2 dialogBoxPos		= new Vec2(mRenderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogTextPos		= new Vec2(mRenderer.getActivity().getString(R.string.gui_dialog_text_pos));
		Vec2 textSizeSmall		= new Vec2(mRenderer.getActivity().getString(R.string.gui_text_size_small));

		float dialogAlpha		= Float.parseFloat (mRenderer.getActivity().getString (R.string.dialog_box_alpha));


		String aboutMsg = mRenderer.getActivity().getString(R.string.about_msg);

		final GuiTile dialog;
		final GuiTile bg;


		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(mRenderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(mRenderer);
		builder.setTexture(new Texture(mRenderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		// Dialog box
		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(mRenderer, "box1.png"));
		builder.setPos(dialogBoxPos);
		builder.setSize(0.9f, 0.325f);
		builder.setAlphaFactor(dialogAlpha);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		// Text
		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(mRenderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setNewLineSpaceFactor(1.0f);
		guiLabelBuilder.setScaleFactor(textSizeSmall);
		guiLabelBuilder.setText(aboutMsg);
		guiLabelBuilder.create();

		// Button
		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(mRenderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);
		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(0.5f - btnSize.x * 0.5f, 0.58f);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(mRenderer, "btn_ok.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(mRenderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				mDialogBoxElements.clear();
			}
		});
		pickableGuiTile.create().zoomInAnimation();

		mDialogBoxElements.add(bg);
	}
}
