
package com.example.kaixuan.internship3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.example.kaixuan.internship3.floatwindow.FloatWindow;
import com.example.kaixuan.internship3.floatwindow.MoveType;
import com.example.kaixuan.internship3.floatwindow.PermissionListener;
import com.example.kaixuan.internship3.floatwindow.Screen;
import com.example.kaixuan.internship3.permission.FloatWindowManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_apply_permission).setOnClickListener(this);
        findViewById(R.id.btn_open_float_window).setOnClickListener(this);
        findViewById(R.id.btn_close_float_window).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_apply_permission:
                applyPermission();
                break;
            case R.id.btn_open_float_window:
                openFloatWindow();
                break;
            case R.id.btn_close_float_window:
                closeFloatWindow();
                break;
            default:
                break;
        }
    }


    //申请权限
    private void applyPermission() {
        if (FloatWindowManager.getInstance().checkPermission(getApplicationContext())) {
            //已开启权限
            Toast.makeText(this, "已开启权限", Toast.LENGTH_SHORT).show();
        } else {
            //去申请权限,里面有弹窗，不要传ApplicationContext
            FloatWindowManager.getInstance().applyPermission(this);
        }
    }


    //关闭悬浮窗
    private void closeFloatWindow() {
        if (FloatWindow.get() != null) {
            FloatWindow.destroy();
        }
    }


    //开启悬浮窗
    private void openFloatWindow() {
        if (FloatWindowManager.getInstance().checkPermission(getApplicationContext())) {
            if (FloatWindow.get() == null) {
                FloatView floatView = new FloatView(this);
                //建造者模式
                FloatWindow
                        .with(getApplication())
                        .setView(floatView)
                        .setX(Screen.width, 0.5f)
                        .setY(Screen.height, 0.5f)
                        .setMoveType(MoveType.active, 0, 0)
                        .setMoveStyle(500, new BounceInterpolator())
                        .setFilter(true, MainActivity.class)//设置MainActivity.class显示悬浮窗true
                        .setPermissionListener(mPermissionListener)
                        .setDesktopShow(true)
                        .build();
                FloatWindow.get().show();
            }
        } else {
            applyPermission();
        }
    }


    private PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFail() {

        }
    };



}
