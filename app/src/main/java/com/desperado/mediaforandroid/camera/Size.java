package com.desperado.mediaforandroid.camera;

import android.support.annotation.NonNull;

/**
 * Created by kamlin on 18-8-25.
 */
public class Size implements Comparable<Size> {

    private int mWidth;
    private int mHeight;

    public Size(int mWidth, int mHeight) {
        this.mWidth = mWidth;
        this.mHeight = mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Size) {
            Size size = (Size) obj;
            return mWidth == size.mWidth && mHeight == size.mHeight;
        }
        return false;
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }

    @Override
    public int hashCode() {
        return mHeight ^ ((mWidth << (Integer.SIZE / 2)) | (mWidth >>> (Integer.SIZE / 2)));
    }

    @Override
    public int compareTo(@NonNull Size o) {
        return mWidth * mHeight - o.mWidth * o.mHeight;
    }
}
