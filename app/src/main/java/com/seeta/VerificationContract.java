package com.seeta;

import android.graphics.Bitmap;
import android.view.TextureView;

import org.opencv.core.Rect;

public interface VerificationContract {

    interface View {

        void drawFaceRect(Rect faceRect);

        void drawTestMat(Bitmap src);

        void toastMessage(String msg);

        void showCameraUnavailableDialog(int errorCode);

//        void setStatus(FaceAntiSpoofing.Status status, Mat matBgr, Rect faceRect);

        void setPresenter(Presenter presenter);

        TextureView getTextureView();
        boolean isActive();
    }

    interface Presenter {

        void detect(byte[] data, int width, int height, int rotation);

        void destroy();

    }
}
