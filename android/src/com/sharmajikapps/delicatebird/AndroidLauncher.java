package com.sharmajikapps.delicatebird;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.sharmajikapps.delicatebird.GameMain;

import hud.MainMenuButtons;

public class AndroidLauncher extends AndroidApplication implements AdsController   {

	private static final String BANNER_AD_UNIT_ID = "ca-app-pub-7282200021394338/2020706952";
    private static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-7282200021394338/2975743125";


    AdView bannerAd;
    InterstitialAd interstitialAd;

	public void setupAds() {
		bannerAd = new AdView(this);
		bannerAd.setVisibility(View.INVISIBLE);
		bannerAd.setBackgroundColor(0xff000000); // black
		bannerAd.setAdUnitId(BANNER_AD_UNIT_ID);
		bannerAd.setAdSize(AdSize.SMART_BANNER);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(INTERSTITIAL_UNIT_ID);

        AdRequest.Builder builder = new AdRequest.Builder();
        AdRequest ad = builder.build();
        interstitialAd.loadAd(ad);
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		View gameView = initializeForView(new GameMain(this), config);
		setupAds();
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(gameView, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layout.addView(bannerAd, params);

		setContentView(layout);

		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
			}
		});
	}

	@Override
	public void showBannerAd() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bannerAd.setVisibility(View.VISIBLE);
				AdRequest.Builder builder = new AdRequest.Builder();
				AdRequest ad = builder.build();
				bannerAd.loadAd(ad);
			}
		});
	}

	@Override
	public void hideBannerAd() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bannerAd.setVisibility(View.INVISIBLE);
			}
		});

	}

    @Override
    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return (ni != null && ni.isConnectedOrConnecting());
    }

    @Override
    public void showInterstitialAd(final Runnable then) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (then != null) {
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            Gdx.app.postRunnable(then);
                            AdRequest.Builder builder = new AdRequest.Builder();
                            AdRequest ad = builder.build();
                            interstitialAd.loadAd(ad);


                        }
                    });
                }
                interstitialAd.show();
            }
        });
    }
}
