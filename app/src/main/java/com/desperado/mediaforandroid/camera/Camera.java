package com.desperado.mediaforandroid.camera;

import android.view.View;

/**
 * Created by kamlin on 18-8-25.
 */
abstract class Camera {

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

    abstract void setAutoFocus(String mode);

    abstract String getFocusMode();

    abstract void setFlash(String flash);

    abstract String getFlash();

    abstract void takePicture();

    abstract void setDisplayOrientation(int orientation);

    abstract AspectRatio getAspectRatio();

    interface Callback {
        void onCameraOpen();
        void onCameraClose();
        void onPictureTaken(byte[] data);
    }
}
