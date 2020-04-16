package com.jtl.aidlservicedemo.base;

import android.content.Context;

/**
 * 作者:jtl
 * 日期:Created in 2019/8/27 15:12
 * 描述:
 * 更改:
 */
public abstract class BaseRender {
    protected abstract void createdGLThread(Context context);

    protected abstract void onSurfaceChanged(float width, float height);

    protected abstract void onUpdate(float[] data);
    protected abstract void onDraw();
}
