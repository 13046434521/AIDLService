package com.jtl.service;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作者:jtl
 * 日期:Created in 2019/9/9 16:31
 * 描述:常量类
 * 更改:
 */
public class Constant {
    public static final int RGB = 0;
    public static final int YUV420P_NV12 = 1; //YUV420P: NV12:IOS只有这一种模式。存储顺序是先存Y，再UV交替存储。YYYYUVUVUV
    public static final int YUV420P_NV21 = 2; //YUV420P: NV21:安卓的模式。存储顺序是先存Y，再VU交替存储。YYYYVUVUVU
    public static final int YUV420SP_YU12 = 3;//YUV420SP: I420:又叫YU12，安卓的模式。存储顺序是先存Y，再存U，最后存V。YYYYUUUVVV
    public static final int YUV420SP_YV12 = 4;//YUV420SP: YV12:存储顺序是先存Y，再存V，最后存U。YYYVVVUUU
    public static final int YUV_Y = 5;//YUV: Y分量

    public static final String CAMERA_BACK = "0";
    public static final String CAMERA_FRONT = "1";

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    public static @CameraType
    String CAMERA_TYPE = CAMERA_BACK;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @IntDef({RGB, YUV420P_NV12, YUV420P_NV21, YUV420SP_YU12, YUV420SP_YV12, YUV_Y})
    public @interface CameraData {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @StringDef({CAMERA_BACK, CAMERA_FRONT})
    public @interface CameraType {
    }
}
