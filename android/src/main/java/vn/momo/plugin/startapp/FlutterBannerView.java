package vn.momo.plugin.startapp;

import android.content.Context;
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

    private final FrameLayout bannerContainer;
    private final Context context;
    private final MethodChannel methodChannel;

    FlutterBannerView(Context context, BinaryMessenger messenger, int id) {
        this.context = context;
        bannerContainer = new FrameLayout(context);
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
                    if (LimitAdClickUtils.onAdClick(context))
                        updateContent(banner);
                    else {
                        bannerContainer.setVisibility(View.GONE);
                        bannerContainer.removeAllViews();
                    }
                }

                @Override
                public void onImpression(View view) {
                }
            });

            if (LimitAdClickUtils.userIsBlocked(context)) {
                bannerContainer.removeAllViews();
            } else {
                banner.loadAd();
            }
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


}
