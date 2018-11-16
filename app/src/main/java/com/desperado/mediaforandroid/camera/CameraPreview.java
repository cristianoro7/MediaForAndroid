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

    private CameraPreviewLifeCircle mCameraPreviewLifeCircle;

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

    public void setCallback(CameraPreviewLifeCircle mCameraPreviewLifeCircle) {
        this.mCameraPreviewLifeCircle = mCameraPreviewLifeCircle;
    }

    protected void dispatchSurfaceChanged() {
        if (mCameraPreviewLifeCircle != null) {
            mCameraPreviewLifeCircle.onSurfaceChange();
        }
    }

    protected void dispatchSurfaceCreate() {
        if (mCameraPreviewLifeCircle != null) {
            mCameraPreviewLifeCircle.onSurfaceChange();
        }
    }

    interface CameraPreviewLifeCircle {
        void onSurfaceChange();
        void onSurfaceCreate();
    }
}
