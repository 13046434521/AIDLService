package com.jtl.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author：TianLong
 * @date：2020/4/17 20:58
 */
public class CameraSize implements Parcelable {
    public static final Creator<CameraSize> CREATOR = new Creator<CameraSize>() {
        @Override
        public CameraSize createFromParcel(Parcel in) {
            return new CameraSize(in);
        }

        @Override
        public CameraSize[] newArray(int size) {
            return new CameraSize[size];
        }
    };
    private ISize[] mISizes;

    public CameraSize(ISize[] ISizes) {
        mISizes = ISizes.clone();
    }

    protected CameraSize(Parcel in) {
        mISizes = in.createTypedArray(ISize.CREATOR);
    }

    public ISize[] getISizes() {
        return mISizes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(mISizes, flags);
    }
}
