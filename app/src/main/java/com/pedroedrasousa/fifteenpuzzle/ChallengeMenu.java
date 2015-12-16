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
import com.pedroedrasousa.engine.UniqueColorFactory;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.gui.GuiElement;
import com.pedroedrasousa.engine.gui.GuiLabel;
import com.pedroedrasousa.engine.gui.GuiTile;
import com.pedroedrasousa.engine.gui.PickableGuiTile;


public class ChallengeMenu implements Scene {

	private static final String TAG = ChallengeMenu.class.getSimpleName();

	private EngineActivity	mActivity;
	private FifteenPuzzle	mRenderer;
	private Font			mFont;

	private int				mScreen = 0;


	private Vec2	guiLvlTileSize;
	private Vec4	guiBgColorChallenge;
	private Vec4	guiBtnColor;
	private Vec4	guiTitleColor;
	private Texture	tileTexture;
	private Texture	lockTexture;

	private LinkedList<GuiElement> mGuiElements = new LinkedList<GuiElement>();

	public ChallengeMenu(EngineActivity activity, FifteenPuzzle renderer, Font font) {

		mRenderer	= renderer;
		mFont		= font;
		mActivity	= activity;
		mRenderer	= renderer;

		guiLvlTileSize		= new Vec2(mRenderer.getActivity().getString(R.string.gui_lvl_tile_size));
		guiBgColorChallenge	= new Vec4(mRenderer.getActivity().getString(R.string.gui_bg_color));
		guiBtnColor			= new Vec4(mRenderer.getActivity().getString(R.string.gui_challenge_btn_color));
		guiTitleColor		= new Vec4(mRenderer.getActivity().getString(R.string.gui_title_color));
		tileTexture			= new Texture(mRenderer, "gui_ch_1.png");
		lockTexture			= new Texture(mRenderer, "lock.png");

		// Set the current screen to the latest level available.
		int time16 = Score.getValue(mActivity, FifteenPuzzleScene.GAME_ID_CHALLENGE, String.valueOf(15), "time");
		int time32 = Score.getValue(mActivity, FifteenPuzzleScene.GAME_ID_CHALLENGE, String.valueOf(31), "time");
		if (time16 != -1)
			mScreen = 1;
		if (time32 != -1)
			mScreen = 2;

		createGui();
	}

