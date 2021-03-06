// ICameraInterface.aidl
package com.jtl.service;

// Declare any non-default types here with import statements
import com.jtl.service.ICameracallBack;

interface ICameraInterface {

    void initCamera(int width,int height,boolean autofocus);

    void openCamera(String cameraId);

    void closeCamera();

    void register(ICameraCallBack callBack);

    void unregister(ICameraCallBack callBack);

    List getCameraSize();

    boolean isSharedMemory(boolean isShared);
}
