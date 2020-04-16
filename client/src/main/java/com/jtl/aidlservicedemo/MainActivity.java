package com.jtl.aidlservicedemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jtl.aidlservicedemo.camera.CameraGLSurface;
import com.jtl.service.CameraData;
import com.jtl.service.ICameraCallBack;
import com.jtl.service.ICameraInterface;

/**
 * @author TianLong
 */
public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private String servicePackage = "com.jtl.service";
    private String serviceAction = "com.jtl.cameraservice";
    private volatile boolean isBindService = false;
    private volatile boolean isConnectService = false;
    private ICameraInterface mICameraInterface;
    private CameraGLSurface mCameraGLSurface;
    private ICameraCallBack mICameraCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraGLSurface = findViewById(R.id.gl_main_camera);
    }


    public void bindService(View view) {
        if (!isBindService) {
            Intent intent = new Intent(serviceAction);
            intent.setPackage(servicePackage);
            isBindService = this.bindService(intent, this, BIND_AUTO_CREATE);
            Toast.makeText(this.getApplicationContext(), isBindService ? "绑定Service成功" : "绑定Service失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getApplicationContext(), "已绑定Service", Toast.LENGTH_SHORT).show();
        }

    }

    public void unBindService(View view) {
        if (isBindService) {
            //回调解注册
            try {
                mICameraInterface.unregister(mICameraCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.unbindService(this);
            isBindService = false;
            isConnectService = false;
            mICameraInterface = null;
            Toast.makeText(this.getApplicationContext(), "解绑Service成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getApplicationContext(), "已解绑Service", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera(View view) throws RemoteException {
        if (isConnectService && mICameraInterface != null) {
            mICameraInterface.openCamera("0");
            Toast.makeText(this.getApplicationContext(), "开启相机", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeCamera(View view) throws RemoteException {
        if (isConnectService && mICameraInterface != null) {
            mICameraInterface.closeCamera();
            Toast.makeText(this.getApplicationContext(), "关闭相机", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mICameraInterface = ICameraInterface.Stub.asInterface(service);
        isConnectService = true;
        try {
            mICameraInterface.initCamera(640, 480, true);
            if (mICameraCallBack == null) {
                mICameraCallBack = new CameraFrameCallBack();
            }
            mICameraInterface.register(mICameraCallBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //在连接正常关闭的情况下是不会被调用的, 该方法只在Service 被破坏了或者被杀死的时候调用.
        isConnectService = false;
        try {
            mICameraInterface.unregister(mICameraCallBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mICameraInterface = null;
    }

    private class CameraFrameCallBack extends ICameraCallBack.Stub {

        @Override
        public void cameraCallBack(CameraData data) throws RemoteException {
            mCameraGLSurface.setCameraData(data.mCameraId, data.imageData);
            mCameraGLSurface.requestRender();
        }
    }
}
