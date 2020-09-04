package com.sharmajikapps.delicatebird;

public interface DummyAdsController {

    public void showBannerAd();
    public void hideBannerAd();
    public boolean isWifiConnected();
    public void showInterstitialAd (Runnable then);

}
