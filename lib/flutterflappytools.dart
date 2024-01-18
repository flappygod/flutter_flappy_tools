import 'package:flutter/services.dart';
import 'dart:convert';
import 'dart:async';
import 'dart:io';

//path size type
enum PathSizeType {
  TYPE_B,
  TYPE_KB,
  TYPE_MB,
  TYPE_GB,
}

//status bar
enum StatusBarType {
  WHITE,
  BLACK,
}

//map type
enum MapType {
  //apple
  MapApple,
  //a map
  MapAMap,
  //baidu
  MapBaidu,
  //tencent
  MapTencent,
}

//permission type
enum PermissionType {
  notification,
  camera,
  photo,
}

//tools
class FlutterFlappyTools {
  //channel
  static const MethodChannel _channel = const MethodChannel('flutterflappytools');

  //get map type
  static String _getMapType(MapType type) {
    int mapType = 0;
    switch (type) {
      case MapType.MapApple:
        mapType = 0;
        break;
      case MapType.MapAMap:
        mapType = 1;
        break;
      case MapType.MapBaidu:
        mapType = 2;
        break;
      case MapType.MapTencent:
        mapType = 3;
        break;
    }
    return mapType.toString();
  }

  //get permission type
  static String _getPermissionType(PermissionType type) {
    int permissionType = 0;
    switch (type) {
      case PermissionType.notification:
        permissionType = 0;
        break;
      case PermissionType.camera:
        permissionType = 1;
        break;
      case PermissionType.photo:
        permissionType = 2;
        break;
    }
    return permissionType.toString();
  }

  //path size
  static Future<String?> getPathSize(String path, PathSizeType type) async {
    int typeInt = 1;
    if (type == PathSizeType.TYPE_B) {
      typeInt = 1;
    }
    if (type == PathSizeType.TYPE_KB) {
      typeInt = 2;
    }
    if (type == PathSizeType.TYPE_MB) {
      typeInt = 3;
    }
    if (type == PathSizeType.TYPE_GB) {
      typeInt = 4;
    }
    //video path
    final String? size = await _channel.invokeMethod('getPathSize', {"path": path, "type": typeInt});
    return size;
  }

  //clear path
  static Future<String?> clearPath(String path) async {
    final String? flag = await _channel.invokeMethod('clearPath', {"path": path});
    return flag;
  }

  //brightness
  static Future<double> getBrightness() async {
    final String? brightness = await _channel.invokeMethod('getBrightness', {});
    return double.parse(brightness ?? "0");
  }

  //brightness
  static Future<String?> setBrightness(double brightness) async {
    final String? set = await _channel.invokeMethod('setBrightness', {"brightness": brightness.toStringAsFixed(2)});
    return set;
  }

  //stay light
  static Future<bool> setScreenSteadyLight(bool state) async {
    final String? ret = await _channel.invokeMethod('setScreenSteadyLight', {"state": (state ? "1" : "0")});
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
    final String? ret = await _channel.invokeMethod('jumpToScheme', {"url": url});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //jump to intent
  static Future<Map> jumpToIntent(String url) async {
    final String? ret = await _channel.invokeMethod('jumpToIntent', {"url": url});
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
    final String? ret = await _channel.invokeMethod('addManagerCookie', {"url": url, "cookie": cookie});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //is map installed
  static Future<bool> isMapInstalled(MapType type) async {
    final String? ret = await _channel.invokeMethod('isMapInstalled', {"type": _getMapType(type)});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //jump to map
  static Future<bool> jumpToMap(MapType type, String lat, String lng, String title) async {
    final String? ret = await _channel.invokeMethod('jumpToMap', {"type": _getMapType(type), "lat": lat, "lng": lng, "title": title});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //check  permission
  static Future<bool> checkPermission(PermissionType permissionType) async {
    final String? ret = await _channel.invokeMethod('checkPermission', {
      "type": _getPermissionType(permissionType),
    });
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }
}
