package acl.siot.opencvwpc20191007noc.util;

import com.potterhsu.Pinger;

import java.io.IOException;

public class VMSPinger {

    public static boolean isRunning = false;
    private Runtime runtime = Runtime.getRuntime();
    private OnPingListener onPingListener;

    public void setOnPingListener(OnPingListener onPingListener) {
        this.onPingListener = onPingListener;
    }

    public boolean ping(String destination, int timeoutInSeconds) throws InterruptedException {
        try {
            String command = String.format("/system/bin/ping -c 3 -W %d %s", timeoutInSeconds, destination);
            Process process = this.runtime.exec(command);
            int ret = process.waitFor();
            process.destroy();
            return ret == 0;
        } catch (IOException var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public void pingUntilSucceeded(final String destination, final long intervalInMillis) {
        if (isRunning) return;
        isRunning = true;
        Thread pingThread = new Thread() {
            public void run() {
                super.run();
                isRunning = true;
                try {
                    for(; !VMSPinger.this.ping(destination, 3); Thread.sleep(intervalInMillis)) {
                        if (onPingListener != null) {
                            onPingListener.onPingFailure();
                        }
                    }

                    if (onPingListener != null) {
                        onPingListener.onPingSuccess();
                    }
                } catch (InterruptedException var2) {
                    var2.printStackTrace();
                }
            }
        };
        pingThread.start();
    }

    public void pingUntilFailed(final String destination, final long intervalInMillis) {
        if (isRunning) return;
        isRunning = true;
        Thread pingThread = new Thread() {
            public void run() {
                super.run();

                try {
                    for(; VMSPinger.this.ping(destination, 3); Thread.sleep(intervalInMillis)) {
                        if (onPingListener != null) {
                            onPingListener.onPingSuccess();
                        }
                    }

                    if (onPingListener != null) {
                        onPingListener.onPingFailure();
                    }
                } catch (InterruptedException var2) {
                    var2.printStackTrace();
                }

//                if (VMSPinger.this.onPingListener != null) {
//                    VMSPinger.this.onPingListener.onPingFinish();
//                }
            }
        };
        pingThread.start();
    }

    public interface OnPingListener {
        void onPingSuccess();

        void onPingFailure();
    }
}
