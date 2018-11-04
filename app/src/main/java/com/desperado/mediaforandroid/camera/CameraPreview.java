package com.desperado.mediaforandroid.camera;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by kamlin on 18-7-1.
 * 预览兼容类, 定义了与相机预览相关的操作, 并且提供向后兼容管理
 */
public abstract class CameraPreview {

    private int mWidth;
    private int mHeight;

    private Callback mCallback;

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    abstract Surface getSurface();

    abstract Class<?> getOutputClass();

    abstract View getView();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract boolean isReady();

    SurfaceHolder getSurfaceHolder() {
        return null;
    }

    Object getSurfaceTexture() {
        return null;
    }

    void setBufferSize(int width, int height) {
    }

    interface Callback {
        void onSurfaceChange();
    }

    public void setCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    protected void dispatchSurfaceChanged() {
        if (mCallback != null) {
            mCallback.onSurfaceChange();
        }
    }
}
