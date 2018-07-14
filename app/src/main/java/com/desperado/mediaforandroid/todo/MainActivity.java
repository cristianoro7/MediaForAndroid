package com.desperado.mediaforandroid.todo;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.desperado.mediaforandroid.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test.pcm";
    private AudioCaptureEngine engine = new AudioCaptureEngine(FILE_PATH);
    private AudioPlayEngine playEngine = new AudioPlayEngine(FILE_PATH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void onAudioCapture(View view) {
        switch (view.getId()) {
            case R.id.start:
//                playEngine.start();
                engine.start();
                break;
            case R.id.stop:
//                playEngine.stop();
                engine.stop();
                break;
            case R.id.start_play:
                playEngine.start();
                break;
            case R.id.stop_play:
                playEngine.stop();
                break;
            default:
                break;
        }
    }
}
