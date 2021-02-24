package acl.siot.opencvwpc20191007noc.rfid;

import android.serialport.SerialPort;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

//import android_serialport_api.Helper;
//import android_serialport_api.SerialPort;

/**
 * The access API of serial port.
 * It's cloning based on jogtek, {@link android_serialport_api.SerialPortActivity}
 * Created by Tm.Shih on 2017/8/11.
 */
public abstract class SerialPortConsole {
    public enum BOUD_RATE {
        BR_115200(115200);

        private final int bits;

        private BOUD_RATE(int boudRate) {
            bits = boudRate;
        }
    }

    protected String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String Uid = "";
    private int head = 0;
    private String Data = "";
    private int head2 = 0;
    private String SRIX4K_KEY = "";

    protected SerialPortConsole(String path, BOUD_RATE baud) throws SecurityException, IOException, InvalidParameterException {
        /* Create and open the serial port. */
        mSerialPort = new SerialPort(new File(path), baud.bits);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    protected void closeSerialPort() {
        if (mReadThread != null) {
            mReadThread.interrupt();

            try {
                Thread.sleep(100L);
                mReadThread = null;
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void initValue() {
        this.Uid = "";
        this.head = 0;
        this.Data = "";
        this.head2 = 0;
    }

    public String retrieve_AUID(String back_string) {
        int head0;
        int tail0;
        String[] Uid2;
        if(this.head == 1) {
            head0 = back_string.indexOf("(");
            if(head0 >= 0) {
                this.head = 1;
                this.Uid = "";
                tail0 = back_string.indexOf(")");
                if(tail0 > 0) {
                    this.Uid = this.Uid + back_string.substring(head0 + 1, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string.substring(head0 + 1);
                }
            } else {
                tail0 = back_string.indexOf(")");
                if(tail0 >= 0) {
                    this.Uid = this.Uid + back_string.substring(0, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string;
                }
            }
        } else {
            head0 = back_string.indexOf("(");
            if(head0 >= 0) {
                this.head = 1;
                this.Uid = "";
                tail0 = back_string.indexOf(")");
                if(tail0 > 0) {
                    this.Uid = this.Uid + back_string.substring(head0 + 1, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string.substring(head0 + 1);
                }
            }
        }

        return "";
    }

    public String retrieve_UID(String back_string) {
        int head0;
        int tail0;
        String[] Uid2;
        if(this.head == 1) {
            head0 = back_string.indexOf("[");
            if(head0 >= 0) {
                this.head = 1;
                this.Uid = "";
                tail0 = back_string.indexOf("]");
                if(tail0 > 0) {
                    this.Uid = this.Uid + back_string.substring(head0 + 1, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string.substring(head0 + 1);
                }
            } else {
                tail0 = back_string.indexOf("]");
                if(tail0 >= 0) {
                    this.Uid = this.Uid + back_string.substring(0, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string;
                }
            }
        } else {
            head0 = back_string.indexOf("[");
            if(head0 >= 0) {
                this.head = 1;
                this.Uid = "";
                tail0 = back_string.indexOf("]");
                if(tail0 > 0) {
                    this.Uid = this.Uid + back_string.substring(head0 + 1, tail0);
                    if(!this.Uid.contains("M")) {
                        Uid2 = this.Uid.split(",");
                        return Uid2[0];
                    }

                    this.Uid = "";
                    this.head = 0;
                } else {
                    this.Uid = this.Uid + back_string.substring(head0 + 1);
                }
            }
        }

        return "";
    }

    public String retrieve_Data(String back_string) {
        int head0;
        if(this.head2 == 1) {
            head0 = back_string.indexOf("]");
            if(head0 != -1) {
                this.Data = this.Data + back_string.substring(0, head0);
                this.head2 = 0;
                String[] tail0 = this.Data.split(",");
                return tail0[0];
            }

            this.Data = this.Data + back_string;
        } else {
            head0 = back_string.indexOf("[");
            if(head0 != -1) {
                this.head2 = 1;
                this.Data = "";
                int tail01 = back_string.indexOf("]");
                if(tail01 != -1) {
                    this.Data = this.Data + back_string.substring(head0 + 1, tail01);
                    this.head2 = 0;
                    String[] Data2 = this.Data.split(",");
                    return Data2[0];
                }

                this.Data = this.Data + back_string.substring(head0 + 1);
            }
        }

        return "";
    }

    protected void GetFirmwareVersion() {
        try {
            this.initValue();
            this.send_command("0108000304FF0000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void WriteS50(String Block, String Data, String uuid, String kkey) {
        if(Data.length() != 32) {
            while(Data.length() < 32) {
                Data = Data + "0";
            }
        }

        if(uuid.length() == 10) {
            if(kkey.length() == 12) {
                if(Block.length() == 2) {
                    try {
                        this.initValue();
                        this.send_command("010A0003041850000000");
                        Thread.sleep(100L);
                        this.send_command("010D000304A2" + uuid + "0000");
                        Thread.sleep(100L);
                        this.send_command("010E000304C0" + kkey + "0000");
                        Thread.sleep(100L);
                        this.send_command("010F000304C160" + Block + uuid + "0000");
                        Thread.sleep(100L);
                        this.send_command("010C000304C23D6E98990000");
                        Thread.sleep(100L);
                        this.send_command("010A000304C8A0" + Block + "0000");
                        Thread.sleep(100L);
                        this.send_command("0118000304C8" + Data + "0000");
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.toString());
                    }

                }
            }
        }
    }

    public void ReadS50(String Block, String uuid, String kkey) {
        if(Block.length() == 2) {
            if(uuid.length() == 10) {
                if(kkey.length() == 12) {
                    try {
                        this.initValue();
                        this.send_command("010A0003041000200000");
                        Thread.sleep(100L);
                        this.send_command("010A0003041850000000");
                        Thread.sleep(100L);
                        this.send_command("010D000304A2" + uuid + "0000");
                        Thread.sleep(100L);
                        this.send_command("010E000304C0" + kkey + "0000");
                        Thread.sleep(100L);
                        this.send_command("010F000304C160" + Block + uuid + "0000");
                        Thread.sleep(100L);
                        this.send_command("010C000304C23D6E98990000");
                        Thread.sleep(100L);
                        this.send_command("010A000304C830" + Block + "0000");
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.toString());
                    }

                }
            }
        }
    }

    public void Read14443b(String Block, String UID) {
        if(this.SRIX4K_KEY.length() == 2) {
            if(Block.length() == 2) {
                try {
                    this.initValue();
                    this.send_command("010A0003041808" + Block + "0000");
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.toString());
                }

            }
        }
    }

    public void Write14443b(String Block, String Data, String UID) {
        if(Data.length() != 8) {
            while(Data.length() < 8) {
                Data = Data + "0";
            }
        }

        if(Block.length() == 2) {
            try {
                this.initValue();
                this.send_command("010E0003041809" + Block + Data + "0000");
                Thread.sleep(100L);
                this.send_command("010A0003041808" + Block + "0000");
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    public void ReadFelica(String Block, String UID) {
        if(UID.length() == 16) {
            if(Block.length() == 2) {
                try {
                    this.initValue();
                    this.send_command("0118000304181006" + UID + "0109000180" + Block + "0000");
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.toString());
                }

            }
        }
    }

    public void WriteFelica(String Block, String Data, String UID) {
        if(UID.length() == 16) {
            if(Block.length() == 2) {
                if(Data.length() != 32) {
                    while(Data.length() < 32) {
                        Data = Data + "0";
                    }
                }

                try {
                    this.initValue();
                    this.send_command("0128000304182008" + UID + "0109000180" + Block + Data + "0000");
                    Thread.sleep(100L);
                    this.send_command("0118000304181006" + UID + "0109000180" + Block + "0000");
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.toString());
                }

            }
        }
    }

    public void Get14443bUID(String KEY) {
        if(KEY.length() == 2) {
            try {
                this.initValue();
                this.SRIX4K_KEY = KEY;
                this.send_command("010A000304180E" + KEY + "0000");
                Thread.sleep(20L);
                this.send_command("0109000304180B0000");
                Thread.sleep(80L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    public void Get14443bKEY() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(30L);
            this.send_command("010C000304100021010C0000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("010A0003041806000000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void GetS50UID() {
        try {
            this.initValue();
            this.send_command("0108000304FD0000");
            Thread.sleep(30L);
            this.send_command("010A0003041000010000");
            Thread.sleep(30L);
            this.send_command("010C00030410002101080000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("0109000304A0010000");
            Thread.sleep(100L);
            this.initValue();
            this.send_command("0108000304FD0000");
            Thread.sleep(30L);
            this.send_command("010A0003041000010000");
            Thread.sleep(30L);
            this.send_command("010C00030410002101080000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("0109000304A0010000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void GetNtagUID() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(30L);
            this.send_command("010C00030410002101090000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("0109000304A0010000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void Get15693UID() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(30L);
            this.send_command("010C00030410002101000000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("010B000304142401000000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void GetFelicaUID() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(30L);
            this.send_command("010C000304100021011A0000");
            Thread.sleep(30L);
            this.send_command("0109000304F0000000");
            Thread.sleep(30L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(30L);
            this.send_command("010E000304180600FFFF00000000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void Polling14443b() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(20L);
            this.send_command("010C000304100021010C0000");
            Thread.sleep(20L);
            this.send_command("0109000304F0000000");
            Thread.sleep(20L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(20L);
            this.send_command("010A0003041806000000");
            Thread.sleep(80L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void PollingS50() {
        try {
            this.initValue();
            this.send_command("0108000304FD0000");
            Thread.sleep(20L);
            this.send_command("010A0003041000010000");
            Thread.sleep(20L);
            this.send_command("010C00030410002101080000");
            Thread.sleep(20L);
            this.send_command("0109000304F0000000");
            Thread.sleep(20L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(20L);
            this.send_command("0109000304A0010000");
            Thread.sleep(80L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void PollingNtag() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(20L);
            this.send_command("010C00030410002101090000");
            Thread.sleep(20L);
            this.send_command("0109000304F0000000");
            Thread.sleep(20L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(20L);
            this.send_command("0109000304A0010000");
            Thread.sleep(80L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void Check14443aUID(String UID) {
        if(UID.length() >= 8) {
            try {
                this.initValue();
                boolean e = false;
                String UIDlenStr = "";
                int e1 = UID.length() / 2 + 8;
                UIDlenStr = Byte.toString((byte)e1);;
                if(UIDlenStr.length() < 2) {
                    return;
                }

                if(UIDlenStr.length() > 2) {
                    UIDlenStr = UIDlenStr.substring(0, 2);
                }

                this.send_command("010A0003041850000000");
                Thread.sleep(100L);
                this.send_command("01" + UIDlenStr + "000304A2" + UID + "0000");
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    public void Polling15693() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(20L);
            this.send_command("010C00030410002101000000");
            Thread.sleep(20L);
            this.send_command("0109000304F0000000");
            Thread.sleep(20L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(20L);
            this.send_command("010B000304142401000000");
            Thread.sleep(80L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void PollingFelica() {
        try {
            this.initValue();
            this.send_command("0108000304FC0000");
            Thread.sleep(100L);
            this.send_command("010C000304100021011A0000");
            Thread.sleep(100L);
            this.send_command("0109000304F0000000");
            Thread.sleep(100L);
            this.send_command("0109000304F1FF0000");
            Thread.sleep(100L);
            this.send_command("010900030444000000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void ReadNtag(String Block) {
        if(Block.length() == 2) {
            try {
                this.initValue();
                this.send_command("010A0003041830" + Block + "0000");
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    public void Read15693(String flag, String Block) {
        try {
            this.initValue();
            this.send_command("010B00030418" + flag + "20" + Block + "0000");
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Log.w(TAG, e.toString());
        }

    }

    public void Write15693(String flag, String Block, String Data) {
        if(Data.length() != 8) {
            while(Data.length() < 8) {
                Data = Data + "0";
            }
        }

        if(flag.length() == 2) {
            try {
                this.initValue();
                this.send_command("010F00030418" + flag + "21" + Block + Data + "0000");
                Thread.sleep(100L);
                this.send_command("010B00030418" + flag + "20" + Block + "0000");
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    public void WriteNtag(String Block, String Data) {
        if(Data.length() != 8) {
            while(Data.length() < 8) {
                Data = Data + "0";
            }
        }

        if(Block.length() == 2) {
            try {
                this.initValue();
                this.send_command("010E00030418A2" + Block + Data + "0000");
                Thread.sleep(100L);
                this.send_command("010A0003041830" + Block + "0000");
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }

        }
    }

    protected void send_command(CharSequence t) {
        try {
            this.mOutputStream.write(t.toString().getBytes());
            this.onSendData(t.toString());
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        }
    }

    public void Send_P2P(byte[] b) {
        try {
            this.mOutputStream.write(b);
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        }
    }

//    public byte[] convert2P2P(String sendStr) {
//        byte[] buffer = null;
//        String bb = "";
//        int len = sendStr.length();
//        int start = 0;
//
//        for(int j = 238; len > 0; len = 0) {
//            if(j > len) {
//                j = len;
//            }
//
//            bb = sendStr.substring(start, j);
//            byte[] cc = bb.getBytes();
//            byte b2 = (byte)(bb.length() + 3);
//            byte b1 = (byte)(b2 + 4);
//            String dd = ByteArrayToHexString(cc);
//            String cmd = "fe" + ByteArrayToHexString(b1) + "d101" + ByteArrayToHexString(b2) + "5402656e" + dd;
//            cmd = cmd.replace(" ", "");
//            buffer = new byte[cmd.length() / 2 + 1];
//            buffer[0] = 101;
//
//            for(int i = 0; i < cmd.length(); i += 2) {
//                String ff = cmd.substring(i, i + 2);
//                byte gg = Helper.ConvertStringToHexByte(ff);
//                buffer[i / 2 + 1] = gg;
//            }
//
//            start += j;
//            int var10000 = len - j;
//        }
//
//        return buffer;
//    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


    public void p2p_on() {
        this.send_command("0108000304EE0000");
    }

    public void p2p_off() {
        this.send_command("0108000304EF0000");
    }

    public void stop_polling_uid() {
        this.send_command("0108000304FF0000");
    }

    public void start_polling_uid() {
        this.send_command("0108000304CA0000");
    }

    public void cea_on(String UID) {
        this.send_command("010D000304CE00" + UID + "0000");
    }

    public void ceb_on(String UID) {
        this.send_command("010D000304CE01" + UID + "0000");
    }

    public void ce_off() {
        this.send_command("0108000304CF0000");
    }

    protected abstract void onDataReceived(byte[] data, int size);

    protected abstract void onSendData(String data);

    private class ReadThread extends Thread {
        public ReadThread() {
        }

        public void run() {
            super.run();

            while (!isInterrupted()) {
                try {
                    byte[] e = new byte[64];
                    if (mInputStream == null) {
                        return;
                    }

                    int size = mInputStream.read(e);
                    if (size > 0) {
                        onDataReceived(e, size);
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.toString());
                    return;
                }
            }

        }
    }
}
