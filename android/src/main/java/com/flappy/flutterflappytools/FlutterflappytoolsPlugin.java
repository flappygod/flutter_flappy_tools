package com.flappy.flutterflappytools;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.text.DecimalFormat;

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

    //上下文
    private Context context;

    //当前activity
    private Activity activity;

    //退出时的时间
    private long mExitTime;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutterflappytools");
        this.context = flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        context = null;
        activity = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        context = binding.getActivity().getApplicationContext();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        context = binding.getActivity().getApplicationContext();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
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
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {

        //如果没有activity存在，默认返回空的
        if (activity == null) {
            result.success("0");
            return;
        }

        //获取路径的大小
        if (call.method.equals("getPathSize")) {
            //创建handler,使用多线程防止卡死
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    //返回
                    result.success(String.valueOf(message.obj));
                }
            };
            new Thread() {
                public void run() {
                    //最大的长度
                    String path = call.argument("path");
                    //类型
                    int type = call.argument("type");
                    //大小
                    double ret = FileSizeUtil.getFileOrFilesSize(path, type);
                    //消息
                    Message msg = handler.obtainMessage(1, ret);
                    //发送
                    handler.sendMessage(msg);

                }
            }.start();
        }
        //清空缓存
        else if (call.method.equals("clearPath")) {

            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    result.success("1");
                }
            };

            new Thread() {
                public void run() {
                    //获取缓存
                    final String path = call.argument("path");
                    //删除整个文件夹
                    CreateDirTool.deleteFile(new File(path));
                    //消息
                    Message msg = handler.obtainMessage(1);
                    //发送
                    handler.sendMessage(msg);
                }
            }.start();
        }
        //获取亮度
        else if (call.method.equals("getBrightness")) {
            //获取当前的屏幕亮度
            int bright = getScreenBrightness(activity);
            //转换
            double brightLess = bright * 1.0 / 255;
            //保留两位小数
            DecimalFormat df = new DecimalFormat("#.00");
            //保留两位小数
            String str = df.format(brightLess);
            //除开
            result.success(str);
        }
        //设置亮度
        else if (call.method.equals("setBrightness")) {
            //亮度
            String brightness = call.argument("brightness");
            //转换为
            double fla = Double.parseDouble(brightness);
            //修改亮度
            changeAppBrightness(activity, (int) (255 * fla));
            //成功
            result.success("1");
        }
        //获取当前的电量
        else if (call.method.equals("getBatteryLevel")) {
            //当前的电量数据
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
        //判断当前是否正在充电
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
        //设置屏幕常亮
        else if (call.method.equals("setSceenSteadyLight")) {
            String state = call.argument("state");
            if (state.equals("1")) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            result.success("1");
        }
        //设置顶部状态栏显示与隐藏
        else if (call.method.equals("setStatusBarShow")) {
            //判断当前是否使用代理
            String show = call.argument("show");
            //显示
            if (show.equals("1")) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            result.success("1");
        }
        //调用系统分享
        else if (call.method.equals("setStatusBarColor")) {
            //获取颜色
            String color = call.argument("color");
            //颜色
            long intColor = Long.parseLong(color);
            //设置颜色
            StatusBarTool.setActivityBarColor(activity, (int) intColor);
            //返回成功
            result.success("1");
        }
        //设置沉浸式状态栏半透明
        else if (call.method.equals("transStatusBar")) {
            //获取颜色
            StatusBarTool.translucentActivity(activity);
            //成功
            result.success("1");
        }
        //震动
        else if (call.method.equals("shake")) {
            //激励震动
            Vibrator mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            //震动时间
            mVibrator.vibrate(1000);
            //取消震动
            mVibrator.cancel();
            //成功
            result.success("1");
        }
        //调用系统分享
        else if (call.method.equals("share")) {
            //切记需要使用Intent.createChooser，否则会出现别样的应用选择框，您可以试试
            String share = call.argument("share");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, share);
            shareIntent.setType("text/plain");
            shareIntent = Intent.createChooser(shareIntent, "分享");
            activity.startActivity(shareIntent);
            result.success("1");
        }
        //前往主页
        else if (call.method.equals("goHome")) {
            //前往主页
            goHome();
            //成功
            result.success("1");
        } else {
            //没有实现
            result.notImplemented();
        }
    }

    //修改屏幕亮度
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

    //获取屏幕的亮度
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

    /*******************
     * 返回主界面
     */
    private void goHome() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show();
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
