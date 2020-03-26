import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:umeng_plugin/umeng_plugin.dart';

void main() => runApp(MyApp());

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
    String key = "";
    if(Platform.isAndroid){
      key = '5e6f4656570df35f24000057';
    }
    if(Platform.isIOS){
      key = '5e6f8f86978eea0774044b9a';
    }
    UmengPlugin.init(key, encrypt: true, channel: "example",reportCrash: true,logEnable: true);
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
      await UmengPlugin.customEvent(eventId, params: params);
    } on Exception {
      //Fluttertoast.showToast(msg: "err");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body:  ListView(
          children: <Widget>[
            button('Running on: $_platformVersion\n'),
            button("个人中心", callback: () {
              sendCustomEvent("home_userCenter_click");
            }),
            button("学习tab", callback: () {
              sendCustomEvent("home_leanrTab_click");
            }),
            button("课程tab", callback: () {
              sendCustomEvent("home_courseTab_click");
            }),
            button("今日任务", callback: () {
              var params = {
                "currentTask_name": "口译",
                "currentTask_title": "第三天"
              };
              sendCustomEvent("home_currentTask_click", params: params);
            }),
            button("我的课程", callback: () {
              sendCustomEvent("home_course_click",
                  params: {"course_name": "新年测试班级", "course_state": "曾经开过"});
            }),
            button("我的课程-全部按钮", callback: () {
              sendCustomEvent("home_courseAll_click");
            }),
            button("推荐课程-报名", callback: () {
              sendCustomEvent("home_recommend_click", params: {
                "recommend_name": "我推荐的课程",
                "recommend_time": "${DateTime.now()}",
                "recommend_type": "报名中"
              });
            }),
            button("推荐课程-全部按钮", callback: () {
              sendCustomEvent("home_recommendAll_click");
            }),
            button("课程tab-报名", callback: () {
              sendCustomEvent("home_recommendTab_content_click", params: {
                "recommendTab_name": "tab里的课程",
                "recommendTab_time": "${DateTime.now()}",
                "recommendTab_type": "不可名之状态"
              });
            }),
            button("channel名",callback: (){
              //getChannel();
            }),
            button("设备信息",callback: (){
              //getDeviceInfo();
            })
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
  /*void getDeviceInfo() async{
    String info = await UmengPlugin.getDeviceId();
    setState(() {
      deviceInfo = info;
    });
  }*/

  Widget button(String content, {VoidCallback callback}) {
    return RaisedButton(
      onPressed: callback ??= () {},
      child: Text(content),
    );
  }

}
