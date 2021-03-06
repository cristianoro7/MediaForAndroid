package com.desperado.mediaforandroid;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.desperado.mediaforandroid.audio.AudioActivity;
import com.desperado.mediaforandroid.camera.CameraActivity;
import com.desperado.mediaforandroid.jni.JNIActivity;
import com.desperado.mediaforandroid.jni.SLAudioActivity;
import com.desperado.mediaforandroid.todo.VideoEditor;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_audio).setOnClickListener(this);
        findViewById(R.id.main_jni).setOnClickListener(this);
        findViewById(R.id.main_native_audio).setOnClickListener(this);
        findViewById(R.id.main_camera).setOnClickListener(this);
        findViewById(R.id.main_merge_video).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_audio:
                AudioActivity.start(this);
                break;
            case R.id.main_jni:
                JNIActivity.start(this);
                break;
            case R.id.main_native_audio:
                SLAudioActivity.start(this);
                break;
            case R.id.main_camera:
                CameraActivity.start(this);
                break;
            case R.id.main_merge_video:
                String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test.h264";
                VideoEditor.muxVideo(null, videoPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "test.mp4");
                break;
        }
    }
}
