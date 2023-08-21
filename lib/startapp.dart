import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef void BannerCreatedCallback(BannerController controller);
typedef StringToVoidFunc = void Function(String?);

const String PLUGIN_KEY = "vn.momo.plugin.startapp.StartAppBannerPlugin";

class StartApp {
  static const platform = const MethodChannel('flutter_startapp');
  static VoidCallback? onVideoCompleted;
  static VoidCallback? onReceiveAd;
  static StringToVoidFunc? onFailedToReceiveAd;

  static init(String appId) async {
    await platform.invokeMethod(
      'init',
      <String, dynamic>{
        'app_id': appId,
      },
    );
  }

  static enableReturnAds(bool enableReturnAds) async {
    await platform.invokeMethod(
      'enableReturnAds',
      <String, dynamic>{
        'enableReturnAds': enableReturnAds,
      },
    );
  }

  static showInterstitialAd() async {
    await platform.invokeMethod('showAd');
  }

  static showRewardedAd(
      {VoidCallback? onVideoCompleted,
      VoidCallback? onReceiveAd,
      StringToVoidFunc? onFailedToReceiveAd}) async {
    StartApp.onVideoCompleted = onVideoCompleted;
    platform.setMethodCallHandler(_handleMethod);
    await platform.invokeMethod('showRewardedAd');
  }

  static Future<dynamic> _handleMethod(MethodCall call) {
    switch (call.method) {
      case "onVideoCompleted":
        if (onVideoCompleted != null) {
          onVideoCompleted!();
        }
        break;
      case "onReceiveAd":
        if (onReceiveAd != null) {
          onReceiveAd!();
        }
        break;
      case "onFailedToReceiveAd":
        if (onFailedToReceiveAd != null) {
          onFailedToReceiveAd!(call.arguments);
        }
        break;
    }
    return Future<dynamic>.value(null);
  }
}

class AdBanner extends StatefulWidget {
  const AdBanner({
    Key? key,
    this.onCreated,
    this.onFailedToReceiveAd,
  }) : super(key: key);

  final BannerCreatedCallback? onCreated;
  final Function? onFailedToReceiveAd;

  @override
  State<AdBanner> createState() => _BannerState();
}

class _BannerState extends State<AdBanner> {
  bool faildToLoad = false;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return faildToLoad
          ? SizedBox(
              width: 0,
              height: 0,
            )
          : Container(
              width: 400.0,
              height: 100.0,
              child: AndroidView(
                viewType: PLUGIN_KEY,
                onPlatformViewCreated: _onPlatformViewCreated,
              ),
            );
    }
    return Text('$defaultTargetPlatform is no need showing ads');
  }

  void _onPlatformViewCreated(int id) {
    BannerController controller = new BannerController._(id);
    controller.loadAd();

    controller._channel.setMethodCallHandler(methodCallHandler);

    if (widget.onCreated == null) {
      return;
    }
    widget.onCreated!(controller);
  }

  Future methodCallHandler(MethodCall call) async {
    if (call.method == "onFailedToReceiveAd") {
      widget.onFailedToReceiveAd?.call();
      faildToLoad = true;
      if(mounted)
        setState(() {

        });
    }
  }
}

class BannerController {
  BannerController._(int id)
      : _channel = new MethodChannel('${PLUGIN_KEY}_$id');

  final MethodChannel _channel;

  Future<void> loadAd() async {
    return _channel.invokeMethod('loadAd');
  }
}
