package com.jtl.aidlservicedemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.system.ErrnoException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jtl.aidlservicedemo.camera.CameraGLSurface;
import com.jtl.service.CameraData;
import com.jtl.service.CameraSize;
import com.jtl.service.ICameraCallBack;
import com.jtl.service.ICameraInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TianLong
 */
public class MainActivity extends AppCompatActivity implements ServiceConnection, Toolbar.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    private String servicePackage = "com.jtl.service";
    private String serviceAction = "com.jtl.cameraservice";
    private volatile boolean isBindService = false;
    private volatile boolean isConnectService = false;
    private ICameraInterface mICameraInterface;
    private ICameraCallBack mICameraCallBack;

    private Toolbar mToolbar;
    private CameraGLSurface mCameraGLSurface;
    private ConstraintLayout mConstraintLayout;
    //    private int imageWidth = 960;
//    private int imageHeight = 720;
    private int imageWidth = 1920;
    private int imageHeight = 1080;
    //    private int imageWidth = 4032;
//    private int imageHeight = 3024;
    private String mCameraId = Constant.CAMERA_BACK;
    private List<CameraSize> lists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraGLSurface = findViewById(R.id.gl_main_camera);
        mConstraintLayout = findViewById(R.id.layout_main_constraint);
        mToolbar = findViewById(R.id.tool_main_menu);

        mToolbar.setTitle("AIDL");
        mToolbar.inflateMenu(R.menu.camera_size);
        mToolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        for (CameraSize size : lists) {
            menu.add(size.getWidth() + " * " + size.getHeight());
        }
        getMenuInflater().inflate(R.menu.camera_size, menu);
        mToolbar.inflateMenu(R.menu.camera_size);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    private void setLayout(final int width, final int height) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mConstraintLayout);
                constraintSet.clear(R.id.gl_main_camera);

                constraintSet.connect(R.id.gl_main_camera, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(R.id.gl_main_camera, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(R.id.gl_main_camera, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
//                constraintSet.connect(R.id.gl_main_camera,ConstraintSet.BOTTOM,R.id.btn_service_bind,ConstraintSet.TOP);

                constraintSet.setDimensionRatio(R.id.gl_main_camera, height + ":" + width);
                constraintSet.applyTo(mConstraintLayout);
            }
        });

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
            mICameraInterface.isSharedMemory(true);
            mICameraInterface.openCamera(mCameraId);
            setLayout(imageWidth, imageHeight);
            lists = getCameraSize();
            for (CameraSize cameraSize : lists) {
                Log.w(TAG, cameraSize.toString());
            }
            Toast.makeText(this.getApplicationContext(), "开启相机", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeCamera(View view) throws RemoteException {
        if (isConnectService && mICameraInterface != null) {
            mICameraInterface.closeCamera();
            Toast.makeText(this.getApplicationContext(), "关闭相机", Toast.LENGTH_SHORT).show();
        }
    }

    public List getCameraSize() throws RemoteException {
        if (isConnectService && mICameraInterface != null) {
            return mICameraInterface.getCameraSize();
        }

        return null;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mICameraInterface = ICameraInterface.Stub.asInterface(service);
        isConnectService = true;
        try {
            mICameraInterface.initCamera(imageWidth, imageHeight, true);
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
            mCameraGLSurface.setDataSize(imageWidth, imageHeight);
            mCameraGLSurface.setCameraData(data.mCameraId, data.imageData);
            mCameraGLSurface.requestRender();
        }

        @RequiresApi(api = Build.VERSION_CODES.O_MR1)
        @Override
        public void sharedCameraCallBack(SharedMemory memory) throws RemoteException {
            mCameraGLSurface.setDataSize(imageWidth, imageHeight);
            byte[] bytes = new byte[memory.getSize()];
            try {
//                memory.mapReadWrite().position(0);
                memory.mapReadWrite().get(bytes);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            mCameraGLSurface.setCameraData(mCameraId, bytes);
            mCameraGLSurface.requestRender();
        }
    }
}
