package com.jtl.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

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
        private SharedMemory mSharedMemory;
        private String SharedName = "cameraService";
        private int width;
        private int height;
        private boolean isSharedMemory = false;

        @Override
        public void initCamera(int width, int height, boolean autofocus) throws RemoteException {
            Log.w(TAG, "initCamera");
            mCameraWrapper = new CameraWrapper(getApplicationContext(), width, height, true, this);
            this.width = width;
            this.height = height;
        }

        @Override
        public void openCamera(String cameraId) throws RemoteException {
            Log.w(TAG, "openCamera:" + cameraId);
            mCameraWrapper.openCamera(cameraId);

//            if (isSharedMemory && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                //YUV数据的宽高
//                try {
//                    mSharedMemory = SharedMemory.create(SharedName, width * height * 3 / 2);
//                    Log.w(TAG, "SharedMemory 共享内存跨进程传输数据");
//                    mSharedMemory.map(PROT_READ | PROT_WRITE,0,width * height * 3 / 2);
////                    mSharedMemory.setProtect(PROT_READ);
//                } catch (ErrnoException e) {
//                    e.printStackTrace();
//                }
//           }
        }

        @Override
        public void closeCamera() throws RemoteException {
            Log.w(TAG, "closeCamera");
            mCameraWrapper.closeCamera();

//            if (mSharedMemory!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                mSharedMemory.close();
//            }
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
        public List getCameraSize() throws RemoteException {
            return getSize();
        }

        @Override
        public boolean isSharedMemory(boolean isShared) throws RemoteException {
            return isSharedMemory = isShared && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1;
        }

        @Override
        public void setCameraDataListener(String mCameraId, byte[] imageData, float timestamp, int imageFormat) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                try {
//                    Log.d(TAG,"共享内存方式跨进程传输数据");
//                    mSharedMemory.mapReadWrite().get(imageData);
//                    upDataCameraData(mSharedMemory);
//                } catch (ErrnoException e) {
//                    e.printStackTrace();
//                }
//            } else {
//
//            }
            Log.d(TAG, "Binder方式跨进程传输数据");
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

        public void upDataCameraData(SharedMemory sharedMemory) {
            int len = mRemoteCallbackList.beginBroadcast();
            for (int i = 0; i < len; i++) {
                try {
                    mRemoteCallbackList.getBroadcastItem(i).sharedCameraCallBack(sharedMemory);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e(TAG, "相机Service回调Exception：" + e.getMessage());
                }
            }
            mRemoteCallbackList.finishBroadcast();
        }

        public List getSize() {
            Size[] sizes = mCameraWrapper.getSizes();
            List list = new ArrayList();
            for (int i = 0; i < sizes.length; i++) {
                CameraSize size = new CameraSize(sizes[i].getWidth(), sizes[i].getHeight());
                list.add(size);
            }
            return list;
        }
    }
}
