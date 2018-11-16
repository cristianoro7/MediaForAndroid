package com.desperado.mediaforandroid.camera;

import android.view.View;

/**
 * Created by kamlin on 18-8-25.
 */
abstract class Camera {

    public static final int FACING_BACK = 0;
    public static final int FACING_FRONT = 1;

    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_TORCH = 2;
    public static final int FLASH_AUTO = 3;
    public static final int FLASH_RED_EYE = 4;

    public static final int LANDSCAPE_90 = 90;
    public static final int LANDSCAPE_270 = 270;

    protected Callback mCallback;

    protected CameraPreview mCameraPreview;

    Camera(Callback callback, CameraPreview preview) {
        this.mCallback = callback;
        this.mCameraPreview = preview;
    }

    View getView() {
        return mCameraPreview.getView();
    }

    abstract boolean start();

    abstract boolean stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract void setAutoFocus(boolean isAutoFocus);

    abstract boolean getFocusMode();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void takePicture();

    abstract void setDisplayOrientation(int orientation);

    abstract AspectRatio getAspectRatio();

    interface Callback {
        void onCameraOpen();
        void onCameraClose();
        void onPictureTaken(byte[] data);
    }
}
