import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class UmengPlugin {
  static const MethodChannel _channel =
      const MethodChannel('umeng_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  static Future<bool> init(String key,
      {int mode = 0,
        bool reportCrash = true,
        bool encrypt = false,
        String channel = "Umeng",
        double interval = 30000,
        bool logEnable = false}) {
    Map<String, dynamic> args = {"key": key};

    if (mode != null) {
      args["mode"] = mode;
    }
    if (reportCrash != null) {
      args["reportCrash"] = reportCrash;
    }
    if (encrypt != null) {
      args["encrypt"] = encrypt;
    }
    if (channel != null) {
      args["channel"] = channel;
    }
    if (interval != null) {
      args["interval"] = interval;
    }
    if (logEnable != null) {
      args["logEnable"] = logEnable;
    }
    _channel.invokeMethod("init", args);
    return new Future.value(true);
  }
  /// 打开页面时进行统计
  /// [name]
  static Future<Null> beginPageView(String name) async {
    _channel.invokeMethod("beginPageView", {"name": name});
  }

  /// 关闭页面时结束统计
  /// [name]
  static Future<Null> endPageView(String name) async {
    _channel.invokeMethod("endPageView", {"name": name});
  }

  /// 登陆统计
  /// [id]
  /// [interval]
  static Future<Null> loginPageView(String id, String channel,
      {int interval, String provider}) async {
    _channel.invokeMethod("loginPageView",
        {"id": id, "interval": interval, "provider": provider});
  }

  ///登出统计
  static Future<Null> logoutPageView() async {
    _channel.invokeMethod("logoutPageView");
  }

  /// 计数事件统计
  /// [eventId]  当前统计的事件ID
  /// [label] 事件的标签属性
  static Future<Null> eventCounts(String eventId, {String label}) async {
    _channel.invokeMethod("eventCounts", {"label": label});
  }

  ///关闭错误统计方法
  static Future<Null> setCatchUncaughtExceptions(bool isEnable) async {
    _channel
        .invokeMethod("setCatchUncaught", {"catchExceptionIsEnable": isEnable});
  }
  ///上报错误
  static Future<Null> reportErr(String errString) async {
    _channel.invokeMethod(
        "reportErr", {"isThrowable": false, "errString": errString});
  }

  ///自定义事件
  static Future<Null> customEvent(String eventId,
      {Map<String, dynamic> params}) {
    _channel.invokeMethod("customEvent",
        {"eventId": eventId, "params": params ?? Map<String, dynamic>()});
  }
  ///获取渠道名
  static Future<String> getChannel() async{
    if(Platform.isIOS){
      return Future.value("appstore");
    }
    String channel = await _channel.invokeMethod("getChannel");
    return Future.value(channel);
  }
  //获取设备信息
  static Future<String> getDeviceId() async{
    String deviceId = await _channel.invokeMethod("getDeviceInfo");
    return Future.value(deviceId);
  }
}
