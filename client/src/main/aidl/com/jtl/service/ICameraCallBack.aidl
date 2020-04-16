// ICameraCallBack.aidl
package com.jtl.service;

// Declare any non-default types here with import statements
import com.jtl.service.CameraData;

interface ICameraCallBack {
    void cameraCallBack(in CameraData data);
}
