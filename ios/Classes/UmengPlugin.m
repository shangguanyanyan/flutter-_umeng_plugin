#import "UmengPlugin.h"
#import <UMCommon/UMCommon.h>
#import <UMCommonLog/UMCommonLogHeaders.h>
#import <UMAnalytics/MobClick.h>
#import "UmengPluginMethods.h"

@implementation UmengPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"umeng_plugin"
            binaryMessenger:[registrar messenger]];
  UmengPlugin* instance = [[UmengPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString *method = call.method;
    NSLog(@"method%@",method);
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if([INIT isEqualToString:call.method]){
      [self init:call result:result];
  } else if([CUSTOME_EVENT isEqualToString:call.method]){
      [self customeEvent:call result:result];
  }else {
    //result(FlutterMethodNotImplemented);
  }
}

- (void)init:(FlutterMethodCall*)call result:(FlutterResult)result{
    BOOL isLogEnable = call.arguments[LOG_ENABLE];
    NSString *channel = call.arguments[CHANNEL];
    NSString *key = call.arguments[KEY];
    BOOL encrypt = call.arguments[ENCRYPT];
    NSLog(@"islogEnable:%d,channel:%@,key:%@,encrypt:%d"
      ,isLogEnable,channel,key,encrypt);
    
    
    [UMConfigure initWithAppkey:key channel:channel];
    [UMConfigure setEncryptEnabled:encrypt];
    if(isLogEnable){
        [UMCommonLogManager setUpUMCommonLogManager];
        [UMConfigure setLogEnabled:isLogEnable];
    }
}
-(void)customeEvent:(FlutterMethodCall*)call result:(FlutterResult)result{
    //初始化字典
    NSDictionary *num = [[NSDictionary alloc] initWithObjectsAndKeys:@"one", @"num1", @"two", @"num2", @"three", @"num3", nil];
    //注：先写value，再写key，一对key-value是一个元素，nil作为字典存放元素的结束标志。

    //便利构造器
    NSDictionary *num1 = [NSDictionary dictionaryWithObjectsAndKeys:@"one",@"num1",@"two",@"num2",nil];

    //字面量
    //使用“ @{} ”，“ {} ”中存入字典元素，key：value一一对应，元素之间使用”，”相隔。
    NSDictionary *num2 = @{@"num1":@"one",@"num2":@"two"};
    //NSLog(@"%@,%@,%@",num,num1,num2);
    NSDictionary *param = call.arguments[PARAMS];
    NSLog(@"直接打印call.arguments%@",call.arguments[PARAMS]);
    NSLog(@"打印param%@",param);
    NSString *eventId = call.arguments[EVENT_ID];
    
    if(param.count>0){
        [MobClick event:eventId attributes:param];
    }else{
        [MobClick event:eventId];
    }
}

@end
