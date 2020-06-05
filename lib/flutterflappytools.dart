import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'dart:io';

//类型
enum PathSizeType {
  TYPE_B,
  TYPE_KB,
  TYPE_MB,
  TYPE_GB,
}

//顶部
enum StatusBarType {
  WHITE,
  BLACK,
}

//工具
class Flutterflappytools {
  //工具
  static const MethodChannel _channel =
      const MethodChannel('flutterflappytools');

  //获取文件夹的大小
  static Future<String> getPathSize(String path, PathSizeType type) async {
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
    //返回视频的地址
    final String size = await _channel
        .invokeMethod('getPathSize', {"path": path, "type": typeInt});
    return size;
  }

  //清空目录
  static Future<String> clearPath(String path) async {
    final String flag =
        await _channel.invokeMethod('clearPath', {"path": path});
    return flag;
  }

  //获取当前的屏幕亮度
  static Future<double> getBrightness() async {
    final String brightness = await _channel.invokeMethod('getBrightness', {});
    return double.parse(brightness);
  }

  //设置屏幕亮度
  static Future<String> setBrightness(double brightness) async {
    final String set = await _channel.invokeMethod(
        'setBrightness', {"brightness": brightness.toStringAsFixed(2)});
    return set;
  }

  //获取电量
  static Future<double> getBatteryLevel() async {
    final String ret = await _channel.invokeMethod('getBatteryLevel', {});
    return double.parse(ret);
  }

  //获取当前的充电状态
  static Future<bool> getBatteryCharge() async {
    final String ret = await _channel.invokeMethod('getBatteryCharge', {});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //设置屏幕是否一致常亮
  static Future<bool> setSceenSteadyLight(bool state) async {
    final String ret = await _channel
        .invokeMethod('setSceenSteadyLight', {"state": (state ? "1" : "0")});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //设置为白色模式
  static Future<bool> changeStatusBar(StatusBarType type) async {
    if (Platform.isAndroid) {
      //白色
      if (type == StatusBarType.WHITE) {
        SystemUiOverlayStyle systemUiOverlayStyle = SystemUiOverlayStyle.light
            .copyWith(statusBarColor: Colors.transparent);
        SystemChrome.setSystemUIOverlayStyle(systemUiOverlayStyle);
      }
      //黑色
      else if (type == StatusBarType.BLACK) {
        SystemUiOverlayStyle systemUiOverlayStyle = SystemUiOverlayStyle.dark
            .copyWith(statusBarColor: Colors.transparent);
        SystemChrome.setSystemUIOverlayStyle(systemUiOverlayStyle);
      }
    } else if (Platform.isIOS) {
      //白色
      if (type == StatusBarType.WHITE) {
        SystemUiOverlayStyle systemUiOverlayStyle = SystemUiOverlayStyle.light
            .copyWith(statusBarColor: Colors.transparent);
        SystemChrome.setSystemUIOverlayStyle(systemUiOverlayStyle);
        await _channel.invokeMethod('changeStatusBar', {"type": "1"});
      }
      //黑色
      else if (type == StatusBarType.BLACK) {
        SystemUiOverlayStyle systemUiOverlayStyle = SystemUiOverlayStyle.dark
            .copyWith(statusBarColor: Colors.transparent);
        SystemChrome.setSystemUIOverlayStyle(systemUiOverlayStyle);
        await _channel.invokeMethod('changeStatusBar', {"type": "2"});
      }
    }
    return true;
  }

  //隐藏和显示状态栏
  static Future<bool> setStatusBarShow(bool show) async {
    final String ret = await _channel
        .invokeMethod('setStatusBarShow', {"show": show ? "1" : "0"});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //设置颜色1234567890
  static Future<bool> setStatusBarColor(Color color) async {
    if (Platform.isAndroid) {
      final String ret = await _channel
          .invokeMethod('setStatusBarColor', {"color": color.value.toString()});
      if (ret == "1") {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  //设置透明1234567890
  static Future<bool> transStatusBar() async {
    if (Platform.isAndroid) {
      final String ret = await _channel.invokeMethod('transStatusBar', {});
      if (ret == "1") {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  //调用震动
  static Future<bool> shake() async {
    final String ret = await _channel.invokeMethod('shake', {});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //分享字符串
  static Future<bool> share(String share) async {
    final String ret = await _channel.invokeMethod('share', {"share": share});
    if (ret == "1") {
      return true;
    } else {
      return false;
    }
  }

  //返回主界面
  static Future goHome() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('goHome', {});
      return true;
    }
  }
}
