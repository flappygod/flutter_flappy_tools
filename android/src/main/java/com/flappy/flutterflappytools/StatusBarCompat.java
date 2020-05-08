package com.flappy.flutterflappytools;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;

/*************************
 * @author lijunlin
 */
public class StatusBarCompat {

    //class的名称
    public static final String TAG = StatusBarCompat.class.getName();


    /******************
     * 设置导航栏颜色
     *
     * @param activity         activity
     * @param statusColor      颜色值
     * @param fitSystemWindows 是否适应系统window变化
     */
    public static void setStatusBarColor(Activity activity, int statusColor, boolean fitSystemWindows) {
        if (fitSystemWindows) {
            //设置系统导航栏颜色，而且要求适配系统
            setStatusBarColorPurity(activity, statusColor);
        } else {
            //设置系统导航栏颜色，仅仅是透明处理
            setStatusBarColorTranslucent(activity, statusColor);
        }
    }


    /***************
     * 设置导航栏颜色，纯色处理
     * @param activity      activity
     * @param statusColor  颜色
     */
    public static void setStatusBarColorPurity(Activity activity, int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusColor);
            //设置顶部
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, true);
            }
        }
    }


    /********************
     * 设置导航栏颜色，兼容4.4的系统版本，但不兼容系统对软键盘的处理
     *
     * @param activity    activity
     * @param statusColor 颜色值
     */
    public static void setStatusBarColorTranslucent(Activity activity, int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //获取window
            Window window = activity.getWindow();
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup mContentParent = (ViewGroup) mContentView.getParent();

            View statusBarView = mContentParent.getChildAt(0);
            //假如statusBarView已经存在了就不用多做处理
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
                statusBarView.setBackgroundColor(statusColor);
            } else {
                //如果不存在就添加这个View到mContentParent里面
                statusBarView = new View(activity);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity));
                statusBarView.setBackgroundColor(statusColor);
                mContentParent.addView(statusBarView, 0, lp);
            }
            //设置铺满
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        }
    }


    /***************
     * 对这个Activity的状态栏进行透明操作
     * 不兼容系统对于软键盘的处理
     *
     * @param activity activity
     */
    public static void translucentStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);

            /*//进行透明操作
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/

            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            //设置直接订满屏幕
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        } else {
            //进行透明操作
            Window window = activity.getWindow();
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //移除已经存在的statusBarView
            ViewGroup mContentParent = (ViewGroup) mContentView.getParent();
            View statusBarView = mContentParent.getChildAt(0);
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
                mContentParent.removeView(statusBarView);
            }
            if (mContentParent.getChildAt(0) != null) {
                ViewCompat.setFitsSystemWindows(mContentParent.getChildAt(0), false);
            }
        }
    }


    /***************
     * 对这个Activity的状态栏进行透明操作
     * @param activity activity
     */
    public static void translucentStatusBarTran(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //进行透明操作
            Window window = activity.getWindow();
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置直接订满屏幕
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        } else {
            //进行透明操作
            Window window = activity.getWindow();
            ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //移除已经存在的statusBarView
            ViewGroup mContentParent = (ViewGroup) mContentView.getParent();
            View statusBarView = mContentParent.getChildAt(0);
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
                mContentParent.removeView(statusBarView);
            }
            if (mContentParent.getChildAt(0) != null) {
                ViewCompat.setFitsSystemWindows(mContentParent.getChildAt(0), false);
            }
        }
    }


    /*********************
     * 获取状态栏高度
     *
     * @param context 上下文
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        try {
            int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                result = context.getResources().getDimensionPixelOffset(resId);
            }
        }catch (Exception e){

        }
        return result;
    }
}
