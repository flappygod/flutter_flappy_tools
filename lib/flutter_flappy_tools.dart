import 'package:flutter/services.dart';
import 'dart:convert';
import 'dart:async';
import 'dart:io';

//path size type
enum PathSizeType { typeB, typeKB, typeMB, typeGB }

//status bar
enum StatusBarType { white, black }

//tools
class FlutterFlappyTools {
  //channel
  static const MethodChannel _channel = MethodChannel('flutter_flappy_tools');

  //path size
  static Future<String?> getPathSize(String path, PathSizeType type) async {
    int typeInt = 1;
    if (type == PathSizeType.typeB) {
      typeInt = 1;
    }
    if (type == PathSizeType.typeKB) {
      typeInt = 2;
    }
    if (type == PathSizeType.typeMB) {
      typeInt = 3;
    }
    if (type == PathSizeType.typeGB) {
      typeInt = 4;
    }
    //video path
    final String? size = await _channel.invokeMethod('getPathSize', {
      "path": path,
      "type": typeInt,
    });
    return size;
  }

  //clear path
  static Future<String?> clearPath(String path) async {
    final String? flag = await _channel.invokeMethod('clearPath', {
      "path": path,
    });
    return flag;
  }

  //brightness
  static Future<double> getBrightness() async {
    final String? brightness = await _channel.invokeMethod('getBrightness', {});
    return double.parse(brightness ?? "0");
  }

  //brightness
  static Future<String?> setBrightness(double brightness) async {
    final String? set = await _channel.invokeMethod('setBrightness', {
      "brightness": brightness.toStringAsFixed(2),
    });
    return set;
  }

  //stay light
  static Future<bool> setScreenSteadyLight(bool state) async {
    final String? ret = await _channel.invokeMethod('setScreenSteadyLight', {
      "state": (state ? "1" : "0"),
    });
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //get device local
  static Future<String?> getDeviceLocal() async {
    final String? local = await _channel.invokeMethod('getDeviceLocal', {});
    return local;
  }

  //battery level
  static Future<double> getBatteryLevel() async {
    final String? ret = await _channel.invokeMethod('getBatteryLevel', {});
    return double.parse(ret ?? "0");
  }

  //charge
  static Future<bool> getBatteryCharge() async {
    final String? ret = await _channel.invokeMethod('getBatteryCharge', {});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //shake
  static Future<bool> shake() async {
    final String? ret = await _channel.invokeMethod('shake', {});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //share
  static Future<bool> share(String share) async {
    final String? ret = await _channel.invokeMethod('share', {"share": share});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //jump to url
  static Future<bool> jumpToUrl(String url) async {
    final String? ret = await _channel.invokeMethod('jumpToUrl', {"url": url});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //jump to scheme
  static Future<bool> jumpToScheme(String url) async {
    final String? ret = await _channel.invokeMethod('jumpToScheme', {
      "url": url,
    });
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //jump to intent
  static Future<Map> jumpToIntent(String url) async {
    final String? ret = await _channel.invokeMethod('jumpToIntent', {
      "url": url,
    });
    return ret == null ? {} : jsonDecode(ret);
  }

  //go home
  static Future<bool> goHome({String? toast}) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('goHome', {"toast": toast});
      return true;
    }
    return true;
  }

  //set cookie
  static Future<bool> addManagerCookie(String url, String cookie) async {
    final String? ret = await _channel.invokeMethod('addManagerCookie', {
      "url": url,
      "cookie": cookie,
    });
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }
}
