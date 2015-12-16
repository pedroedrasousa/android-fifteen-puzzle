package com.pedroedrasousa.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.pedroedrasousa.engine.shader.AbstractShaderProg;

import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.app.Activity;
import android.os.Bundle;

/**
 * The Android activity used by the engine.
 *
 * @author Pedro Edra Sousa
 */
public class EngineActivity extends Activity {

    /**
     * Log writer.
     */
	private static final Logger logger = LoggerFactory.getLogger(AbstractShaderProg.class.getSimpleName());

    /**
     * The engine application.
     */
	private EngineApplication application;

	/**
	 * Engine OpenGL Surface view.
	 */
	private EngineGLSurfaceView	glSurfaceView;

    /**
     * Engine renderer.
     */
	private Renderer			renderer;

	private boolean				isAdBannerVisible;

    /**
     * AdMob interstitial ad.
     */
    private InterstitialAd interstitialAd;

    /**
     * AdMob ad view.
     */
    private AdView adView;

    /**
     * AdMob ad request builder.
     */
    private AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

    //<editor-fold desc="Getters and setters">
    /**
     * Gets the OpenGL surface view.
     *
     * @return The OpenGL surface view.
     */
    public EngineGLSurfaceView getGLSurfaceView() {
        return glSurfaceView;
    }

    /**
     * Gets engine renderer.
     *
     * @return The engine renderer.
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Sets the engine renderer.
     *
     * @param renderer The engine renderer.
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
    //</editor-fold>

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.game_activity);

        application = (EngineApplication) getApplication();

		glSurfaceView = (EngineGLSurfaceView)findViewById(R.id.gl_surface_view);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        renderer.setEngineGLSurfaceView(glSurfaceView);
        renderer.setActivity(this);

        // Request an OpenGL ES 2.0 compatible context and set the renderer
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer((Renderer) renderer);
        glSurfaceView.setOnTouchListener(renderer);

        // Initialize ads
        adRequestBuilder.addTestDevice("76F85C3EE9D49323AA56DC33A45089EA");
        initializeAdBanner();
        initAdInterstitial();
	}

    //<editor-fold desc="AdMob stuff">
    /**
     * Initializes AdMob banner.
     */
    private void initializeAdBanner() {
        AdView adView = new AdView(getBaseContext());
        adView.setAdUnitId(application.getBannerAdUnitId());
        adView.setAdSize(AdSize.BANNER);

        LinearLayout adLayout = (LinearLayout) this.findViewById(R.id.ad_layout);
        adLayout.addView(adView);

        AdRequest adRequest = adRequestBuilder.build();
        adView.loadAd(adRequest);
    }

    /**
     * Initializes AdMob interstitial.
     */
    private void initAdInterstitial() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(application.getInterstitialAdUnitId());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
    }

    /**
     * Requests a new AdMob interstitial.
     */
    private void requestNewInterstitial() {
        AdRequest adRequest = adRequestBuilder.build();
        interstitialAd.loadAd(adRequest);
    }

    /**
     * Show an AdMob interstitial ad if loaded.
     */
    public void tryShowInterstitial() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }
    //</editor-fold>

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {

		super.onResume();

		if (glSurfaceView != null) {
			glSurfaceView.onResume();
		}

		if (renderer!= null) {
			renderer.onResume();
		}
	}

	@Override
	protected void onPause() {

		if (glSurfaceView != null) {
			glSurfaceView.onPause();
		}

		if (renderer!= null) {
			renderer.onPause();
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		glSurfaceView.queueEvent(new Runnable() {
            public void run() {
                renderer.onBackPressed();
            }
        });
	}




	public void terminateWithInterstitial() {
		super.onBackPressed();
	}


}