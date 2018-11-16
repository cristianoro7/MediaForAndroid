package com.desperado.mediaforandroid.camera;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kamlin on 18-8-26.
 * Camera1:
 * DONE
 * 1. 设置正反面拍摄
 * 2. 支持设置聚焦模式
 * 3. 支持设置闪光灯模式
 * 4. 使用SurfaceView进行预览
 * 5. 使用TextureView进行渲染
 * TODO:
 * 1. 拍摄和拍照的尺寸最佳尺寸大小算法
 * 2. 相机旋转角度算法: 主要理解mCameraInfo.orientation, 将摄像头传感器旋转顺时针旋转多少度后, 预览的画面是自然画面. 后置摄像头: 90 前置摄像头: 270 (镜面对称)
 * 3. 拍照
 * 4. 录制
 *
 * Camera2:
 *
 */
public class CameraView extends FrameLayout {

    private Camera mCamera;
    private CameraLifeCircleBridge mCameraLifeCircle;
    private DisplayOrientationDetector mDisplayOrientationDetector;


    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        CameraPreview cameraPreview = crateCameraPreview(context);
        mCameraLifeCircle = new CameraLifeCircleBridge();
        mCamera = createCamera(cameraPreview);
        mDisplayOrientationDetector = new CameraDisplayOrientationDetector(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isCameraOpen()) {
            mCameraLifeCircle.reserveRequestLayoutOnOpen();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private CameraPreview crateCameraPreview(Context context) {
        CameraPreview cameraPreview;
        if (Build.VERSION.SDK_INT < 14) {
            cameraPreview = new SurfaceViewPreview(context, this);
        } else {
            cameraPreview = new TextureViewPreview(context, this);
        }
        return cameraPreview;
    }

    public void start() {
        mCamera.start();
    }

    public void stop() {
        mCamera.stop();
    }

    public boolean isCameraOpen() {
        return mCamera.isCameraOpened();
    }

    public void setFacing(int face) {
        mCamera.setFacing(face);
    }

    public void setAutoFocusMode(boolean isAuto) {
        mCamera.setAutoFocus(isAuto);
    }

    public void setFlashMode(int mode) {
        mCamera.setFlash(mode);
    }

    private Camera createCamera(CameraPreview cameraPreview) {
        Camera camera;
        if (Build.VERSION.SDK_INT < 21) {
            camera = new Camera1(mCameraLifeCircle, cameraPreview);
        } else {
            camera = new Camera1(mCameraLifeCircle, cameraPreview); //todo: 替换成Camera2
        }
        return camera;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDisplayOrientationDetector.disable();
    }

    public AspectRatio getAspectRatio() {
        return mCamera.getAspectRatio();
    }

    private class CameraLifeCircleBridge implements Camera.Callback {

        private final List<CameraLifeCircle> cameraLifeCircles = new ArrayList<>();

        private boolean mRequestLayoutOnOpen;

        public void add(CameraLifeCircle lifeCircle) {
            cameraLifeCircles.add(lifeCircle);
        }

        public void remove(CameraLifeCircle lifeCircle) {
            cameraLifeCircles.remove(lifeCircle);
        }

        @Override
        public void onCameraOpen() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false;
                requestLayout();
            }
            for (CameraLifeCircle c : cameraLifeCircles) {
                c.onCameraOpened(CameraView.this);
            }
        }

        @Override
        public void onCameraClose() {
            for (CameraLifeCircle c : cameraLifeCircles) {
                c.onCameraClosed(CameraView.this);
            }
        }

        @Override
        public void onPictureTaken(byte[] data) {
            for (CameraLifeCircle c : cameraLifeCircles) {
                c.onPictureTaken(CameraView.this, data);
            }
        }

        public void reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true;
        }
    }

    public abstract static class CameraLifeCircle {

        public void onCameraOpened(CameraView cameraView) {
        }

        public void onCameraClosed(CameraView cameraView) {
        }

        public void onPictureTaken(CameraView cameraView, byte[] data) {
        }
    }

    private class CameraDisplayOrientationDetector extends DisplayOrientationDetector {

        public CameraDisplayOrientationDetector(Context context) {
            super(context);
        }

        @Override
        public void onDisplayOrientationChanged(int displayOrientation) {
            mCamera.setDisplayOrientation(displayOrientation);
        }
    }
}
