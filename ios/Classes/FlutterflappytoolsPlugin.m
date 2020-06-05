#import "FlutterflappytoolsPlugin.h"
#import "AudioToolbox/AudioToolbox.h"

@implementation FlutterflappytoolsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutterflappytools"
                                     binaryMessenger:[registrar messenger]];
    FlutterflappytoolsPlugin* instance = [[FlutterflappytoolsPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

//控制信息
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if([@"getPathSize" isEqualToString:call.method]){
        //获取路径
        NSString* path=(NSString*)call.arguments[@"path"];
        //获取类型
        NSString* type=(NSString*)call.arguments[@"type"];
        //计算大小
        long long retSize=[self caculateSize:path];
        //返回字符串
        NSString* retStr=@"";
        //B
        if(type.intValue==1){
            retStr=[NSString stringWithFormat:@"%.2f",(float)retSize];
        }
        //KB
        else if(type.intValue==2){
            retStr=[NSString stringWithFormat:@"%.2f",(float)retSize/1024];
        }
        //MB
        else if(type.intValue==3){
            retStr=[NSString stringWithFormat:@"%.2f",(float)retSize/1048576];
        }
        //GB
        else if(type.intValue==4){
            retStr=[NSString stringWithFormat:@"%.2f",(float)retSize/1073741824];
        }
        result(retStr);
    }
    //清空路径
    else if([@"clearPath" isEqualToString:call.method]){
        //清空路径下面的所有缓存
        NSString* path=(NSString*)call.arguments[@"path"];
        //清空
        NSFileManager *fileManager = [NSFileManager defaultManager];
        //清空
        [fileManager removeItemAtPath:path error:nil];
        //返回成功
        result(@"1");
    }
    //获取亮度
    else  if([@"getBrightness" isEqualToString:call.method]){
        CGFloat brightness=[UIScreen mainScreen].brightness;
        //播放视频
        result([NSString stringWithFormat:@"%.2f",brightness]);
    }
    //设置亮度
    else if([@"setBrightness" isEqualToString:call.method]){
        NSString* brightness=(NSString*)call.arguments[@"brightness"];
        CGFloat fla=brightness.doubleValue;
        [[UIScreen mainScreen] setBrightness:fla];
        result(@"1");
    }
    //获取当前电池的电量
    else if([@"getBatteryLevel" isEqualToString:call.method]){
        [UIDevice currentDevice].batteryMonitoringEnabled = YES;
        double deviceLevel = [UIDevice currentDevice].batteryLevel;
        result([NSString stringWithFormat:@"%f",deviceLevel]);
    }
    //获取当前的充电状态
    else if([@"getBatteryCharge" isEqualToString:call.method]){
        if([UIDevice currentDevice].batteryState==UIDeviceBatteryStateCharging){
            result(@"1");
        }else{
            result(@"0");
        }
    }
    //设置常亮
    else if([@"setSceenSteadyLight" isEqualToString:call.method]){
        NSString* state=(NSString*)call.arguments[@"state"];
        //设置屏幕常亮
        if([state isEqualToString:@"1"]){
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        }else{
            [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
        }
        result(@"1");
    }
    //获取当前的充电状态
    else if([@"changeStatusBar" isEqualToString:call.method]){
        NSString* type=(NSString*)call.arguments[@"type"];
        if([type isEqualToString:@"1"]){
            [UIApplication sharedApplication].statusBarStyle=UIStatusBarStyleLightContent;
        }else  if([type isEqualToString:@"2"]){
            if (@available(iOS 13.0, *)) {
                [UIApplication sharedApplication].statusBarStyle=UIStatusBarStyleDarkContent;
            } else {
                [UIApplication sharedApplication].statusBarStyle=UIStatusBarStyleDefault;
            }
        }
    }
    //显示状态栏
    else if([@"setStatusBarShow" isEqualToString:call.method]){
        //返回
        NSString* flag=(NSString*)call.arguments[@"show"];
        //代理成功
        if([flag isEqualToString:@"1"]){
            [[UIApplication sharedApplication] setStatusBarHidden:false withAnimation:UIStatusBarAnimationFade];
        }else{
            [[UIApplication sharedApplication] setStatusBarHidden:true withAnimation:UIStatusBarAnimationFade];
        }
        result(@"1");
    }
    //整栋
    else  if([@"shake" isEqualToString:call.method]){
        //调用系统震动
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        //播放视频
        result(@"1");
    }
    else if([@"share" isEqualToString:call.method]){
        NSString* share=(NSString*)call.arguments[@"share"];
        NSArray * activityItems = [[NSArray alloc] initWithObjects:share, nil];
        UIActivityViewController * activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
        UIActivityViewControllerCompletionWithItemsHandler myBlock = ^(UIActivityType activityType, BOOL completed, NSArray *returnedItems, NSError *activityError) {
            NSLog(@"%@",activityType);
            if (completed) {
                NSLog(@"分享成功");
            } else {
                NSLog(@"分享失败");
            }
            [activityVC dismissViewControllerAnimated:YES completion:nil];
        };
        activityVC.completionWithItemsHandler = myBlock;
        //拿到最顶层的controller
        UIViewController *topController = [self _topViewController:[[UIApplication sharedApplication].keyWindow rootViewController]];
        
        [topController presentViewController:activityVC animated:YES completion:nil];
        
        result(@"true");
    }
    else {
        result(FlutterMethodNotImplemented);
    }
}

//这里是获取整个应用的顶部Controller;
- (UIViewController *)_topViewController:(UIViewController *)vc {
    if ([vc isKindOfClass:[UINavigationController class]]) {
        return [self _topViewController:[(UINavigationController *)vc topViewController]];
    } else if ([vc isKindOfClass:[UITabBarController class]]) {
        return [self _topViewController:[(UITabBarController *)vc selectedViewController]];
    } else {
        return vc;
    }
    return nil;
}

//获取大小
-(long long)caculateSize:(NSString*) filePath{
    if([self isDirectory:filePath]){
        return [self folderSizeAtPath:filePath];
    }else{
        return [self fileSizeAtPath:filePath];
    }
}

//判断是否是文件夹
- (BOOL)isDirectory:(NSString *)filePath
{
    BOOL isDirectory = NO;
    [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDirectory];
    return isDirectory;
}

//通常用于删除缓存的时，计算缓存大小
- (long long) fileSizeAtPath:(NSString*) filePath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if ([manager fileExistsAtPath:filePath]){
        return [[manager attributesOfItemAtPath:filePath error:nil] fileSize];
    }
    return 0;
}

//遍历文件夹获得文件夹大小，返回多少M
- (long long) folderSizeAtPath:(NSString*) folderPath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if (![manager fileExistsAtPath:folderPath]) return 0;
    NSEnumerator *childFilesEnumerator = [[manager subpathsAtPath:folderPath] objectEnumerator];
    NSString* fileName;
    long long folderSize = 0;
    while ((fileName = [childFilesEnumerator nextObject]) != nil){
        NSString* fileAbsolutePath = [folderPath stringByAppendingPathComponent:fileName];
        folderSize += [self fileSizeAtPath:fileAbsolutePath];
    }
    return folderSize;
}

@end
