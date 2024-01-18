#import "FlutterflappytoolsPlugin.h"
#import "AudioToolbox/AudioToolbox.h"
#import <UserNotifications/UserNotifications.h>
#import <AVFoundation/AVFoundation.h>
#import <MapKit/MapKit.h>
#import <Photos/Photos.h>



@implementation FlutterflappytoolsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    
    FlutterflappytoolsPlugin* instance = [[FlutterflappytoolsPlugin alloc] init];
    //create method channel
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutterflappytools"
                                     binaryMessenger:[registrar messenger]];
    [registrar addMethodCallDelegate:instance channel:channel];
    
    
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if([@"getPathSize" isEqualToString:call.method]){
        NSString* path=(NSString*)call.arguments[@"path"];
        NSString* type=(NSString*)call.arguments[@"type"];
        long long retSize=[self caculateSize:path];
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
    //clear path
    else if([@"clearPath" isEqualToString:call.method]){
        NSString* path=(NSString*)call.arguments[@"path"];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        [fileManager removeItemAtPath:path error:nil];
        result(@"1");
    }
    //get brightness
    else  if([@"getBrightness" isEqualToString:call.method]){
        CGFloat brightness=[UIScreen mainScreen].brightness;
        result([NSString stringWithFormat:@"%.2f",brightness]);
    }
    //get device current locale
    else  if([@"getDeviceLocal" isEqualToString:call.method]){
        UIDevice* currentDevice = [UIDevice currentDevice];
        NSArray*  languageArray = [NSLocale preferredLanguages];
        if(languageArray.count==0){
            result(@"en-US");
            return;
        }
        NSString* language = [languageArray objectAtIndex:0];
        result(language);
    }
    //setBrightness
    else if([@"setBrightness" isEqualToString:call.method]){
        NSString* brightness=(NSString*)call.arguments[@"brightness"];
        CGFloat fla=brightness.doubleValue;
        [[UIScreen mainScreen] setBrightness:fla];
        result(@"1");
    }
    //getBatteryLevel
    else if([@"getBatteryLevel" isEqualToString:call.method]){
        [UIDevice currentDevice].batteryMonitoringEnabled = YES;
        double deviceLevel = [UIDevice currentDevice].batteryLevel;
        result([NSString stringWithFormat:@"%f",deviceLevel]);
    }
    //getBatteryCharge
    else if([@"getBatteryCharge" isEqualToString:call.method]){
        if([UIDevice currentDevice].batteryState==UIDeviceBatteryStateCharging){
            result(@"1");
        }else{
            result(@"0");
        }
    }
    //getBatteryCharge
    else if([@"checkPermission" isEqualToString:call.method]){
        NSInteger type=((NSString*)call.arguments[@"type"]).intValue;
        if(type==0){
            [[UNUserNotificationCenter currentNotificationCenter]
             requestAuthorizationWithOptions:(UNAuthorizationOptionAlert + UNAuthorizationOptionSound + UNAuthorizationOptionBadge)
             completionHandler:^(BOOL granted, NSError * _Nullable error) {
                if(granted){
                    result(@"1");
                } else {
                    result(@"0");
                }
            }];
            return;
        }
        if(type==1){
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
                if(granted){
                    result(@"1");
                } else {
                    result(@"0");
                }
            }];
            return;
        }
        if(type==2){
            [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
                switch (status) {
                    case PHAuthorizationStatusLimited:
                    case PHAuthorizationStatusAuthorized:
                        result(@"1");
                        break;
                    case PHAuthorizationStatusDenied:
                        result(@"0");
                        break;
                    case PHAuthorizationStatusNotDetermined:
                        result(@"0");
                        break;
                    case PHAuthorizationStatusRestricted:
                        result(@"0");
                        break;
                }
            }];
            return;
        }
        
    }
    //setSceenSteadyLight
    else if([@"setScreenSteadyLight" isEqualToString:call.method]){
        NSString* state=(NSString*)call.arguments[@"state"];
        if([state isEqualToString:@"1"]){
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        }else{
            [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
        }
        result(@"1");
    }
    //shake
    else  if([@"shake" isEqualToString:call.method]){
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        result(@"1");
    }
    //cookie
    else  if([@"addManagerCookie" isEqualToString:call.method]){
        NSString* cookie=(NSString*)call.arguments[@"cookie"];
        //URL
        NSString* url=(NSString*)call.arguments[@"url"];
        //array
        NSArray *aArray = [cookie componentsSeparatedByString:@"="];
        //alloc
        NSDictionary* properties = [[NSMutableDictionary alloc] init];
        [properties setValue:aArray[1] forKey:NSHTTPCookieValue];
        [properties setValue:aArray[0] forKey:NSHTTPCookieName];
        [properties setValue:[[NSURL URLWithString:url] host] forKey:NSHTTPCookieDomain];
        [properties setValue:@"/" forKey:NSHTTPCookiePath];
        [properties setValue:@"0" forKey:NSHTTPCookieVersion];
        [properties setValue:[NSDate dateWithTimeIntervalSinceNow:3600*24*30*12] forKey:NSHTTPCookieExpires];
        NSHTTPCookie* cook = [[NSHTTPCookie alloc] initWithProperties:properties];
        [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookie:cook];
        result(@"1");
    }
    else if([@"share" isEqualToString:call.method]){
        NSString* share=(NSString*)call.arguments[@"share"];
        NSArray * activityItems = [[NSArray alloc] initWithObjects:share, nil];
        UIActivityViewController * activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
        UIActivityViewControllerCompletionWithItemsHandler myBlock = ^(UIActivityType activityType, BOOL completed, NSArray *returnedItems, NSError *activityError) {
            NSLog(@"%@",activityType);
            if (completed) {
                NSLog(@"Share success");
            } else {
                NSLog(@"Share failure");
            }
            [activityVC dismissViewControllerAnimated:YES completion:nil];
        };
        activityVC.completionWithItemsHandler = myBlock;
        UIViewController *topController = [self _topViewController:[[UIApplication sharedApplication].keyWindow rootViewController]];
        
        [topController presentViewController:activityVC animated:YES completion:nil];
        
        result(@"true");
    }
    else if([@"jumpToUrl" isEqualToString:call.method]){
        NSString* url=(NSString*)call.arguments[@"url"];
        [self openScheme:url];
        result(@"true");
    }
    else if([@"jumpToScheme" isEqualToString:call.method]){
        NSString* url=(NSString*)call.arguments[@"url"];
        [self openScheme:url];
        result(@"true");
    }
    //is map install
    else if([@"isMapInstalled" isEqualToString:call.method]){
        //type
        NSInteger type=((NSString*)call.arguments[@"type"]).intValue;
        //apple
        if(type==0){
            result(@"1");
        }
        //amap
        else if(type==1){
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"iosamap://"]]) {
                result(@"1");
            }else{
                result(@"0");
            }
        }
        //baidu
        else if(type==2){
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"baidumap://"]]) {
                result(@"1");
            }else{
                result(@"0");
            }
        }
        //tencent
        else if(type==3){
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"qqmap://"]]) {
                result(@"1");
            }else{
                result(@"0");
            }
        }else{
            result(@"0");
        }
    }
    //jump to map
    else if([@"jumpToMap" isEqualToString:call.method]){
        //map type
        NSInteger type=((NSString*)call.arguments[@"type"]).intValue;
        NSString* lat=(NSString*)call.arguments[@"lat"];
        NSString* lng=(NSString*)call.arguments[@"lng"];
        NSString* title=(NSString*)call.arguments[@"title"];
        //apple
        if(type==0){
            CLLocationCoordinate2D loc = CLLocationCoordinate2DMake([lat floatValue],
                                                                    [lng floatValue]);
            MKMapItem *currentLocation = [MKMapItem mapItemForCurrentLocation];
            MKMapItem *toLocation = [[MKMapItem alloc] initWithPlacemark:[[MKPlacemark alloc] initWithCoordinate:loc addressDictionary:nil]];
            [MKMapItem openMapsWithItems:@[currentLocation, toLocation]
                           launchOptions:@{MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving,
                                           MKLaunchOptionsShowsTrafficKey: [NSNumber numberWithBool:YES]}];
            result(@"1");
        }
        //amap
        else if(type==1){
            // check amap
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"iosamap://"]]) {
                
                NSString *urlString = [[NSString stringWithFormat:@"iosamap://navi?sourceApplication=%@&poiname=%@&backScheme=%@&lat=%f&lon=%f&dev=0&style=2",
                                        @"Navigation",
                                        title,
                                        @"",
                                        [lat floatValue],
                                        [lng floatValue]] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
                if (@available(iOS 10.0, *)) {
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString] options:@{} completionHandler:nil];
                }else{
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
#pragma clang diagnostic pop
                }
                result(@"1");
            }else{
                result(@"0");
            }
        }
        //baidumap
        else if(type==2){
            // check baidumap
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"baidumap://"]]) {
                
                NSString *urlString = [[NSString stringWithFormat:@"baidumap://map/direction?origin={{Location}}&destination=latlng:%f,%f|name=%@&mode=driving&coord_type=gcj02",[lat floatValue],[lng floatValue],title] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
                if (@available(iOS 10.0, *)) {
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString] options:@{} completionHandler:nil];
                }else{
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
#pragma clang diagnostic pop
                }
                result(@"1");
            }else{
                result(@"0");
            }
        }
        //tencent map
        else if(type==3){
            // tencent map
            if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"qqmap://"]]) {
                NSString *urlString = [[NSString stringWithFormat:@"qqmap://map/routeplan?type=drive&fromcoord=CurrentLocation&tocoord=%@,%@&referer=IXHBZ-QIZE4-ZQ6UP-DJYEO-HC2K2-EZBXJ",
                                        lat,
                                        lng] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
                if (@available(iOS 10.0, *)) {
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString] options:@{} completionHandler:nil];
                }else{
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
#pragma clang diagnostic pop
                }
                result(@"1");
            }else{
                result(@"0");
            }
        }else{
            result(@"0");
        }
    }
    else {
        result(FlutterMethodNotImplemented);
    }
}



//goto scheme
- (void)openScheme:(NSString *)scheme {
    NSString* encodedString =  [scheme stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    UIApplication *application = [UIApplication sharedApplication];
    NSURL *URL = [NSURL URLWithString:encodedString];
    if (@available(iOS 10.0, *)) {
        [application openURL:URL options:@{}
           completionHandler:^(BOOL success) {
            NSLog(@"Open %@: %d",scheme,success);
        }];
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
        [application openURL:URL];
#pragma clang diagnostic pop
    }
}

//get top controller
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

//caculateSize
-(long long)caculateSize:(NSString*) filePath{
    if([self isDirectory:filePath]){
        return [self folderSizeAtPath:filePath];
    }else{
        return [self fileSizeAtPath:filePath];
    }
}

//check isDirectory
- (BOOL)isDirectory:(NSString *)filePath
{
    BOOL isDirectory = NO;
    [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDirectory];
    return isDirectory;
}

//path size
- (long long) fileSizeAtPath:(NSString*) filePath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if ([manager fileExistsAtPath:filePath]){
        return [[manager attributesOfItemAtPath:filePath error:nil] fileSize];
    }
    return 0;
}

//folder size
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
