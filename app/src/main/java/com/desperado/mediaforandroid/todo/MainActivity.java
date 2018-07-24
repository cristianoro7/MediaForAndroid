package com.desperado.mediaforandroid.todo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.desperado.mediaforandroid.R;
import com.desperado.mediaforandroid.audio.AudioActivity;
import com.desperado.mediaforandroid.jni.JNIActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_audio).setOnClickListener(this);
        findViewById(R.id.main_jni).setOnClickListener(this);
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
        }
    }
}
