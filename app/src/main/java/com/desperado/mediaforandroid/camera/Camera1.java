package com.desperado.mediaforandroid.camera;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by kamlin on 18-8-25.
 */
public class Camera1 extends Camera implements CameraPreview.CameraPreviewLifeCircle {

    private static final String TAG = "Camera1";

    private static final int INVALID_CAMERA_ID = -1;

    private static final SparseArrayCompat<String> FLASH_MAP = new SparseArrayCompat<>(); //闪光灯映射map

    static {
        FLASH_MAP.put(FLASH_OFF, android.hardware.Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MAP.put(FLASH_ON, android.hardware.Camera.Parameters.FLASH_MODE_ON);
        FLASH_MAP.put(FLASH_TORCH, android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MAP.put(FLASH_AUTO, android.hardware.Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MAP.put(FLASH_RED_EYE, android.hardware.Camera.Parameters.FLASH_MODE_RED_EYE);
    }

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
    private boolean mIsAutoMode = true;
    private int mFlash;


    Camera1(Callback callback, CameraPreview preview) {
        super(callback, preview);
        preview.setCallback(this);
    }

    @Override
    boolean start() {
        //步骤1: 获取摄像头ID
        chooseCamera();
        //步骤2:打开获取Camera对象
        openCamera();
        boolean isSuccess = configPreview(); //设置预览View
        if (isSuccess) {
            mCamera.startPreview();
            mShowingPreview = true;
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
                return;
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
            Log.d(TAG, "start: preview size: " + s.width + ", " + s.height);
        }
        //获取支持照相的大小
        mPictureSizes.clear();
        List<android.hardware.Camera.Size> supportPictureSizes = mCameraParameters.getSupportedPictureSizes();
        for (android.hardware.Camera.Size s : supportPictureSizes) {
            mPictureSizes.add(new Size(s.width, s.height));
            Log.d(TAG, "start: picture size: " + s.toString());
        }
        if (mAspectRatio == null) {
            mAspectRatio = AspectRatio.of(16, 9);
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
        Log.d("CR7", "configCameraParameters: " + size.getWidth() + ", " + size.getHeight());
        mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
        mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        mCameraParameters.setRotation(calCameraRotation(mDisplayOrientation));
        setFocusMode(mIsAutoMode);
        setFlashMode(mFlash);
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

    private boolean setFocusMode(boolean isAutoMode) {
        mIsAutoMode = isAutoMode;
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (isAutoMode && modes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO)) {
                mCameraParameters.setFocusMode(android.hardware.Camera.Parameters.FLASH_MODE_AUTO);
            } else if (modes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean setFlashMode(int flash) {
        if (isCameraOpened()) {
            List<String> flashModes = mCameraParameters.getSupportedFlashModes();
            String mode = FLASH_MAP.get(flash);
            if (flashModes != null && flashModes.contains(mode)) {
                mFlash = flash;
                mCameraParameters.setFlashMode(mode);
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
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
                } else {
                    mCamera.setPreviewTexture((SurfaceTexture) mCameraPreview.getSurfaceTexture());
                }
                mCamera.startPreview();
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
    void setAutoFocus(boolean isAutoFocus) {
        if (mIsAutoMode == isAutoFocus) {
            return;
        }
        if (setFocusMode(isAutoFocus)) {
            mCamera.setParameters(mCameraParameters);
        }
    }


    @Override
    boolean getFocusMode() {
        return mIsAutoMode;
    }

    @Override
    void setFlash(int flash) {
        if (mFlash == flash) {
            return;
        }
        if (setFlashMode(flash)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    int getFlash() {
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
            int o =  (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
            Log.d(TAG, "calcDisplayOrientation: " + o);
            return o;
        } else {  // back-facing
            int p = (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
            Log.d(TAG, "calcDisplayOrientation: " + p);
            return p;
//            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        }
    }

    @Override
    public void onSurfaceChange() {
        if (mCamera != null) { //TODO: 屏幕旋转的回调, 重新设置和预览类的宽高
            configPreview(); //1. 配置CameraPreview
            configCameraParameters(); //2. 重新设置Camera的参数
        }
    }

    @Override
    public void onSurfaceCreate() {

    }
}
