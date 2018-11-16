package com.desperado.mediaforandroid.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.desperado.mediaforandroid.R;

/**
 * Created by kamlin on 2018/11/13.
 */
public class CameraActivity extends AppCompatActivity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_camera);
        mCameraView = findViewById(R.id.camera_view);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.setFacing(Camera.FACING_BACK);
                mCameraView.setAutoFocusMode(true);
                mCameraView.setFlashMode(Camera.FLASH_RED_EYE);
            }
        });
        findViewById(R.id.front).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.setFacing(Camera.FACING_FRONT);
                mCameraView.setAutoFocusMode(true);
                mCameraView.setFlashMode(Camera.FLASH_RED_EYE);
            }
        });
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, CameraActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

}