	@SuppressWarnings("rawtypes")
	private void createGui() {

		UniqueColorFactory.resetColor();

	    Vec2 textSizeSmall		= new Vec2(mRenderer.getActivity().getString(R.string.gui_text_size_small));
		Vec2 labelFontSize		= new Vec2(0.12f, 0.07f);
		Vec2 labelFontSizeSmall	= new Vec2(0.05f, 0.03f);

		// Background
		GuiTile.Builder guiTileBuilder = new GuiTile.Builder();
		guiTileBuilder.setRenderer(mRenderer);
		guiTileBuilder.setOrthoSize(1.0f, 1.0f);
		guiTileBuilder.setBaseColor(guiBgColorChallenge);
		guiTileBuilder.setPos(0.0f, 0.0f);
		guiTileBuilder.setSize(1.0f, 1.0f);
		guiTileBuilder.setTexture(new Texture(mRenderer, "bg1.jpg"));
		mGuiElements.add(guiTileBuilder.create());

		// Title
		guiTileBuilder.setBaseColor(guiTitleColor);
		guiTileBuilder.setPos(0.05f, 0.05f);
		guiTileBuilder.setSize(0.9f, 0.125f);
		guiTileBuilder.setTexture(new Texture(mRenderer, "challenges.png"));
		mGuiElements.add(guiTileBuilder.create());

		// Subtitle
		String footerMsg = mRenderer.getActivity().getString(R.string.msg_challenge_mode_intro);
		GuiLabel.Builder guiSubtitleLabelBuilder = new GuiLabel.Builder();
		guiSubtitleLabelBuilder.setRenderer(mRenderer);
		guiSubtitleLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiSubtitleLabelBuilder.setFont(mFont);
		guiSubtitleLabelBuilder.setScaleFactor(textSizeSmall);
		guiSubtitleLabelBuilder.setPos(0.05f, 0.18f);
		guiSubtitleLabelBuilder.setText(footerMsg);
		mGuiElements.add(guiSubtitleLabelBuilder.create());

		GuiTile.OnClickListener onClickListener2 = new GuiTile.OnClickListener() {
			@Override
			public void onClick(final Object extraData) {


				Runnable r = new Runnable() {
					public void run() {
			    		FifteenPuzzleScene f = new FifteenPuzzleScene(mRenderer);
			    		f.setGameId(FifteenPuzzleScene.GAME_ID_CHALLENGE, Integer.toString((Integer)extraData));
			    		f.restartCurrentGame();
			    		mRenderer.setScene(f);
					}
				};
				mRenderer.queueEvent(r);

			}
		};




		int levelOffset = mScreen * 16;

		for (int i = 0; i < 16; i++) {

			int time			= Score.getValue(mActivity, FifteenPuzzleScene.GAME_ID_CHALLENGE, String.valueOf(i + levelOffset), "time");
			int prevLevelTime	= Score.getValue(mActivity, FifteenPuzzleScene.GAME_ID_CHALLENGE, String.valueOf(i - 1 + levelOffset), "time");

			float xPos = 0.02f + 0.01f * ((float)(i % 4) + 1.0f) + (float)(i % 4) * 0.23f;
			float yPos = 0.29f + (float)(i / 4) * 0.12f;

			if ((time == -1 && prevLevelTime != -1) || (i + levelOffset == 0 && time == -1)) {
			//if (true) {
				// Available but not solved

				PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
				builder.setRenderer(mRenderer);
				builder.setOrthoSize(1.0f, 1.0f);
				builder.setPos(xPos, yPos);
				builder.setSize(guiLvlTileSize);
				builder.setBaseColor(guiBtnColor);
				builder.setExtraData(Integer.valueOf(i + levelOffset));
				builder.setOnClickListener(onClickListener2);
				builder.setTexture(tileTexture);
				GuiTile chTile = builder.create();
				mGuiElements.add(chTile);

				GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
				guiLabelBuilder.setRenderer(mRenderer);
				guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
				guiLabelBuilder.setFont(mFont);
				guiLabelBuilder.setParent(chTile);

				guiLabelBuilder.setScaleFactor(labelFontSize);
				guiLabelBuilder.setPos(0.15f, 0.02f);
				guiLabelBuilder.setText(Integer.toString(i + 1 + levelOffset));
				guiLabelBuilder.create();

			} else if (time == -1 && prevLevelTime == -1) {
				// Locked

				PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
				builder.setRenderer(mRenderer);
				builder.setOrthoSize(1.0f, 1.0f);
				builder.setPos(xPos, yPos);
				builder.setSize(guiLvlTileSize);
				builder.setBaseColor(guiBtnColor);
				builder.setExtraData(Integer.valueOf(i + levelOffset));
				builder.setTexture(tileTexture);
				GuiTile chTile = builder.create();
				mGuiElements.add(chTile);

				builder.setSize(guiLvlTileSize);
				builder.setBaseColor(new Vec4(-1.0f));
				builder.setTexture(lockTexture);
				builder.setAlphaFactor(0.5f);
				mGuiElements.add( builder.create() );

				GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
				guiLabelBuilder.setRenderer(mRenderer);
				guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
				guiLabelBuilder.setFont(mFont);
				guiLabelBuilder.setParent(chTile);

			} else {
				// Solved

				PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
				builder.setRenderer(mRenderer);
				builder.setOrthoSize(1.0f, 1.0f);
				builder.setPos(xPos, yPos);
				builder.setSize(guiLvlTileSize);
				builder.setBaseColor(guiBtnColor);
				builder.setExtraData(Integer.valueOf(i + levelOffset));
				builder.setOnClickListener(onClickListener2);
				builder.setTexture(tileTexture);
				GuiTile chTile = builder.create();
				mGuiElements.add(chTile);

				GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
				guiLabelBuilder.setRenderer(mRenderer);
				guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
				guiLabelBuilder.setFont(mFont);
				guiLabelBuilder.setParent(chTile);

				guiLabelBuilder.setScaleFactor(labelFontSize);
				guiLabelBuilder.setPos(0.15f, 0.02f);
				guiLabelBuilder.setText(Integer.toString(i + 1 + levelOffset));
				guiLabelBuilder.create();

				guiLabelBuilder.setScaleFactor(labelFontSizeSmall);
				guiLabelBuilder.setPos(0.2f, 0.65f);
				guiLabelBuilder.setText(Timer.secToStringMMSS(time));
				guiLabelBuilder.create();
			}
		}

		PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
		builder.setRenderer(mRenderer);
		builder.setOrthoSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(-1.0f));

