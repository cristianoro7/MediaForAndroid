package com.desperado.mediaforandroid.jni;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.desperado.mediaforandroid.R;

/**
 * Created by kamlin on 2018/7/24.
 */
public class JNIActivity extends AppCompatActivity implements View.OnClickListener {

    private JNITest jniTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni);
        init();
        initView();
    }

    private void init() {
        jniTest = new JNITest();
    }

    private void initView() {
        findViewById(R.id.jni_btn_say_hello).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jni_btn_say_hello:
                String stringFromNative = jniTest.sayHello("kamlin");
                makeToast(stringFromNative);
                break;
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, JNIActivity.class);
        context.startActivity(starter);
    }

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
