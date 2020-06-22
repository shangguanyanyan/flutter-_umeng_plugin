import 'dart:developer';
import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:umeng_plugin/umeng_plugin.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  String key = "";
  if (Platform.isAndroid) {
    key = '5e1c0c620cafb21f3e00000e';
  }
  if (Platform.isIOS) {
    key = '5e6f8f86978eea0774044b9a';
  }
  UmengPlugin.init(key,
      encrypt: true, channel: "test", reportCrash: true, logEnable: true);
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String deviceInfo = "未获取";

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await UmengPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> sendCustomEvent(String eventId, {Map params}) async {
    try {
      UmengPlugin.customEvent(eventId, params: params);
    } catch (e) {
      log("$e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ListView(
          children: <Widget>[
            button('Running on: $_platformVersion\n'),
            button("play_music", callback: () {
              var param = {
                "user_id":"${math.Random.secure().nextInt(100000)}",
                "music_type": "摇滚",
                "singer": "郑钧",
                "song_name": "赤裸裸",
                "song_price": "233"
              };
              sendCustomEvent("play_music", params: param);
            }),
            button("$deviceInfo", callback: () {
              getDeviceInfo();
            }),
          ],
          //child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }

  /*void getChannel() async{
    String channel = await UmengPlugin.getChannel();
    print(channel);
  }*/
  void getDeviceInfo() async {
    String info = await UmengPlugin.getDeviceId();
    setState(() {
      deviceInfo = info;
    });
  }

  Widget button(String content, {VoidCallback callback}) {
    return RaisedButton(
      onPressed: callback ??= () {},
      child: Text(content),
    );
  }
}
