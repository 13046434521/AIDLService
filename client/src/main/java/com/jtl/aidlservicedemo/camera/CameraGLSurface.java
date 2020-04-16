package com.jtl.aidlservicedemo.camera;

import android.content.Context;
import android.util.AttributeSet;

import com.jtl.aidlservicedemo.Constant;
import com.jtl.aidlservicedemo.base.BaseGLSurface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.jtl.aidlservicedemo.Constant.CAMERA_BACK;
import static com.jtl.aidlservicedemo.Constant.YUV420P_NV12;

/**
 * 作者:jtl
 * 日期:Created in 2019/9/9 19:44
 * 描述:
 * 更改:
 */
public class CameraGLSurface extends BaseGLSurface {
    private byte[] mYData;
    private byte[] mUVData;
    private int width = Constant.WIDTH;
    private int height = Constant.HEIGHT;
    private ByteBuffer mYBuffer;
    private ByteBuffer mUVBuffer;
    private CameraRender mCameraRender;

    private @Constant.CameraData
    int mCameraType = YUV420P_NV12;

    private @Constant.CameraType
    String mCameraId = CAMERA_BACK;
    public CameraGLSurface(Context context) {
        super(context);
    }

    public CameraGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    private void initData() {
        mYData = new byte[width * height];
        mYBuffer = ByteBuffer.allocateDirect(width * height);
        mYBuffer.order(ByteOrder.nativeOrder());
        mYBuffer.position(0);

        mUVData = new byte[width * height / 2];
        mUVBuffer = ByteBuffer.allocateDirect(width * height / 2);
        mUVBuffer.order(ByteOrder.nativeOrder());
        mUVBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);

        mCameraRender = new CameraRender();
        mCameraRender.createdGLThread(getContext().getApplicationContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
//        this.width = width;
//        this.height = height;
        mCameraRender.onSurfaceChanged(this.width, this.height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if (mCameraType == YUV420P_NV12) {
            mCameraRender.setRotate(mCameraId.equals(CAMERA_BACK) ? -90 : 90);
            mCameraRender.onDraw(mYBuffer, mUVBuffer);
        }
    }

    public void setCameraData(@Constant.CameraType String cameraId, byte[] data) {
        if (data != null && data.length > 0) {
            mCameraId = cameraId;

            System.arraycopy(data, 0, mYData, 0, mYData.length);
            System.arraycopy(data, mYData.length, mUVData, 0, mUVData.length);
//            FileHelper.getInstance().createFileWithByte(data, FileHelper.getInstance().getDataFolderPath(),System.currentTimeMillis()+"y_uv.yuv");

            mYBuffer.put(mYData).position(0);
            mUVBuffer.put(mUVData).position(0);
        }
    }

    public void setCameraDataType(@Constant.CameraData int cameraDataType) {
        this.mCameraType = cameraDataType;
    }
}
