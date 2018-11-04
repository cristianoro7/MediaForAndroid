package com.desperado.mediaforandroid.camera;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * Created by kamlin on 18-8-25.
 */
public class AspectRatio implements Comparable<AspectRatio>, Parcelable {

    private static final SparseArrayCompat<SparseArrayCompat<AspectRatio>> CACHE = new SparseArrayCompat<>(16);

    private int mX;
    private int mY;

    public int getmX() {
        return mX;
    }

    public int getmY() {
        return mY;
    }

    private AspectRatio(int x, int y) {
        this.mX = x;
        this.mY = y;
    }

    public boolean matches(Size size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        int x = size.getWidth() / gcd;
        int y = size.getHeight() / gcd;
        return mX == x && mY == y;
    }

    public float toFloat() {
        return mX / mY;
    }

    public AspectRatio inverse() {
        //noinspection SuspiciousNameCombination
        return AspectRatio.of(mY, mX);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof AspectRatio) {
            AspectRatio aspectRatio = (AspectRatio) obj;
            return mX == aspectRatio.mX && mY == aspectRatio.mY;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return mY ^ ((mX << (Integer.SIZE / 2)) | (mX >>> (Integer.SIZE / 2)));
    }

    @Override
    public int compareTo(@NonNull AspectRatio another) {
        if (equals(another)) {
            return 0;
        } else if (toFloat() - another.toFloat() > 0) {
            return 1;
        }
        return -1;
    }

    public static AspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;

        SparseArrayCompat<AspectRatio> arrayX = CACHE.get(x);
        if (arrayX == null) {
            AspectRatio ratio = new AspectRatio(x, y);
            arrayX = new SparseArrayCompat<>();
            arrayX.put(y, ratio);
            CACHE.put(x, arrayX);
            return ratio;
        } else {
            AspectRatio ratio = arrayX.get(y);
            if (ratio == null) {
                ratio = new AspectRatio(x, y);
                arrayX.put(y, ratio);
            }
            return ratio;
        }
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }

    public static final Creator<AspectRatio> CREATOR = new Creator<AspectRatio>() {
        @Override
        public AspectRatio createFromParcel(Parcel in) {
            int x = in.readInt();
            int y = in.readInt();
            return of(x, y);
        }

        @Override
        public AspectRatio[] newArray(int size) {
            return new AspectRatio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }
}
