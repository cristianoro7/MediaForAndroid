package com.desperado.mediaforandroid.audio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.desperado.mediaforandroid.R;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by kamlin on 18-7-21.
 */
public class AudioActivity extends AppCompatActivity implements View.OnClickListener, AudioCapture.OnAudioCaptureListener {

    private AudioCapture mAudioCapture;

    private AudioPlayer mAudioPlayer;

    private WaveEncoder mWaveEncoder;

    private AACEncoder mAacEncoder;

    private Button mBtnCaptureAndPlay;
    private Button mBtnAACEncder;
    private Button mBtnWaveEncoder;

    private boolean isPlay = false;
    private boolean isWaveEncode = false;
    private boolean isAACEncode = false;

    private static final int AUDIO_RECORD_CODE = 111;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 222;
    private File mFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        requestPermission();
        initView();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORD_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
        }
    }

    private void initView() {
        mBtnCaptureAndPlay = findViewById(R.id.audio_btn_capture_play);
        mBtnAACEncder = findViewById(R.id.audio_btn_aac_encoder);
        mBtnWaveEncoder = findViewById(R.id.audio_btn_capture_wave_encoder);

        mBtnCaptureAndPlay.setOnClickListener(this);
        mBtnAACEncder.setOnClickListener(this);
        mBtnWaveEncoder.setOnClickListener(this);

        findViewById(R.id.audio_btn_capture_play_stop).setOnClickListener(this);
        findViewById(R.id.audio_btn_aac_encoder_stop).setOnClickListener(this);
        findViewById(R.id.audio_btn_capture_wave_encoder_stop).setOnClickListener(this);
    }

    private void init() {
        mAudioCapture = new AudioCapture();
        mAudioCapture.setOnAudioCaptureListener(this);
        mAudioPlayer = new AudioPlayer();
        mAacEncoder = new AACEncoder();
        mWaveEncoder = new WaveEncoder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_btn_capture_play:
                isPlay = true;
                isAACEncode = false;
                isWaveEncode = false;
                performCaptureAndPlay();
                break;
            case R.id.audio_btn_aac_encoder:
                break;
            case R.id.audio_btn_capture_wave_encoder:
                isWaveEncode = true;
                isPlay = false;
                isAACEncode = false;
                performCaptureAndWaveEncode();
                break;
            case R.id.audio_btn_capture_play_stop:
                mAudioPlayer.stop();
                break;
            case R.id.audio_btn_capture_wave_encoder_stop:
                mWaveEncoder.closeFile();
                break;
        }
    }


    @Override
    public void onAudioFrameCaptured(byte[] bytes) {
        if (isPlay) {
            mAudioPlayer.play(bytes, 0, bytes.length);
        } else if (isWaveEncode) {
            mWaveEncoder.writeData(bytes, 0, bytes.length);
        } else {
        }
    }

    private void performCaptureAndPlay() {
        mAudioCapture.start();
        mAudioPlayer.start();
    }

    private void performCaptureAndWaveEncode() {
        boolean isOpenOk = false;
        try {
            isOpenOk = mWaveEncoder.open(mFileDir + "/" + System.currentTimeMillis() + ".wav", AudioCapture.DEFAULT_SAMPLE_RATE,
                    (short) 1, (short) 16);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isOpenOk = false;
        }
        if (isOpenOk) {
            mAudioCapture.start();
        } else {
            Toast.makeText(this, "打开文件失败!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_RECORD_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"请允许录音", Toast.LENGTH_LONG).show();
                }
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请允许SD写操作", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AudioActivity.class);
        context.startActivity(starter);
    }
}
