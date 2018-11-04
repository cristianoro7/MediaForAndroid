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
 */
public class CameraView extends FrameLayout {

    private CameraPreview mCameraPreview;
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
        mCameraPreview = crateCameraPreview(context);
        mCameraLifeCircle = new CameraLifeCircleBridge();
        mCamera = createCamera();
        mDisplayOrientationDetector = new CameraDisplayOrientationDetector(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //测试CameraPreview
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        AspectRatio ratio = getAspectRatio();
        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            ratio = ratio.inverse();
        }
        if (h < w * )
    }

    private CameraPreview crateCameraPreview(Context context) {
        CameraPreview cameraPreview;
        if (Build.VERSION.SDK_INT < 14) {
            cameraPreview = new SurfaceViewPreview(context, this);
        } else {
            cameraPreview = new SurfaceViewPreview(context ,this); //todo:替换TextureView
        }
        return cameraPreview;
    }

    private Camera createCamera() {
        Camera camera;
        if (Build.VERSION.SDK_INT < 21) {
            camera = new Camera1(mCameraLifeCircle, mCameraPreview);
        } else {
            camera = new Camera1(mCameraLifeCircle, mCameraPreview); //todo: 替换成Camera2
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
