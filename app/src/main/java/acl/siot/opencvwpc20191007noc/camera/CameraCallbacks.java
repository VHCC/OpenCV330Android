package acl.siot.opencvwpc20191007noc.camera;

import android.hardware.Camera;

@SuppressWarnings("deprecation")
public interface CameraCallbacks extends Camera.PreviewCallback{

    void onCameraUnavailable(int errorCode);
}
