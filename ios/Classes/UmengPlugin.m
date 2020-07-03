#import "UmengPlugin.h"
#import <UMCommon/UMCommon.h>
#import <UMCommonLog/UMCommonLogHeaders.h>
#import <UMCommon/MobClick.h>
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
  }else if([PROFILE_SIGN_IN isEqualToString:call.method]){
      [self profileSignIn:call result:result];
  }else if([PROFILE_SIGN_OFF isEqualToString:call.method]){
      [self profileSignOff:call result:result];
  }else{
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
    
    NSDictionary *param = call.arguments[PARAMS];
    NSString *eventId = call.arguments[EVENT_ID];
    
    if(param.count>0){
        [MobClick event:eventId attributes:param];
    }else{
        [MobClick event:eventId];
    }
}

-(void)profileSignIn:(FlutterMethodCall*)call result:(FlutterResult) result{
    NSString *provider = call.arguments[PROVIDER];
    NSString *puid = call.arguments[PUID];
    if(provider != NULL){
        [MobClick profileSignInWithPUID:puid provider:provider];
    }else{
        [MobClick profileSignInWithPUID:puid];
    }
    
}

-(void)profileSignOff:(FlutterMethodCall*)call result:(FlutterResult) result{
    [MobClick profileSignOff];
}

@end
