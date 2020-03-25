
package com.example.kaixuan.internship3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.kaixuan.internship3.floatwindow.FloatWindow;
import com.example.kaixuan.internship3.floatwindow.IFloatWindow;
import com.example.kaixuan.internship3.floatwindow.MoveType;
import com.example.kaixuan.internship3.floatwindow.PermissionListener;
import com.example.kaixuan.internship3.floatwindow.Screen;
import com.example.kaixuan.internship3.floatwindow.ScreenUtil;
import com.example.kaixuan.internship3.permission.FloatWindowManager;
import com.example.kaixuan.internship3.recordingscreen.ScreenRecorder;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean IS_PORTRAIT = true;

    private static boolean IS_OPEN = false;
    private static boolean IS_CLOSE = true;
    private static boolean IS_BACK = false;

    private static final int REQUEST_CODE = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private Button mButton;

//    private  WindowManager mWindowManager;
//    private  WindowManager.LayoutParams mLayoutParams;
    private FloatView floatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_apply_permission).setOnClickListener(this);
        findViewById(R.id.btn_open_float_window).setOnClickListener(this);
        findViewById(R.id.btn_close_float_window).setOnClickListener(this);
        findViewById(R.id.btn_change_orientation).setOnClickListener(this);
        findViewById(R.id.btn_change_view).setOnClickListener(this);

        //录屏
        mButton = (Button) findViewById(R.id.btn_start_recorder);
        mButton.setOnClickListener(this);
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mGetExternalStoragePermission();

        floatView = new FloatView(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_apply_permission:
                applyPermission();
                break;
            case R.id.btn_open_float_window:
                IS_OPEN = true;
                IS_CLOSE = false;
                openFloatWindow(IS_PORTRAIT,IS_OPEN);
                break;
            case R.id.btn_close_float_window:
                IS_CLOSE = true;
                closeFloatWindow(IS_CLOSE);
                break;
            case R.id.btn_change_orientation:
                changeOrientation();
                break;
            case R.id.btn_start_recorder:
                if (mRecorder != null) {
                    mRecorder.quit();
                    mRecorder = null;
                    Toast.makeText(this, "mp4文件存储在"+Environment.getExternalStorageDirectory()+
                            "record-" + 1280 + "x" + 720 + "-" + System.currentTimeMillis() + ".mp4", Toast.LENGTH_SHORT).show();

                    mButton.setText("重新录制");
                } else {
                    Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, REQUEST_CODE);
                }
                break;
                case R.id.btn_change_view:
                    IS_PORTRAIT=false;
                    changeView(IS_BACK);
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

    //开启悬浮窗
    private void openFloatWindow(boolean IS_PORTRAIT,boolean IS_OPEN) {
        if (FloatWindowManager.getInstance().checkPermission(getApplicationContext())) {
            if (FloatWindow.get() == null && IS_OPEN ) {
                // FloatView floatView = new FloatView(this);
                //建造者模式
                FloatWindow
                        .with(getApplication())
                        .setView(floatView)
                        .setX(Screen.width, IS_PORTRAIT,0.0f)
                        .setY(Screen.height, IS_PORTRAIT,0.3f)
                        //.setX(Screen.width,0.0f)
                        //.setY(Screen.height,0.3f)
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

    //关闭悬浮窗
    private void closeFloatWindow(boolean IS_CLOSE) {
        if (FloatWindow.get() != null ) {
            FloatWindow.destroy();
            if(IS_CLOSE) IS_OPEN = false;
        }
    }

    //手动activity横竖屏切换
    private void changeOrientation(){
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Toast.makeText(MainActivity.this, "竖屏", Toast.LENGTH_SHORT).show();
        } else {
            MainActivity.this. setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Toast.makeText(MainActivity.this, "横屏", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        // video size
        final int width = 1280;
        final int height = 720;
        File file = new File(Environment.getExternalStorageDirectory(),
                "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
        final int bitrate = 6000000;
        mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
        mRecorder.start();
        mButton.setText("停止录制");
        Toast.makeText(this, "正在录制中……", Toast.LENGTH_SHORT).show();
        moveTaskToBack(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRecorder != null){
            mRecorder.quit();
            mRecorder = null;
        }
    }

    //动态获取外部存储权限
    private void  mGetExternalStoragePermission(){
        //Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
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

    private void changeView(boolean mIS_BACK){
        if (FloatWindow.get() != null ) {
            IFloatWindow mIFloatWindow = FloatWindow.B.getFloatWindowImpl();
            Point point = ScreenUtil.getCurScreenSize(getApplication());

            // point x y 会根据横竖屏变换 mIFloatWindow.getX()和getY()也是；
            // 但是变换的顺序不一样，mIFloatWindow为旧 point为新
            //但如果是按BACK键 又不一样 比如在竖屏下按back键 pointx ponty 是没有变换的 横屏下也是没有变换的，特别注意
            double XP,YP;
            int newX,newY;
            if(!mIS_BACK) {
                 XP = mIFloatWindow.getX() * 1.0 / point.y;
                 YP = mIFloatWindow.getY() * 1.0 / point.x;
                 newX = (int) (point.x * XP);
                 newY = (int) (point.y * YP);
            }else {
                XP = mIFloatWindow.getX() * 1.0 / point.x;
                YP = mIFloatWindow.getY() * 1.0 / point.y;
                newX = (int) (point.y * XP);
                newY = (int) (point.x * YP);
            }
            IS_BACK=false;


            Log.e("testXY", "point.x" + point.x + " point.y" + point.y + " mIFloatWindow.getX()" + mIFloatWindow.getX()
                    + " mIFloatWindow.getY()" + mIFloatWindow.getY() + " XP" + XP + " YP" + YP + " newX" + newX + " newY" + newY);
            Toast.makeText(this, "oldX:" + mIFloatWindow.getX() + " oldY:" + mIFloatWindow.getY() +
                    "\nX的比例："+XP+"\nY的比例："+YP
                    +"\n按比例得到： newX:" + newX + " newY:" + newY, Toast.LENGTH_SHORT).show();
            mIFloatWindow.updateXY(newX, newY, MoveType.active);
        }else{
            Toast.makeText(this, "请先开始悬浮窗", Toast.LENGTH_SHORT).show();
        }
    }

    //监听横竖屏切换，调整悬浮框的位置
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        closeFloatWindow(IS_CLOSE);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            //切换横屏
            IS_PORTRAIT = false;
//            openFloatWindow(IS_PORTRAIT,IS_OPEN);
            changeView(IS_BACK);

        }else{
            //切换竖屏
            IS_PORTRAIT = true;
//            openFloatWindow(IS_PORTRAIT,IS_OPEN);
            changeView(IS_BACK);

        }
    }

    // 监听back键导致的横竖屏变换
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK  ){
//            closeFloatWindow(IS_CLOSE);
//            IS_PORTRAIT = true;
//            openFloatWindow(IS_PORTRAIT,IS_OPEN);
            if(!IS_PORTRAIT) {
                IS_BACK=true;
                changeView(IS_BACK);
                IS_PORTRAIT=true;
            }
        }
        return super.onKeyDown(keyCode,event);
    }

}
