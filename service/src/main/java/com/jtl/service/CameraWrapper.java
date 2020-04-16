package com.jtl.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 作者:jtl
 * 日期:Created in 2019/9/9 21:32
 * 描述:Camera2 的工具类
 * 更改:
 */
public class CameraWrapper implements ImageReader.OnImageAvailableListener {
    private static final String TAG = CameraWrapper.class.getSimpleName();
    private Context mContext;
    private volatile @Constant.CameraType
    String mCameraId;
    private int mWidth;
    private int mHeight;
    private boolean isAutoFocus;
    private byte[] mImageData = null;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraDataListener mCameraDataListener;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    // 彩色相机打开关闭锁
    private Semaphore mCameraSemaphore = new Semaphore(1);

    private Size[] mSizes;

    public CameraWrapper(Context context, int width, int height, boolean isAutoFocus, CameraDataListener cameraDataListener) {
        mContext = context;
        mWidth = width;
        mHeight = height;
        this.isAutoFocus = isAutoFocus;
        mCameraDataListener = cameraDataListener;

        mImageData = new byte[mWidth * mHeight * 3 / 2];
    }

    public static Size[] getSizes(Context context, String cameraId) {
        Size[] sizes = null;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            sizes = map.getOutputSizes(ImageFormat.YUV_420_888);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return sizes;
    }

    @SuppressLint("MissingPermission")
    public void openCamera(@Constant.CameraType final String cameraType) {
        if (mCameraDevice != null) {
            closeCamera();
        }
        startBackgroundThread();
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraType);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

            if (!mCameraSemaphore.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                return;
            }

            manager.openCamera(cameraType, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    startPreview(cameraType);

                    Log.w(TAG, "onOpened");
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                    mCameraSemaphore.release();
                    Log.w(TAG, "onDisconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                    mCameraSemaphore.release();
                    Log.w(TAG, "onError:error:" + error);
                }
            }, mBackgroundHandler);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.w(TAG, "onError:InterruptedException:" + e.getMessage());
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.w(TAG, "onError:CameraAccessException:" + e.getMessage());
        }
    }

    private void startPreview(final String cameraType) {
        mImageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.YUV_420_888, /*maxImages*/2);
        mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);
        List<Surface> surfaceList = new ArrayList<Surface>();
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface imageReaderSurface = mImageReader.getSurface();

            Surface surface = null;
            mPreviewRequestBuilder.addTarget(imageReaderSurface);

            if (surface == null) {
                surfaceList = Arrays.asList(imageReaderSurface);
            } else {
                surfaceList = Arrays.asList(surface, imageReaderSurface);
            }

            mCameraDevice.createCaptureSession(surfaceList,
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                return;
                            }

                            mCaptureSession = cameraCaptureSession;
                            try {
                                if (isAutoFocus) {
                                    // 自动对焦
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                } else {
                                    // 固定焦距
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                                    mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0f);
                                }
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                                mCameraId = cameraType;
                                mCameraSemaphore.release();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                Log.w(TAG, "onError:CameraAccessException:" + e.getMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.w(TAG, "onConfigureFailed");
                            mCameraSemaphore.release();
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.w(TAG, "onError:CameraAccessException:" + e.getMessage());
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        Log.w(TAG, "startBackgroundThread");
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
            Log.w(TAG, "stopBackgroundThread");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.w(TAG, "onError:InterruptedException:" + e.getMessage());
        }
    }

    public void closeCamera() {
        try {
            mCameraSemaphore.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock color camera closing.", e);
        } finally {
            stopBackgroundThread();
            mCameraSemaphore.release();
            Log.w(TAG, "closeCamera");
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        if (reader == null) {
            return;
        }
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uvBuffer = image.getPlanes()[1].getBuffer();
        yBuffer.position(0);
        uvBuffer.position(0);

        yBuffer.get(mImageData, 0, yBuffer.limit());
        uvBuffer.get(mImageData, yBuffer.limit(), uvBuffer.limit());
        if (mCameraDataListener != null) {
            mCameraDataListener.setCameraDataListener(mCameraId, getImageData(), image.getTimestamp(), image.getFormat());
        }

        image.close();
        Log.w(TAG, "onImageAvailable");
    }

    public @Constant.CameraType
    String getCameraId() {
        return mCameraId;
    }

    public Size[] getSizes() {
        return mSizes;
    }

    public byte[] getImageData() {
        byte[] data = new byte[mImageData.length];
        System.arraycopy(mImageData, 0, data, 0, data.length);

        return data;
    }

    public interface CameraDataListener {

        /**
         * @param imageData
         * @param timestamp
         * @param imageFormat
         * @see ImageFormat
         */
        void setCameraDataListener(@Constant.CameraType String mCameraId, byte[] imageData, float timestamp, int imageFormat);
    }
}
