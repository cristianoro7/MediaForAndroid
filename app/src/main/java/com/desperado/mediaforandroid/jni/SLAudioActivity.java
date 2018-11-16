package com.desperado.mediaforandroid.jni;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.desperado.mediaforandroid.R;

import java.io.File;

/**
 * Created by kamlin on 18-8-18.
 */
public class SLAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SLAudioActivity";

    private File mFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_audio);
        findViewById(R.id.pcm_btn_capture_play).setOnClickListener(this);
        findViewById(R.id.pcm_btn_capture_play_stop).setOnClickListener(this);
        initRecorder();

    }


    public static void start(Context context) {
        Intent starter = new Intent(context, SLAudioActivity.class);
        context.startActivity(starter);
    }

    private void initRecorder() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String nsr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String nsbs = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        Log.d(TAG, "initRecorder: sampleRate: " + nsr + ", framePerBuff: " + nsbs);
        SLAudioApi.initSLEngine(Integer.parseInt(nsr), Integer.parseInt(nsbs));
        SLAudioApi.createAudioRecorder(mFileDir + "/test.pcm");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pcm_btn_capture_play:
                SLAudioApi.start();
                break;
            case R.id.pcm_btn_capture_play_stop:
                SLAudioApi.stop();
                break;
        }
    }
}
