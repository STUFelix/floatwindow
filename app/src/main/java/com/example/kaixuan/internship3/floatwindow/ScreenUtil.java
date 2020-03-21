package com.example.kaixuan.internship3.floatwindow;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class ScreenUtil {

    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;

    private ScreenUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }



    /**
     * 获取当前屏幕大小：当前是横屏还是竖屏
     *
     * @param context
     * @return
     */
    public static Point getCurScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point;
    }



}