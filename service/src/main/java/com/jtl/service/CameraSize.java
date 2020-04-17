package com.jtl.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * @author：TianLong
 * @date：2020/4/17 21:26
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
    private int width;
    private int height;

    public CameraSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    protected CameraSize(Parcel in) {
        width = in.readInt();
        height = in.readInt();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @NonNull
    @Override
    public String toString() {
        return width + " x " + height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }
}
