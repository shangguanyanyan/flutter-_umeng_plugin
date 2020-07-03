import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:umeng_plugin/constant.dart';

class UmengPlugin {
  static const MethodChannel _channel =
      const MethodChannel(UMENG_PLUGIN);

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod(PLATFORM_VERSION);
    return version;
  }
  static Future<bool> init(String key,
      {int mode = 0,
        bool reportCrash = true,
        bool encrypt = false,
        String channel = DEFAULT_CHANNEL,
        double interval = 30000,
        bool logEnable = false}) {
    Map<String, dynamic> args = {key: key};

    if (mode != null) {
      args[MODE] = mode;
    }
    if (reportCrash != null) {
      args[REPORT_CRASH] = reportCrash;
    }
    if (encrypt != null) {
      args[ENCRYPT] = encrypt;
    }
    if (channel != null) {
      args[CHANNEL] = channel;
    }
    if (interval != null) {
      args[INTERVAL] = interval;
    }
    if (logEnable != null) {
      args[LOG_ENABLE] = logEnable;
    }
    _channel.invokeMethod(INIT, args);
    return new Future.value(true);
  }
  /// 打开页面时进行统计
  /// [name]
  static Future<Null> beginPageView(String name) async {
    _channel.invokeMethod(BEGIN_PAGE_VIEW, {NAME: name});
  }

  /// 关闭页面时结束统计
  /// [name]
  static Future<Null> endPageView(String name) async {
    _channel.invokeMethod(END_PAGE_VIEW, {NAME: name});
  }

  /// 登陆统计
  /// [id]
  /// [interval]
  static Future<Null> loginPageView(String id, String channel,
      {int interval, String provider}) async {
    _channel.invokeMethod(LOGIN_PAGE_VIEW,
        {ID: id, INTERVAL: interval, PROVIDER: provider});
  }

  ///登出统计
  static Future<Null> logoutPageView() async {
    _channel.invokeMethod(LOGOUT_PAGE_VIEW);
  }

  /// 计数事件统计
  /// [eventId]  当前统计的事件ID
  /// [label] 事件的标签属性
  static Future<Null> eventCounts(String eventId, {String label}) async {
    _channel.invokeMethod(EVENT_COUNTS, {LABEL: label});
  }

  ///关闭错误统计方法
  static Future<Null> setCatchUncaughtExceptions(bool isEnable) async {
    _channel
        .invokeMethod(SET_CATCH_UNCAUGHT, {CATCH_EXCEPTION_IS_ENABLE: isEnable});
  }
  ///上报错误
  static Future<Null> reportErr(String errString) async {
    _channel.invokeMethod(
        REPORT_ERR, {IS_THROWABLE: false, ERR_STRING: errString});
  }

  ///自定义事件
  static Future<Null> customEvent(String eventId,
      {Map<String, dynamic> params}) {
    _channel.invokeMethod(CUSTOM_EVENT,
        {EVENT_ID: eventId, PARAMS: params ?? Map<String, dynamic>()});
  }
  ///获取渠道名
  static Future<String> getChannel() async{
    if(Platform.isIOS){
      return Future.value(APP_STORE);
    }
    String channel = await _channel.invokeMethod(GET_CHANNEL);
    return Future.value(channel);
  }
  //获取设备信息
  static Future<String> getDeviceId() async{
    String deviceId = await _channel.invokeMethod(GET_DEVICE_INFO);
    return Future.value(deviceId);
  }
}
