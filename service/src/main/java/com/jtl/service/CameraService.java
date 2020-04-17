package com.jtl.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;

/**
 * @author TianLong
 * @date 2020/4/15
 */
public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private CameraInterface mCameraInterface;

    public CameraService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG, "onBind");
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.w(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");
    }


    private class CameraInterface extends ICameraInterface.Stub implements CameraWrapper.CameraDataListener {
        private static final String TAG = "CameraInterface";
        private RemoteCallbackList<ICameraCallBack> mRemoteCallbackList = new RemoteCallbackList<>();
        private CameraWrapper mCameraWrapper;

        @Override
        public void initCamera(int width, int height, boolean autofocus) throws RemoteException {
            Log.w(TAG, "initCamera");
            mCameraWrapper = new CameraWrapper(getApplicationContext(), width, height, true, this);
        }

        @Override
        public void openCamera(String cameraId) throws RemoteException {
            Log.w(TAG, "openCamera:" + cameraId);
            mCameraWrapper.openCamera(cameraId);
        }

        @Override
        public void closeCamera() throws RemoteException {
            Log.w(TAG, "closeCamera");
            mCameraWrapper.closeCamera();
        }

        @Override
        public void register(ICameraCallBack callBack) throws RemoteException {
            Log.w(TAG, "register");
            mRemoteCallbackList.register(callBack);
        }

        @Override
        public void unregister(ICameraCallBack callBack) throws RemoteException {
            Log.w(TAG, "unregister");
            mRemoteCallbackList.unregister(callBack);
        }

        @Override
        public CameraSize getCameraSize() throws RemoteException {
            return getSize();
        }

        @Override
        public void setCameraDataListener(String mCameraId, byte[] imageData, float timestamp, int imageFormat) {
            CameraData cameraData = new CameraData(mCameraId, imageData, timestamp, imageFormat);
            upDataCameraData(cameraData);
        }

        public void upDataCameraData(CameraData cameraData) {
            int len = mRemoteCallbackList.beginBroadcast();
            for (int i = 0; i < len; i++) {
                try {
                    mRemoteCallbackList.getBroadcastItem(i).cameraCallBack(cameraData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e(TAG, "相机Service回调Exception：" + e.getMessage());
                }
            }
            mRemoteCallbackList.finishBroadcast();
        }

        public CameraSize getSize() {
            Size[] sizes = mCameraWrapper.getSizes();
            ISize[] iSize = new ISize[sizes.length];
            for (int i = 0; i < sizes.length; i++) {
                ISize size = new ISize(sizes[i].getWidth(), sizes[i].getHeight());
                iSize[i] = size;
            }
            return new CameraSize(iSize);
        }
    }
}