		// Left arrow
		builder.setPos(0.1f, 0.8f);
		builder.setSize(0.2f, 0.08f);
		builder.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(final Object extraData) {
				Runnable r = new Runnable() {
					public void run() {
						if (mScreen > 0) {
							mScreen--;
				    		mGuiElements.clear();
				    		createGui();
						}
					}
				};
				mRenderer.queueEvent(r);

			}
		});
		builder.setTexture(new Texture(mRenderer, "btn_arrow_left.png"));
		mGuiElements.add(builder.create());


		// Right arrow
		builder.setPos(0.7f, 0.8f);
		builder.setSize(0.2f, 0.08f);
		builder.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(final Object extraData) {
				Runnable r = new Runnable() {
					public void run() {
						if (mScreen < 2) {
							mScreen++;
				    		mGuiElements.clear();
				    		createGui();
						}
					}
				};
				mRenderer.queueEvent(r);

			}
		});
		builder.setTexture(new Texture(mRenderer, "btn_arrow_right.png"));
		mGuiElements.add(builder.create());


		GuiTile.Builder guiTilebuilder = new GuiTile.Builder();
		guiTilebuilder.setRenderer(mRenderer);
		guiTilebuilder.setOrthoSize(1.0f, 1.0f);

		// Left arrow
		guiTilebuilder.setPos(0.4f, 0.82f);
		guiTilebuilder.setSize(0.06f, 0.03f);
		if (mScreen == 0)
			guiTilebuilder.setBaseColor(new Vec4(guiBtnColor));
		else
			guiTilebuilder.setBaseColor(new Vec4(-1.0f));
		guiTilebuilder.setTexture(new Texture(mRenderer, "btn_round.png"));
		mGuiElements.add(guiTilebuilder.create());

		guiTilebuilder.setPos(0.5f - 0.07f/2.0f, 0.82f);
		guiTilebuilder.setSize(0.06f, 0.03f);
		if (mScreen == 1)
			guiTilebuilder.setBaseColor(new Vec4(guiBtnColor));
		else
			guiTilebuilder.setBaseColor(new Vec4(-1.0f));
		guiTilebuilder.setTexture(new Texture(mRenderer, "btn_round.png"));
		mGuiElements.add(guiTilebuilder.create());

		guiTilebuilder.setPos(0.6f - 0.07f, 0.82f);
		guiTilebuilder.setSize(0.06f, 0.03f);
		if (mScreen == 2)
			guiTilebuilder.setBaseColor(new Vec4(guiBtnColor));
		else
			guiTilebuilder.setBaseColor(new Vec4(-1.0f));
		guiTilebuilder.setTexture(new Texture(mRenderer, "btn_round.png"));
		mGuiElements.add(guiTilebuilder.create());
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
