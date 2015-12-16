package com.pedroedrasousa.fifteenpuzzle;

import java.util.LinkedList;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

import com.pedroedrasousa.engine.EngineActivity;
import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.Scene;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.Timer;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.gui.GuiElement;
import com.pedroedrasousa.engine.gui.GuiLabel;
import com.pedroedrasousa.engine.gui.GuiTile;
import com.pedroedrasousa.engine.gui.PickableGuiTile;


public class ClassicMenu implements Scene {

	private EngineActivity		mActivity;
	private FifteenPuzzle	mRenderer;
	private Font			mFont;
	
	private LinkedList<GuiElement> mGuiElements = new LinkedList<GuiElement>();
	
	@SuppressWarnings("rawtypes")
	public ClassicMenu(EngineActivity activity, FifteenPuzzle renderer, Font font) {
				
		int time;
		int moves;
		
		mFont		= font;
		mActivity	= activity;
		mRenderer	= renderer;
		
		Vec4 btnColor 		= new Vec4(mRenderer.getActivity().getString(R.string.gui_classic_btn_color));
		Vec4 bgColor 		= new Vec4(mRenderer.getActivity().getString(R.string.gui_bg_color));
		Vec4 guiTitleColor	= new Vec4(mRenderer.getActivity().getString(R.string.gui_title_color));
		Vec2 textSizeSmall	= new Vec2(mRenderer.getActivity().getString(R.string.gui_text_size_small));
		
		Texture tileTexture	= new Texture(mRenderer, "tile2.png");
		
		// Background
		GuiTile.Builder guiTileBuilder = new GuiTile.Builder();
		guiTileBuilder.setRenderer(mRenderer);
		guiTileBuilder.setOrthoSize(1.0f, 1.0f);
		guiTileBuilder.setBaseColor(bgColor);
		guiTileBuilder.setPos(0.0f, 0.0f);
		guiTileBuilder.setSize(1.0f, 1.0f);
		guiTileBuilder.setTexture(new Texture(mRenderer, "bg1.jpg"));
		mGuiElements.add(guiTileBuilder.create());
		
		// Title
		guiTileBuilder.setBaseColor(guiTitleColor);
		guiTileBuilder.setPos(0.05f, 0.05f);
		guiTileBuilder.setSize(0.9f, 0.125f);
		guiTileBuilder.setTexture(new Texture(mRenderer, "classic.png"));
		mGuiElements.add(guiTileBuilder.create());
		
		// Subtitle
		String footerMsg = mRenderer.getActivity().getString(R.string.msg_classic_mode_intro);
		GuiLabel.Builder guiSubtitleLabelBuilder = new GuiLabel.Builder();
		guiSubtitleLabelBuilder = new GuiLabel.Builder();
		guiSubtitleLabelBuilder.setRenderer(mRenderer);
		guiSubtitleLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiSubtitleLabelBuilder.setFont(mFont);
		guiSubtitleLabelBuilder.setScaleFactor(textSizeSmall);
		guiSubtitleLabelBuilder.setPos(0.1f, 0.18f);
		guiSubtitleLabelBuilder.setText(footerMsg);
		mGuiElements.add(guiSubtitleLabelBuilder.create());
		
		GuiTile.OnClickListener onClickListener = new GuiTile.OnClickListener() {
			@Override
			public void onClick(final Object extraData) {

				Runnable r = new Runnable() {
					public void run() {
			    		FifteenPuzzleScene f = new FifteenPuzzleScene(mRenderer);
			    		f.setGameId("classic", (String)extraData);
			    		f.restartCurrentGame();
			    		mRenderer.setScene(f);
					}
				};
				mRenderer.queueEvent(r);
			}
		};
		
		
		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(mRenderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);		
		guiLabelBuilder.setScaleFactor(0.13f, 0.08f);
		guiLabelBuilder.setPos(0.15f, 0.0f);

		GuiLabel.Builder smallLabelBuilder = new GuiLabel.Builder();
		smallLabelBuilder.setRenderer(mRenderer);
		smallLabelBuilder.setOrthoSize(1.0f, 1.0f);
		smallLabelBuilder.setFont(mFont);
		smallLabelBuilder.setPos(0.15f, 0.6f);
		smallLabelBuilder.setScaleFactor(textSizeSmall);
		
		
		PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
		builder.setRenderer(mRenderer);
		builder.setOrthoSize(1.0f, 1.0f);
		builder.setBaseColor(btnColor);
		builder.setOnClickListener(onClickListener);
		
		builder.setTexture(tileTexture);
		builder.setSize(0.8f, 0.12f);

		
		// Button 3x3
		time	= Score.getValue(mActivity, "classic", "3x3", "time");
		moves	= Score.getValue(mActivity, "classic", "3x3", "moves");
		
		builder.setPos(0.1f, 0.28f);
		builder.setExtraData("3x3");
		GuiTile chTile = builder.create();
		
		guiLabelBuilder.setParent(chTile);
		guiLabelBuilder.setText("3x3");
		guiLabelBuilder.create();
		
		smallLabelBuilder.setParent(chTile);
		if (time != -1)
			smallLabelBuilder.setText("Best: " + Timer.secToStringMMSS(time) + " in " + moves + " moves");
		else
			smallLabelBuilder.setText("No Record");
		smallLabelBuilder.create();
		
		mGuiElements.add(chTile);
		
		
		// Button 3x4
		time	= Score.getValue(mActivity, "classic", "3x4", "time");
		moves	= Score.getValue(mActivity, "classic", "3x4", "moves");
			
		builder.setPos(0.1f, 0.43f);
		builder.setExtraData("3x4");
		GuiTile chTile2 = builder.create();
		
		guiLabelBuilder.setParent(chTile2);
		guiLabelBuilder.setText("3x4");
		guiLabelBuilder.create();
		
		smallLabelBuilder.setParent(chTile2);
		if (time != -1)
			smallLabelBuilder.setText("Best: " + Timer.secToStringMMSS(time) + " in " + moves + " moves");
		else
			smallLabelBuilder.setText("No Record");
		smallLabelBuilder.create();
		
		mGuiElements.add(chTile2);
		
		// Button 4x4
		time	= Score.getValue(mActivity, "classic", "4x4", "time");
		moves	= Score.getValue(mActivity, "classic", "4x4", "moves");
		
		builder.setPos(0.1f, 0.58f);
		builder.setExtraData("4x4");
		GuiTile chTile3 = builder.create();
		
		guiLabelBuilder.setParent(chTile3);
		guiLabelBuilder.setText("4x4");
		guiLabelBuilder.create();
		
		smallLabelBuilder.setParent(chTile3);
		if (time != -1)
			smallLabelBuilder.setText("Best: " + Timer.secToStringMMSS(time) + " in " + moves + " moves");
		else
			smallLabelBuilder.setText("No Record");
		smallLabelBuilder.create();
		
		mGuiElements.add(chTile3);
	}
	
	@Override
	public void render() {
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		for (GuiElement e : mGuiElements) {
			e.render();
		}
		
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	@Override
	public void onBackPressed() {
		Runnable r = new Runnable() {
			public void run() {
				mRenderer.setScene( new MainMenu(mActivity, mRenderer, mFont) );
			}
		};
		mRenderer.queueEvent(r);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		for (GuiElement e: mGuiElements) {
			e.onTouch(view, event);
		}
		return true;
	}

	@Override
	public void onResume() {
		;
	}

	@Override
	public void onPause() {
		;
	}
}
