package com.pedroedrasousa.fifteenpuzzle;

import android.os.Bundle;

import com.pedroedrasousa.engine.EngineActivity;
import com.pedroedrasousa.engine.EngineApplication;
import com.pedroedrasousa.engine.Renderer;

public class FifteenPuzzleActivity extends EngineActivity {

    private EngineApplication application;

	public FifteenPuzzleActivity() {
		Renderer renderer = new FifteenPuzzle();
		
		super.setRenderer(renderer);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        application = (EngineApplication) getApplication();
        application.setTrakerID(getString(R.string.global_tracker));
        application.setBannerAdUnitId(getString(R.string.interstitial_ad_view_unit_id));
        application.setInterstitialAdUnitId(getString(R.string.ad_view_unit_id_game_screen));
		super.onCreate(savedInstanceState);
	}
}
