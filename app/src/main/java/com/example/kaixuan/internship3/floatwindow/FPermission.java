package com.example.kaixuan.internship3.floatwindow;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class FPermission {

    private static List<PermissionListener> mPermissionListenerList;

    private static PermissionListener mPermissionListener;

    static synchronized void request(Context context, PermissionListener permissionListener) {
        if (PermissionUtil.hasPermission(context)) {
            permissionListener.onSuccess();
            return;
        }
        if (mPermissionListenerList == null) {
            mPermissionListenerList = new ArrayList<>();
            mPermissionListener = new PermissionListener() {
                @Override
                public void onSuccess() {
                    for (PermissionListener listener : mPermissionListenerList) {
                        listener.onSuccess();
                    }
                    mPermissionListenerList.clear();
                }

                @Override
                public void onFail() {
                    for (PermissionListener listener : mPermissionListenerList) {
                        listener.onFail();
                    }
                    mPermissionListenerList.clear();
                }
            };
        }
        mPermissionListenerList.add(permissionListener);
    }
}
