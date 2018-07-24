package com.desperado.mediaforandroid.jni;

/**
 * Created by kamlin on 2018/7/24.
 */
public class JNITest {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native String sayHello(String name);
}
