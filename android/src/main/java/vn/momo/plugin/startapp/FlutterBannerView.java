package vn.momo.plugin.startapp;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

/**
 * @author dungvu
 * @since 2019-06-04
 */
public class FlutterBannerView implements PlatformView, MethodChannel.MethodCallHandler, ActivityAware {

    private final FrameLayout bannerContainer;
    private final Activity activity;
    private final MethodChannel methodChannel;

    FlutterBannerView(Activity context, BinaryMessenger messenger, int id) {
        this.activity = context;
        bannerContainer = new FrameLayout(context);
        methodChannel = new MethodChannel(messenger, StartAppBannerPlugin.PLUGIN_KEY + "_" + id);
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, @NonNull MethodChannel.Result result) {

        if ("loadAd".equals(methodCall.method)) {

            if (LimitAdClickUtils.userIsBlocked(activity)) {
                Log.e("start.io", "banner blocked for 24hrs");
                bannerContainer.setVisibility(View.GONE);
                bannerContainer.removeAllViews();

            } else {
                Banner banner = new Banner(activity, new BannerListener() {
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
                        if (LimitAdClickUtils.onAdClick(activity))
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


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
    }

    @Override
    public void onDetachedFromActivity() {

    }
}
