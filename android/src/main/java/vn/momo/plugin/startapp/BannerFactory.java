package vn.momo.plugin.startapp;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

/**
 * @author dungvu
 * @since 2019-06-04
 */
public class BannerFactory extends PlatformViewFactory {
    private final BinaryMessenger messenger;
    private final Activity activity;

    BannerFactory(BinaryMessenger messenger, Activity activity) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
        this.activity = activity;
    }

    @Override
    public PlatformView create(Context context, int id, Object o) {
        return new FlutterBannerView(activity, messenger, id);
    }
}
