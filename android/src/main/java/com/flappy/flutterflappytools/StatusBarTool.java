package com.flappy.flutterflappytools;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;


/**
 * Created by yang on 2016/7/2.
 */
public class StatusBarTool {


    public static final int COLOR_DEFAULT_WHITE = Color.parseColor("#FFFFFFFF");

    /*********
     * 设置颜色
     *
     * @param activity
     * @param color
     */
    public static void setActivityBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompat.setStatusBarColorPurity(activity, color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompat.setStatusBarColorTranslucent(activity, color);
        }
    }

    /***************
     * 导航栏透明
     *
     * @param activity actviity
     */
    public static void translucentActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompat.translucentStatusBar(activity);
        }
    }


}
