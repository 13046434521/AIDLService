package com.jtl.aidlservicedemo.camera;

import java.nio.ByteBuffer;

/**
 * 作者:jtl
 * 日期:Created in 2019/9/10 20:02
 * 描述:
 * 更改:
 */
public interface ICameraYUV {
    int rotate = -90;
    void onDraw(ByteBuffer yData, ByteBuffer uvData);

    void setRotate(int rotate);
}
