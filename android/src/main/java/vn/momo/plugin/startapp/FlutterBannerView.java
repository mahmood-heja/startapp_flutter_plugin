package vn.momo.plugin.startapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

/**
 * @author dungvu
 * @since 2019-06-04
 */
public class FlutterBannerView implements PlatformView, MethodChannel.MethodCallHandler {
    public static final String IS_BLOCKED_KEY = "is_blocked", LAST_TIME_CLICKED_KEY = "last_time_clicked";

    private final FrameLayout bannerContainer;
    private final Context context;
    private final MethodChannel methodChannel;
    static private int numberOfClicks;

    FlutterBannerView(Context context, BinaryMessenger messenger, int id) {
        this.context = context;
        bannerContainer = new FrameLayout(context);
        numberOfClicks = lastClickIn24h();
        methodChannel = new MethodChannel(messenger, StartAppBannerPlugin.PLUGIN_KEY + "_" + id);
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {

        if ("loadAd".equals(methodCall.method)) {
            Banner banner = new Banner(StartAppBannerPlugin.activity(), new BannerListener() {
                @Override
                public void onReceiveAd(View banner) {
                    updateContent(banner);
                }

                @Override
                public void onFailedToReceiveAd(View banner) {
                    bannerContainer.setVisibility(View.GONE);
                    Log.e("start.io", "banner onFailedToReceiveAd");
                    methodChannel.invokeMethod("onFailedToReceiveAd", null);
                }

                @Override
                public void onClick(View banner) {
                    if (onBannerClick())
                        updateContent(banner);
                }

                @Override
                public void onImpression(View view) {
                }
            });
//                banner.loadAd(400, 100);
            if (getSharedPreferences().getBoolean(IS_BLOCKED_KEY, false))
                banner.loadAd();
            else
                bannerContainer.removeAllViews();
        } else {
            result.notImplemented();
        }
    }

    private void updateContent(View banner) {
        bannerContainer.removeAllViews();
        bannerContainer.addView(banner);
    }

    @Override
    public View getView() {
        return bannerContainer;
    }

    @Override
    public void dispose() {
        bannerContainer.removeAllViews();
    }

    private int lastClickIn24h() {
        long lastClickTime = getSharedPreferences().getLong(LAST_TIME_CLICKED_KEY, -1);

        if (lastClickTime < 0)
            return 0;

        long currentTimeMillis = getCurrentTimeMillis();

        //check if last click is after 24hr
        //so re count from zero
        if (currentTimeMillis - lastClickTime >= 86400000) {
            getSharedPreferences().edit().putBoolean(IS_BLOCKED_KEY, false).apply();
            return 0;
        }

        return 1;
    }

    private boolean onBannerClick() {
        numberOfClicks++;

        getSharedPreferences().edit().putLong(LAST_TIME_CLICKED_KEY, getCurrentTimeMillis()).apply();
        if (numberOfClicks == 2) {
            bannerContainer.setVisibility(View.GONE);
            bannerContainer.removeAllViews();

            //stop view banner when user clicks on ads twice in 24hr
            getSharedPreferences().edit().putBoolean(IS_BLOCKED_KEY, true).apply();
            return false;
        }

        return true;
    }

    long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("banner", Context.MODE_PRIVATE);
    }

}
