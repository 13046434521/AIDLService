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
public class MainActivity extends AppCompatActivity implements ServiceConnection{
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
            this.getApplicationContext().bindService(intent, this, BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this.getApplicationContext(), "已绑定Service", Toast.LENGTH_SHORT).show();
        }

    }

    public void unBindService(View view) {
        if (isBindService) {
            Intent intent = new Intent(serviceAction);
            intent.setPackage(servicePackage);
            this.getApplicationContext().bindService(intent, this, BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this.getApplicationContext(), "已解绑Service", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera(View view) throws RemoteException {
        if (isConnectService&&mICameraInterface!=null){
            mICameraInterface.openCamera("0");
        }
    }

    public void closeCamera(View view) throws RemoteException {
        if (isConnectService&&mICameraInterface!=null){
            mICameraInterface.closeCamera();
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mICameraInterface = ICameraInterface.Stub.asInterface(service);
        isConnectService = true;
        try {
            mICameraInterface.initCamera(640,480,true);
            if (mICameraCallBack==null){
                mICameraCallBack = new CameraFrameCallBack();
            }
            mICameraInterface.register(mICameraCallBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isConnectService = false;
        try {
            mICameraInterface.unregister(mICameraCallBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mICameraInterface = null;
    }

    private class CameraFrameCallBack extends ICameraCallBack.Stub{

        @Override
        public void cameraCallBack(CameraData data) throws RemoteException {
            mCameraGLSurface.setCameraData(data.mCameraId,data.imageData);
            mCameraGLSurface.requestRender();
        }
    }
}
