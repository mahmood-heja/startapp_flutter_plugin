package vn.momo.plugin.startapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import io.flutter.BuildConfig;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformViewRegistry;

/**
 * @author dungvu
 * @since 2019-06-04
 */
public class StartAppBannerPlugin implements FlutterPlugin, ActivityAware {
    static final String PLUGIN_KEY = "vn.momo.plugin.startapp.StartAppBannerPlugin";
    private static final String STARTAPP_SPLASH_AD_ENABLED_KEY = "vn.momo.plugin.startapp.SPLASH_AD_ENABLED";

    private static  Activity mainActivity;
    private  StartAppAd startAppAd;

    /*
      still contain the static registerWith() method to remain compatible
      with apps that don’t use the v2 Android embedding
     */

    /*
     * Dev still finding a better solution to not keep {@link Activity} as a static field.
     * Keep the method to support embedding v1 logic - using {@link StartAppBannerPlugin#(Registrar)}
     */
    public  static  Activity getActivity(){
        return  mainActivity;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        pluginLogic(binding.getPlatformViewRegistry(), binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {

    }

    private void pluginLogic(PlatformViewRegistry platformViewRegistry,
                                    BinaryMessenger messenger) {

        platformViewRegistry.registerViewFactory(PLUGIN_KEY, new BannerFactory(messenger));

        final MethodChannel channel = new MethodChannel(messenger, "flutter_startapp");
        channel.setMethodCallHandler(
                (call, result) -> {
                    switch (call.method) {
                        case "showAd":
                            if (LimitAdClickUtils.userIsBlocked(mainActivity)) {
                                result.error("User blocked for 24h", null, null);
                            } else {
                                startAppAd.showAd(new AdDisplayListener() {
                                    @Override
                                    public void adHidden(Ad ad) {

                                    }

                                    @Override
                                    public void adDisplayed(Ad ad) {

                                    }

                                    @Override
                                    public void adClicked(Ad ad) {
                                        LimitAdClickUtils.onAdClick(mainActivity);
                                    }

                                    @Override
                                    public void adNotDisplayed(Ad ad) {

                                    }


                                });
                                result.success(null);
                            }
                            break;
                        case "showRewardedAd":
                            startAppAd.setVideoListener(() -> {
                                channel.invokeMethod("onVideoCompleted", null);
                                Log.d("onVideoCompleted", "Complete");
                            });
                            startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull Ad ad) {
                                    startAppAd.showAd();
                                    channel.invokeMethod("onReceiveAd", null);
                                }

                                @Override
                                public void onFailedToReceiveAd(Ad arg0) {
                                    channel.invokeMethod("onFailedToReceiveAd",
                                            arg0.getErrorMessage());
                                    Log.e("StartAppPlugin",
                                            "Failed to load rewarded video with reason: "
                                                    + arg0.getErrorMessage());
                                }
                            });
                            result.success(null);
                            break;
                        case "init":
                            String app_id = call.argument("app_id");
                            if (app_id == null) {
                                result.error("start.io init: app id must not be null", null, null);
                                break;
                            }
                            StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
                            StartAppSDK.init(mainActivity, app_id);
                            Log.i("start_app", "init app_id start.io : " + app_id);
                            result.success(null);
                            break;
                        case "enableReturnAds" :
                            Boolean enable = call.argument("enableReturnAds");
                            if(enable == null)
                                enable = false;

                            StartAppSDK.enableReturnAds(enable);
                            result.success(null);
                            break;
                        default:
                            result.notImplemented();
                    }
                });
    }

    private  void bindActivity(Activity activity) {
        Context context = activity.getApplicationContext();

        boolean splashAppEnabled = true;
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            splashAppEnabled = bundle.getBoolean(STARTAPP_SPLASH_AD_ENABLED_KEY, true);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (!splashAppEnabled) {
            StartAppAd.disableSplash();
        }

        // https://github.com/StartApp-SDK/StartApp_InApp_SDK_Example/#set-up-test-ad
        // NOTE always use test ads during development and testing
        StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);

        mainActivity = activity;
        startAppAd = new StartAppAd(context);
        LimitAdClickUtils.init(mainActivity);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        bindActivity(activityPluginBinding.getActivity());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        bindActivity(activityPluginBinding.getActivity());
    }

    @Override
    public void onDetachedFromActivity() {
        mainActivity = null;
        startAppAd = null;
    }
}
