package com.flappy.flutter_flappy_tools;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.IntentFilter;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

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

/**
 * FlutterFlappyToolsPlugin
 */
public class FlutterFlappyToolsPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    /// 用于 Flutter 和原生 Android 之间通信的 MethodChannel
    ///
    /// 该引用用于将插件注册到 Flutter 引擎，并在 Flutter 引擎从 Activity 分离时取消注册
    private MethodChannel channel;

    // 上下文对象，表示应用程序的环境信息
    private Context context;

    // 当前 Activity 的引用
    private Activity activity;

    // Activity 插件绑定对象，用于管理 Activity 生命周期
    private ActivityPluginBinding activityPluginBinding;

    // 用于记录退出时间的变量
    private long mExitTime;

    /**
     * 当插件附加到 Flutter 引擎时调用
     *
     * @param flutterPluginBinding 提供与 Flutter 引擎交互的接口
     */
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_flappy_tools");
        channel.setMethodCallHandler(this);
    }

    /**
     * 当插件从 Flutter 引擎分离时调用
     *
     * @param binding 提供与 Flutter 引擎交互的接口
     */
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        context = null;
        activity = null;
    }

    /**
     * 当插件附加到 Activity 时调用
     *
     * @param binding 提供与 Activity 交互的接口
     */
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    /**
     * 当 Activity 配置发生变化时重新附加插件
     *
     * @param binding 提供与 Activity 交互的接口
     */
    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    /**
     * 当 Activity 配置发生变化时分离插件
     */
    @Override
    public void onDetachedFromActivityForConfigChanges() {
        removeBinding();
    }

    /**
     * 当插件从 Activity 分离时调用
     */
    @Override
    public void onDetachedFromActivity() {
        removeBinding();
    }

    /**
     * 添加 Activity 绑定
     *
     * @param binding 提供与 Activity 交互的接口
     */
    private void addBinding(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        activityPluginBinding = binding;
    }

    /**
     * 移除 Activity 绑定
     */
    private void removeBinding() {
        activity = null;
        activityPluginBinding = null;
    }

    /**
     * 处理来自 Flutter 的方法调用
     *
     * @param call   包含方法名和参数的对象
     * @param result 用于返回结果给 Flutter 的对象
     */
    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final MethodChannel.Result result) {
        if (activity == null) {
            result.success("0");
            return;
        }

        // 根据方法名调用对应的处理方法
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
            default:
                result.notImplemented();
                break;
        }
    }

    /**
     * 处理获取路径大小的方法调用
     *
     * @param call   包含路径和类型参数
     * @param result 返回路径大小结果
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
     * 处理清除路径的方法调用
     *
     * @param call   包含路径参数
     * @param result 返回清除结果
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
     * 处理获取屏幕亮度的方法调用
     *
     * @param result 返回屏幕亮度结果
     */
    private void handleGetBrightness(MethodChannel.Result result) {
        int bright = getScreenBrightness(activity);
        double brightLess = bright * 1.0 / 255;
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(brightLess);
        result.success(str);
    }

    /**
     * 处理获取设备语言的方法调用
     *
     * @param result 返回设备语言结果
     */
    private void handleGetDeviceLocal(MethodChannel.Result result) {
        Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Resources.getSystem().getConfiguration().getLocales().isEmpty()
                ? Resources.getSystem().getConfiguration().getLocales().get(0)
                : Resources.getSystem().getConfiguration().locale;
        result.success(getLocaleTag(locale));
    }

    /**
     * 处理设置屏幕亮度的方法调用
     *
     * @param call   包含亮度参数
     * @param result 返回设置结果
     */
    private void handleSetBrightness(MethodCall call, MethodChannel.Result result) {
        String brightness = call.argument("brightness");
        assert brightness != null;
        double fla = Double.parseDouble(brightness);
        changeAppBrightness(activity, (int) (255 * fla));
        result.success("1");
    }

    /**
     * 处理获取电池电量的方法调用
     *
     * @param result 返回电池电量结果
     */
    private void handleGetBatteryLevel(MethodChannel.Result result) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        result.success(Double.toString(level * 1.0 / 100));
    }

    /**
     * 处理获取电池充电状态的方法调用
     *
     * @param result 返回充电状态结果
     */
    private void handleGetBatteryCharge(MethodChannel.Result result) {
        Intent intent = new ContextWrapper(context.getApplicationContext()).registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert intent != null;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        result.success(status == BatteryManager.BATTERY_STATUS_CHARGING ? "1" : "0");
    }

    /**
     * 处理设置屏幕常亮的方法调用
     *
     * @param call   包含状态参数
     * @param result 返回设置结果
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
     * 处理设备震动的方法调用
     *
     * @param result 返回震动结果
     */
    private void handleShake(MethodChannel.Result result) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        mVibrator.vibrate(1000);
        mVibrator.cancel();
        result.success("1");
    }

    /**
     * 处理分享内容的方法调用
     *
     * @param call   包含分享内容参数
     * @param result 返回分享结果
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
     * 处理跳转到 URL 的方法调用
     *
     * @param call   包含 URL 参数
     * @param result 返回跳转结果
     */
    private void handleJumpToUrl(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (checkApkExist(context)) {
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
     * 处理跳转到 Scheme 的方法调用
     *
     * @param call   包含 Scheme 参数
     * @param result 返回跳转结果
     */
    private void handleJumpToScheme(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
        result.success("1");
    }

    /**
     * 处理跳转到 Intent 的方法调用
     *
     * @param call   包含 Intent 参数
     * @param result 返回跳转结果
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
     * 处理添加 Cookie 的方法调用
     *
     * @param call   包含 URL 和 Cookie 参数
     * @param result 返回添加结果
     */
    private void handleAddManagerCookie(MethodCall call, MethodChannel.Result result) {
        String url = call.argument("url");
        String cookie = call.argument("cookie");
        new Thread(() -> {
            try {
                addCookie(url, cookie);
                new Handler(Looper.getMainLooper()).post(() -> result.success("1"));
            } catch (Exception ex) {
                new Handler(Looper.getMainLooper()).post(() -> result.success("0"));
            }
        }).start();
    }

    /**
     * 处理返回到主屏幕的方法调用
     *
     * @param call   包含 Toast 参数
     * @param result 返回操作结果
     */
    private void handleGoHome(MethodCall call, MethodChannel.Result result) {
        String toast = call.argument("toast");
        goHome(toast);
        result.success("1");
    }

    /**
     * 检查指定的 APK 是否存在
     *
     * @param context 上下文对象
     * @return 是否存在
     */
    private boolean checkApkExist(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.android.browser", PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            List<PackageInfo> info = context.getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : info) {
                if (packageInfo.packageName.equalsIgnoreCase("com.android.browser")) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 获取指定 Locale 的语言标签
     *
     * @param locale Locale 对象
     * @return 语言标签
     */
    private String getLocaleTag(Locale locale) {
        return locale.toLanguageTag();
    }

    /**
     * 更改应用的屏幕亮度
     *
     * @param activity   当前 Activity
     * @param brightness 亮度值
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
     * 获取屏幕亮度
     *
     * @param activity 当前 Activity
     * @return 屏幕亮度值
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
     * 向 CookieManager 添加 Cookie
     *
     * @param url    URL
     * @param cookie Cookie 值
     */
    private void addCookie(String url, String cookie) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, cookie);
        cookieManager.setCookie(url, "Domain=" + url);
        cookieManager.setCookie(url, "Path=/");
        cookieManager.flush();
    }

    /**
     * 返回到主屏幕，并显示 Toast 提示（如果有）
     *
     * @param toast Toast 提示内容
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
}