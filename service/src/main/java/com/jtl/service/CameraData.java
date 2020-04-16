package com.jtl.service;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * @author：TianLong
 * @date：2020/4/15 11:54
 */
public class CameraData implements Parcelable {
    private String mCameraId;
    private byte[] imageData;
    private float timestamp;
    private int imageFormat;

    public CameraData(String cameraId, byte[] imageData, float timestamp, int imageFormat) {
        mCameraId = cameraId;
        this.imageData = imageData;
        this.timestamp = timestamp;
        this.imageFormat = imageFormat;
    }

    protected CameraData(Parcel in) {
        mCameraId = in.readString();
        imageData = in.createByteArray();
        timestamp = in.readFloat();
        imageFormat = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCameraId);
        dest.writeByteArray(imageData);
        dest.writeFloat(timestamp);
        dest.writeInt(imageFormat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CameraData> CREATOR = new Creator<CameraData>() {
        @Override
        public CameraData createFromParcel(Parcel in) {
            return new CameraData(in);
        }

        @Override
        public CameraData[] newArray(int size) {
            return new CameraData[size];
        }
    };
}
