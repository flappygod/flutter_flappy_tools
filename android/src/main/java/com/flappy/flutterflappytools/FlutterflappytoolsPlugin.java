package com.flappy.flutterflappytools;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import org.json.JSONObject;

public class FlutterflappytoolsPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    private static final int REQUEST_PERMISSION_CODE = 1;

    private Context context;
    private Activity activity;
    private ActivityPluginBinding activityPluginBinding;
    private PermissionListener permissionListener;
    private MethodChannel channel;
    private long mExitTime;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutterflappytools");
        channel.setMethodCallHandler(this);
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
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    private void removeBinding() {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = null;
        activityPluginBinding = null;
    }

    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutterflappytools");
        FlutterflappytoolsPlugin plugin = new FlutterflappytoolsPlugin();
        plugin.context = registrar.activity();
        plugin.activity = registrar.activity();
        plugin.channel = channel;
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final MethodChannel.Result result) {
        if (activity == null) {
            result.success("0");
            return;
        }

        switch (call.method) {
            case "getPathSize":
                handleGetPathSize(call, result);
                break;
            case "clearPath":
                handleClearPath(call, result);
                break;
            case "getBrightness":
                handleGetBrightness(result);
                break;
            case "getDeviceLocal":
                handleGetDeviceLocal(result);
                break;
            case "checkPermission":
                handleCheckPermission(call, result);
                break;
            case "setBrightness":
                handleSetBrightness(call, result);
                break;
            case "getBatteryLevel":
                handleGetBatteryLevel(result);
                break;
            case "getBatteryCharge":
                handleGetBatteryCharge(result);
                break;
            case "setScreenSteadyLight":
                handleSetScreenSteadyLight(call, result);
                break;
            case "shake":
                handleShake(result);
                break;
            case "share":
                handleShare(call, result);
                break;
            case "jumpToUrl":
                handleJumpToUrl(call, result);
                break;
            case "jumpToScheme":
                handleJumpToScheme(call, result);
                break;
            case "jumpToIntent":
                handleJumpToIntent(call, result);
                break;
            case "addManagerCookie":
                handleAddManagerCookie(call, result);
                break;
            case "goHome":
                handleGoHome(call, result);
                break;
            case "isMapInstalled":
                handleIsMapInstalled(call, result);
                break;
            case "jumpToMap":
                handleJumpToMap(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /**
     * Handles the getPathSize method call.
     */
    private void handleGetPathSize(MethodCall call, MethodChannel.Result result) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message message) {
                result.success(String.valueOf(message.obj));
            }
        };
        new Thread(() -> {
            String path = call.argument("path");
            int type = call.argument("type");
            double ret = FileSizeUtil.getFileOrFilesSize(path, type);
            Message msg = handler.obtainMessage(1, ret);
            handler.sendMessage(msg);
        }).start();
    }

    /**
     * Handles the clearPath method call.
     */
    private void handleClearPath(MethodCall call, MethodChannel.Result result) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message message) {
                result.success("1");
            }
        };
        new Thread(() -> {
            final String path = call.argument("path");
            assert path != null;
            CreateDirTool.deleteFile(new File(path));
            Message msg = handler.obtainMessage(1);
            handler.sendMessage(msg);
        }).start();
    }

    /**
     * Handles the getBrightness method call.
     */
    private void handleGetBrightness(MethodChannel.Result result) {
        int bright = getScreenBrightness(activity);
        double brightLess = bright * 1.0 / 255;
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(brightLess);
        result.success(str);
    }

    /**
     * Handles the getDeviceLocal method call.
     */
    private void handleGetDeviceLocal(MethodChannel.Result result) {
        Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Resources.getSystem().getConfiguration().getLocales().isEmpty()
                ? Resources.getSystem().getConfiguration().getLocales().get(0)
                : Resources.getSystem().getConfiguration().locale;
        result.success(getLocaleTag(locale));
    }

    /**
     * Handles the checkPermission method call.
     */
    private void handleCheckPermission(MethodCall call, MethodChannel.Result result) {
        int type = Integer.parseInt(Objects.requireNonNull(call.argument("type")));
        List<String> permission = new ArrayList<>();
        switch (type) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permission.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                }
                break;
            case 1:
                permission.add(Manifest.permission.CAMERA);
                break;
            case 2:
                permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case 3:
                permission.add(Manifest.permission.WRITE_CALENDAR);
                permission.add(Manifest.permission.READ_CALENDAR);
                break;
        }
        if (!permission.isEmpty()) {
            checkPermission(permission, flag -> result.success(flag ? "1" : "0"));
        }
    }

    /**
     * Handles the setBrightness method call.
     */
    private void handleSetBrightness(MethodCall call, MethodChannel.Result result) {
        String brightness = call.argument("brightness");
        assert brightness != null;
        double fla = Double.parseDouble(brightness);
        changeAppBrightness(activity, (int) (255 * fla));
        result.success("1");
    }

    /**
     * Handles the getBatteryLevel method call.
     */
    private void handleGetBatteryLevel(MethodChannel.Result result) {
        int level;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(context.getApplicationContext()).registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            assert intent != null;
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
        result.success(Double.toString(level * 1.0 / 100));
    }

    /**
     * Handles the getBatteryCharge method call.
     */
    private void handleGetBatteryCharge(MethodChannel.Result result) {
        Intent intent = new ContextWrapper(context.getApplicationContext()).registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert intent != null;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        result.success(status == BatteryManager.BATTERY_STATUS_CHARGING ? "1" : "0");
    }

    /**
     * Handles the setScreenSteadyLight method call.
     */
    private void handleSetScreenSteadyLight(MethodCall call, MethodChannel.Result result) {
        String state = call.argument("state");
        assert state != null;
        if (state.equals("1")) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        result.success("1");
    }

    /**
     * Handles the shake method call.
     */
    private void handleShake(MethodChannel.Result result) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        mVibrator.vibrate(1000);
        mVibrator.cancel();
        result.success("1");
    }

    /**
     * Handles the share method call.
     */
    private void handleShare(MethodCall call, MethodChannel.Result result) {
        String share = call.argument("share");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, share);
        shareIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(shareIntent, "Share"));
        result.success("1");
    }

    /**
     * Handles the jumpToUrl method call.
     */
    private void handleJumpToUrl(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (checkApkExist(context, "com.android.browser")) {
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            }
            activity.startActivity(intent);
        } catch (Exception ex) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        }
        result.success("1");
    }

    /**
     * Handles the jumpToScheme method call.
     */
    private void handleJumpToScheme(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
        result.success("1");
    }

    /**
     * Handles the jumpToIntent method call.
     */
    private void handleJumpToIntent(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        Uri uri = Uri.parse(url);
        try {
            Intent intentOne = new Intent(Intent.ACTION_VIEW, uri);
            PackageManager packageManager = activity.getPackageManager();
            if (intentOne.resolveActivity(packageManager) != null) {
                activity.startActivity(intentOne);
                result.success("{}");
                return;
            }

            Intent intentTwo = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if (intentTwo.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intentTwo);
                result.success("{}");
                return;
            }

            String fallbackUrl = intentTwo.getStringExtra("browser_fallback_url");
            if (fallbackUrl != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("browser_fallback_url", fallbackUrl);
                result.success(jsonObject.toString());
                return;
            }

            Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + intentTwo.getPackage()));
            if (marketIntent.resolveActivity(packageManager) != null) {
                activity.startActivity(marketIntent);
                result.success("{}");
            }
        } catch (Exception e) {
            result.success("{}");
        }
    }

    /**
     * Handles the addManagerCookie method call.
     */
    private void handleAddManagerCookie(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        String cookie = call.argument("cookie");
        new Thread(() -> {
            addCookie(url, cookie);
            new Handler(Looper.getMainLooper()).post(() -> result.success("1"));
        }).start();
    }

    /**
     * Handles the goHome method call.
     */
    private void handleGoHome(MethodCall call, MethodChannel.Result result) {
        String toast = call.argument("toast");
        goHome(toast);
        result.success("1");
    }

    /**
     * Handles the isMapInstalled method call.
     */
    private void handleIsMapInstalled(MethodCall call, MethodChannel.Result result) {
        int type = Integer.parseInt((String) Objects.requireNonNull(call.argument("type")));
        String packageName = null;
        switch (type) {
            case 1:
                packageName = "com.autonavi.minimap";
                break;
            case 2:
                packageName = "com.baidu.BaiduMap";
                break;
            case 3:
                packageName = "com.tencent.map";
                break;
        }
        result.success(packageName != null && checkApkExist(context, packageName) ? "1" : "0");
    }

    /**
     * Handles the jumpToMap method call.
     */
    private void handleJumpToMap(MethodCall call, MethodChannel.Result result) {
        String lat = call.argument("lat");
        String lng = call.argument("lng");
        String title = call.argument("title");
        int type = Integer.parseInt((String) Objects.requireNonNull(call.argument("type")));
        switch (type) {
            case 1:
                if (checkApkExist(context, "com.autonavi.minimap")) {
                    invokeAuToNaveMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
                break;
            case 2:
                if (checkApkExist(context, "com.baidu.BaiduMap")) {
                    invokeBaiDuMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
                break;
            case 3:
                if (checkApkExist(context, "com.tencent.map")) {
                    invokeQQMap(context, lat, lng, title);
                    result.success("1");
                } else {
                    result.success("0");
                }
                break;
            default:
                result.success("0");
                break;
        }
    }

    /**
     * Checks if the specified APK exists.
     */
    private boolean checkApkExist(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            List<PackageInfo> info = context.getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : info) {
                if (packageInfo.packageName.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Gets the locale tag for the specified locale.
     */
    private String getLocaleTag(Locale locale) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? locale.toLanguageTag() : locale.toString();
    }

    /**
     * Invokes the Baidu Map application.
     */
    private static void invokeBaiDuMap(Context context, String lat, String lng, String title) {
        try {
            Uri uri = Uri.parse("baidumap://map/geocoder?location=" + lat + "," + lng + "&name=" + title + "&coord_type=gcj02");
            Intent intent = new Intent();
            intent.setPackage("com.baidu.BaiduMap");
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Invokes the AutoNavi Map application.
     */
    private static void invokeAuToNaveMap(Context context, String lat, String lng, String title) {
        try {
            String stringBuffer = "androidamap://navi?sourceApplication=Location&poiname=" + title + "&lat=" + lat + "&lon=" + lng + "&dev=0&style=2";
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(stringBuffer));
            intent.setPackage("com.autonavi.minimap");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Invokes the QQ Map application.
     */
    private static void invokeQQMap(Context context, String lat, String lng, String title) {
        try {
            Uri uri = Uri.parse("qqmap://map/routeplan?type=drive&to=" + title + "&tocoord=" + lat + "," + lng + "&referer={Location}");
            Intent intent = new Intent();
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Changes the application's brightness.
     */
    public void changeAppBrightness(Activity activity, int brightness) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness == -1 ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE : (brightness <= 0 ? 1 : brightness) / 255f;
        window.setAttributes(lp);
    }

    /**
     * Gets the screen brightness.
     */
    public static int getScreenBrightness(Activity activity) {
        if (activity == null) {
            return 0;
        }
        try {
            ContentResolver cr = activity.getContentResolver();
            return Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    /**
     * Adds a cookie to the CookieManager.
     *
     * @param url    The URL to which the cookie should be added.
     * @param cookie The cookie to be added.
     */
    private void addCookie(String url, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, cookie);
        cookieManager.setCookie(url, "Domain=" + url);
        cookieManager.setCookie(url, "Path=/");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }

    /**
     * Navigates the user to the home screen, showing a toast message if specified.
     *
     * @param toast The toast message to be shown.
     */
    private void goHome(String toast) {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            if (toast != null && !toast.isEmpty()) {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
            mExitTime = System.currentTimeMillis();
        } else {
            Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
            mHomeIntent.addCategory(Intent.CATEGORY_HOME);
            mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(mHomeIntent);
        }
    }

    /**
     * Checks if the specified permission is granted. If not, requests the permission.
     *
     * @param permissions The permission to check.
     * @param listener    The listener to notify the result.
     */
    private void checkPermission(List<String> permissions, PermissionListener listener) {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.isEmpty()) {
            listener.result(true);
        } else {
            permissionListener = listener;
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionListener != null) {
                permissionListener.result(granted);
                permissionListener = null;
            }
            return true;
        }
        return false;
    }

    /**
     * Interface for permission result callback.
     */
    private interface PermissionListener {
        void result(boolean flag);
    }
}