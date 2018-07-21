package com.desperado.mediaforandroid.todo;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.desperado.mediaforandroid.R;
import com.desperado.mediaforandroid.audio.AudioActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_audio).setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_audio:
                AudioActivity.start(this);
                break;
        }
    }
}
