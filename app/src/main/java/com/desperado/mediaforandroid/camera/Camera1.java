package com.desperado.mediaforandroid.camera;

import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by kamlin on 18-8-25.
 */
public class Camera1 extends Camera {

    private static final String TAG = "Camera1";

    private static final int INVALID_CAMERA_ID = -1;

    private android.hardware.Camera mCamera;
    private android.hardware.Camera.CameraInfo mCameraInfo = new android.hardware.Camera.CameraInfo();
    private android.hardware.Camera.Parameters mCameraParameters;
    private int mFacing;
    private int mCameraId;

    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();
    private AspectRatio mAspectRatio;
    private int mDisplayOrientation;
    private boolean mShowingPreview;
    private String mFocusMode;
    private String mFlash;


    Camera1(Callback callback, CameraPreview preview) {
        super(callback, preview);
        preview.setCallback(new CameraPreview.Callback() {
            @Override
            public void onSurfaceChange() {
                if (mCamera != null) {

                }
            }
        });
    }

    @Override
    boolean start() {
        //步骤1: 获取摄像头ID
        chooseCamera();
        //步骤2:打开获取Camera对象
        openCamera();
        boolean isSuccess = configPreview(); //设置预览View
        if (isSuccess) {
            mShowingPreview = true;
            mCamera.startPreview();
            return true;
        } else {
            return false;
        }
    }

    private void chooseCamera() {
        for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); i++) {
            android.hardware.Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    private void openCamera() {
        releaseCamera(); //先释放掉Camera对象
        mCamera = android.hardware.Camera.open(mCameraId);
        mCameraParameters = mCamera.getParameters();
        //获取支持的预览大小
        mPreviewSizes.clear();
        List<android.hardware.Camera.Size> supportedPreviewSizes = mCameraParameters.getSupportedPreviewSizes();
        for (android.hardware.Camera.Size s : supportedPreviewSizes) {
            mPreviewSizes.add(new Size(s.width, s.height));
            Log.d(TAG, "start: preview size: " + s.toString());
        }
        //获取支持照相的大小
        mPictureSizes.clear();
        List<android.hardware.Camera.Size> supportPictureSizes = mCameraParameters.getSupportedPictureSizes();
        for (android.hardware.Camera.Size s : supportPictureSizes) {
            mPictureSizes.add(new Size(s.width, s.height));
            Log.d(TAG, "start: picture size: " + s.toString());
        }
        if (mAspectRatio == null) {
            mAspectRatio = AspectRatio.of(4, 3);
        }
        configCameraParameters();
        mCamera.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation));
        mCallback.onCameraOpen();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClose();
        }
    }

    private void configCameraParameters() {
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) {
            throw new IllegalStateException("unsupport size");
        }
        Size size = findBestSize(sizes);
        Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
        if (mShowingPreview) {
            mCamera.stopPreview();
        }
        //设置相机参数, 包括preview size, picture size, 相机旋转角度, display角度,聚焦模式, 闪光灯模式
        mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
        mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        mCameraParameters.setRotation(calCameraRotation(mDisplayOrientation));
        setFocusMode();
        setFlashMode();
        mCamera.setParameters(mCameraParameters);
        if (mShowingPreview) {
            mCamera.startPreview();
        }
    }

    private Size findBestSize(SortedSet<Size> sizes) {
        //选择最优的比例
        if (!mCameraPreview.isReady()) {
            return sizes.first(); //选取最小的比例
        }
        int dw;
        int dh;
        if (isLandscape(mDisplayOrientation)) {
            dw = mCameraPreview.getHeight();
            dh = mCameraPreview.getWidth();
        } else {
            dw = mCameraPreview.getWidth();
            dh = mCameraPreview.getHeight();
        }
        Size result = null;
        for (Size sz : sizes) { //寻找最接近的比例
            if (dw <= sz.getWidth() && dh <= sz.getHeight()) {
                return sz;
            }
            result = sz;
        }
        return result;
    }

    private void setFocusMode() {
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (modes != null) {
                if (modes.contains(mFocusMode)) {
                    mCameraParameters.setFocusMode(mFocusMode);
                } else {
                    mCameraParameters.setFocusMode(modes.get(0));
                }
            }
        }
    }

    private void setFlashMode() {
        if (isCameraOpened()) {
            List<String> flashModes = mCameraParameters.getSupportedFlashModes();
            if (flashModes != null) {
                if (flashModes.contains(mFlash)) {
                    mCameraParameters.setFlashMode(mFlash);
                } else {
                    mCameraParameters.setFlashMode(flashModes.get(0));
                }
            }
        }
    }

    private boolean configPreview() {
        if (mCameraPreview.isReady()) {
            try {
                if (mCameraPreview.getOutputClass() == SurfaceHolder.class) {
                    boolean isNeedsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
                    if (isNeedsToStopPreview) {
                        mCamera.stopPreview();
                    }
                    mCamera.setPreviewDisplay(mCameraPreview.getSurfaceHolder());
                    if (isNeedsToStopPreview) {
                        mCamera.startPreview();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    boolean stop() {
        if (mCamera != null) {
            releaseCamera();
        }
        mShowingPreview = false;
        return true;
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    int getFacing() {
        return mFacing;
    }

    @Override
    void setAutoFocus(String mode) {
        if (mFocusMode != null && mFocusMode.equals(mode)) {
            return;
        }
        mFocusMode = mode;
        setFocusMode();
        mCamera.setParameters(mCameraParameters);
    }


    @Override
    String getFocusMode() {
        return mFocusMode;
    }

    @Override
    void setFlash(String flash) {
        if (mFlash != null && mFlash.equals(flash)) {
            return;
        }
        mFlash = flash;
        setFlashMode();
        mCamera.setParameters(mCameraParameters);
    }

    @Override
    String getFlash() {
        return mFlash;
    }

    @Override
    void takePicture() {

    }

    @Override
    void setDisplayOrientation(int orientation) {
        if (mDisplayOrientation == orientation) {
            return;
        }
        mDisplayOrientation = orientation;
        if (isCameraOpened()) {
            mCameraParameters.setRotation(calCameraRotation(orientation));
            final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(calcDisplayOrientation(orientation));
            if (needsToStopPreview) {
                mCamera.startPreview();
            }
        }
    }

    @Override
    AspectRatio getAspectRatio() {
        return mAspectRatio;
    }

    protected boolean isLandscape(int orientation) {
        return orientation == 90 || orientation == 270;
    }

    private int calCameraRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    private int calcDisplayOrientation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }
}
