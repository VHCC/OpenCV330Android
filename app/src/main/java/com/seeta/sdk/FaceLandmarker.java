package com.seeta.sdk;

import android.util.Log;

public class FaceLandmarker {
    static{
        Log.d("FaceLandmarker", " *** Load Ndk [SeetaFaceLandmarker600_java] *** ");
        System.loadLibrary("SeetaFaceLandmarker600_java");
    }

    public long impl = 0;
    private native void construct(SeetaModelSetting seeting);
    public FaceLandmarker(SeetaModelSetting setting){
        this.construct(setting);
    }

    public native void dispose();
    protected void finalize()throws Throwable{
        super.finalize();
        this.dispose();
    }

    public native int number();

    public native void mark(SeetaImageData imageData, SeetaRect seetaRect, SeetaPointF[] pointFS);

    public native void mark(SeetaImageData imageData, SeetaRect seetaRect, SeetaPointF[] pointFS, int[] masks);
}
