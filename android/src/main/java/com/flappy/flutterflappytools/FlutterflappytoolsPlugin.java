package com.flappy.flutterflappytools;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.Context.BATTERY_SERVICE;

/**
 * FlutterflappytoolsPlugin
 */
public class FlutterflappytoolsPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {


    //context
    private Context context;

    //activity
    private Activity activity;

    //binding
    private ActivityPluginBinding activityPluginBinding;

    //channel
    private MethodChannel channel;

    //exit time
    private long mExitTime;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutterflappytools");
        channel.setMethodCallHandler(this);
        this.context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        context = null;
        activity = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onDetachedFromActivity() {
        removeBinding();
    }

    private void addBinding(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        activityPluginBinding = binding;
    }

    private void removeBinding() {
        activity = null;
        activityPluginBinding = null;
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutterflappytools");
        FlutterflappytoolsPlugin plugin = new FlutterflappytoolsPlugin();
        plugin.context = registrar.activity();
        plugin.activity = registrar.activity();
        plugin.channel = channel;
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {

        //return 0
        if (activity == null) {
            result.success("0");
            return;
        }

        //get path size
        if (call.method.equals("getPathSize")) {
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    result.success(String.valueOf(message.obj));
                }
            };
            new Thread() {
                public void run() {
                    String path = call.argument("path");
                    int type = call.argument("type");
                    double ret = FileSizeUtil.getFileOrFilesSize(path, type);
                    Message msg = handler.obtainMessage(1, ret);
                    handler.sendMessage(msg);

                }
            }.start();
        }
        //clear path
        else if (call.method.equals("clearPath")) {
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    result.success("1");
                }
            };
            new Thread() {
                public void run() {
                    final String path = call.argument("path");
                    CreateDirTool.deleteFile(new File(path));
                    Message msg = handler.obtainMessage(1);
                    handler.sendMessage(msg);
                }
            }.start();
        }
        //get Brightness
        else if (call.method.equals("getBrightness")) {
            //get Brightness
            int bright = getScreenBrightness(activity);
            //change
            double brightLess = bright * 1.0 / 255;
            //format
            DecimalFormat df = new DecimalFormat("#.00");
            //format
            String str = df.format(brightLess);
            //success
            result.success(str);
        }
        //set Brightness
        else if (call.method.equals("setBrightness")) {
            //brightness
            String brightness = call.argument("brightness");
            //parse double
            double fla = Double.parseDouble(brightness);
            //change bright ness
            changeAppBrightness(activity, (int) (255 * fla));
            //success
            result.success("1");
        }
        //getBatteryLevel
        else if (call.method.equals("getBatteryLevel")) {
            //level
            int level;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
                level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            } else {
                Intent intent = new ContextWrapper(context.getApplicationContext()).registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,
                        -1) / intent.getIntExtra(BatteryManager.EXTRA_SCALE,
                        -1);
            }
            result.success(Double.toString(level * 1.0 / 100));
        }
        //is Charging or not
        else if (call.method.equals("getBatteryCharge")) {
            Intent intent = new ContextWrapper(context.getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                result.success("1");
            } else {
                result.success("0");
            }
        }
        //setScreenSteadyLight
        else if (call.method.equals("setSceenSteadyLight")) {
            String state = call.argument("state");
            if (state.equals("1")) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            result.success("1");
        }
        //shake
        else if (call.method.equals("shake")) {
            Vibrator mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            mVibrator.vibrate(1000);
            mVibrator.cancel();
            result.success("1");
        }
        //share
        else if (call.method.equals("share")) {
            String share = call.argument("share");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, share);
            shareIntent.setType("text/plain");
            shareIntent = Intent.createChooser(shareIntent, "Share");
            activity.startActivity(shareIntent);
            result.success("1");
        }
        //jump to url
        else if (call.method.equals("jumpToUrl")) {
            String url = call.argument("url");
            //start activity
            try {
                if (isInstalledApp(context, "com.android.browser")) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                    activity.startActivity(intent);
                }
            }
            //has error
            catch (Exception ex) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
            result.success("1");
        }
        //jump to scheme
        else if (call.method.equals("jumpToScheme")) {
            String url = call.argument("url");
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
            result.success("1");
        }
        //add cookie
        else if (call.method.equals("addManagerCookie")) {
            String url = call.argument("url");
            String cookie = call.argument("cookie");
            addCookie(url, cookie);
            result.success("1");
        }
        //got home
        else if (call.method.equals("goHome")) {
            //toast
            String toast = call.argument("toast");
            goHome(toast);
            result.success("1");
        }
        //check map install
        else if (call.method.equals("isMapInstalled")) {
            int type = Integer.parseInt((String) call.argument("type"));
            if (type == 0) {
                result.success("0");
            } else if (type == 1) {
                if (checkApkExist(context, "com.autonavi.minimap")) {
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else if (type == 2) {
                if (checkApkExist(context, "com.baidu.BaiduMap")) {
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else if (type == 3) {
                if (checkApkExist(context, "com.tencent.map")) {
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else {
                result.success("0");
            }
        }
        //jump to map
        else if (call.method.equals("jumpToMap")) {
            String lat = call.argument("lat");
            String lng = call.argument("lng");
            String title = call.argument("title");
            int type = Integer.parseInt((String) call.argument("type"));
            if (type == 0) {
                result.success("0");
            } else if (type == 1) {
                if (checkApkExist(context, "com.autonavi.minimap")) {
                    invokeAuToNaveMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else if (type == 2) {
                if (checkApkExist(context, "com.baidu.BaiduMap")) {
                    invokeBaiDuMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else if (type == 3) {
                if (checkApkExist(context, "com.tencent.map")) {
                    invokeQQMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
            } else {
                result.success("0");
            }
        } else {
            result.notImplemented();
        }
    }

    //check install
    public static boolean isInstalledApp(Context context, String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            final PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName.toLowerCase(Locale.ENGLISH);
                    if (pn.equals(packageName)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    //baidumap
    private static void invokeBaiDuMap(Context context, String lat, String lng, String title) {

        try {
            Uri uri = Uri.parse("baidumap://map/geocoder?" +
                    "location=" + lat + "," + lng +
                    "&name=" + title +
                    "&coord_type=gcj02");
            Intent intent = new Intent();
            intent.setPackage("com.baidu.BaiduMap");
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    //amap
    private static void invokeAuToNaveMap(Context context, String lat, String lng, String title) {
        try {
            StringBuffer stringBuffer = new StringBuffer("androidamap://navi?sourceApplication=")
                    .append("Location");
            stringBuffer.append("&poiname=").append(title);
            stringBuffer.append("&lat=").append(lat)
                    .append("&lon=").append(lng)
                    .append("&dev=").append("0")
                    .append("&style=").append("2");
            Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse(stringBuffer.toString()));
            intent.setPackage("com.autonavi.minimap");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    //tencent map
    private static void invokeQQMap(Context context, String lat, String lng, String title) {
        try {
            Uri uri = Uri.parse("qqmap://map/routeplan?type=drive" +
                    "&to=" + title
                    + "&tocoord=" + lat + "," + lng
                    + "&referer={Location}");
            Intent intent = new Intent();
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    //check exist
    private boolean checkApkExist(Context context, String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //change brightness
    public void changeAppBrightness(Activity activity, int brightness) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }

    //get screen brightness
    public static int getScreenBrightness(Activity activity) {
        if (activity == null) {
            return 0;
        }
        try {
            ContentResolver cr = activity.getContentResolver();
            int value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
            return value;
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    //add cookie
    private void addCookie(String url, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, cookie);
        cookieManager.setCookie(url, "Domain=" + url);
        cookieManager.setCookie(url, "Path=/");
        String cookies = cookieManager.getCookie(url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }

    //go home
    private void goHome(String toast) {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            if (toast != null && !toast.equals("")) {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
            mExitTime = System.currentTimeMillis();
        } else {
            Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
            mHomeIntent.addCategory(Intent.CATEGORY_HOME);
            mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(mHomeIntent);
        }
    }

}
