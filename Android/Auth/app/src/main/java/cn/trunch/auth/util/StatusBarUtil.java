package cn.trunch.auth.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.trunch.auth.R;


public class StatusBarUtil {

    /**
     * 設置狀態欄為透明
     * @param activity
     */
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 修改狀態欄顏色，支持4.4以上版本
     * @param activity
     * @param colorId
     */
    public static void setStatusBarColor(Activity activity, int colorId) {

        //Android6.0（API 23）以上，系統方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(activity.getResources().getColor(colorId));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTint庫使4.4版本狀態欄變色，需要先將狀態欄設置為透明
            setTranslucentStatus(activity);
            //設置狀態欄顏色
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(colorId);
        }
    }

    /**
     * 設置狀態欄模式
     * @param activity
     * @param isTextDark 文字、圖標是否為黑色 （false為默認的白色）
     * @param colorId 狀態欄顏色
     * @return
     */
    public static void setStatusBarMode(Activity activity, boolean isTextDark, int colorId) {
        if(!isTextDark) {
            //文字、圖標顏色不變，只修改狀態欄顏色
            setStatusBarColor(activity, colorId);
        } else {
            //修改狀態欄顏色和文字圖標顏色
            setStatusBarColor(activity, colorId);
            //4.4以上才可以改文字圖標顏色
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(OSUtil.isMIUI()) {
                    //小米MIUI系統
                    setMIUIStatusBarTextMode(activity, isTextDark);
                } else if(OSUtil.isFlyme()) {
                    //魅族flyme系統
                    setFlymeStatusBarTextMode(activity, isTextDark);
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //6.0以上，調用系統方法
                    Window window = activity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    setStatusBarColor(activity, R.color.colorPrimaryDark);
                }
            }
        }
    }

    /**
     * 設置Flyme系統狀態欄的文字圖標顏色
     * @param activity
     * @param isDark 狀態欄文字及圖標是否為深色
     * @return
     */
    public static boolean setFlymeStatusBarTextMode(Activity activity, boolean isDark) {
        Window window = activity.getWindow();
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 設置MIUI系統狀態欄的文字圖標顏色（MIUIV6以上）
     * @param activity
     * @param isDark 狀態欄文字及圖標是否為深色
     * @return
     */
    public static boolean setMIUIStatusBarTextMode(Activity activity, boolean isDark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (isDark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//狀態欄透明且黑色字體
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字體
                }
                result = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //開發版 7.7.13 及以後版本採用了系統API，舊方法無效但不會報錯，所以兩個方式都要加上
                    if (isDark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View
                                .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                                .SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View
                                .SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            } catch (Exception e) {

            }
        }
        return result;
    }
}
