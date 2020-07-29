// ICameraCallBack.aidl
package com.jtl.service;

// Declare any non-default types here with import statements
import com.jtl.service.CameraData;
import android.os.SharedMemory;

interface ICameraCallBack {
    void cameraCallBack(in CameraData data);

    void sharedCameraCallBack(in SharedMemory memory);
}
