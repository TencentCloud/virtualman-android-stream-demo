package com.tencent.virtualman_demo_app.view.blur;

import android.content.Context;
import android.graphics.Bitmap;

interface BlurImpl {

    boolean prepare(Context context, Bitmap buffer, float radius);

    void release();

    void blur(Bitmap input, Bitmap output);

}