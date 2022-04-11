package com.example.vvuexampermitapp;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothReader {
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    public static final String DEVICE_NAME = "Bluetooth Reader";
    public static final String TOAST = "Hint";
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int MESSAGE_STATE_CHANGE = 113;
    public static final int MESSAGE_READ = 114;
    public static final int MESSAGE_WRITE = 115;
    public static final int MESSAGE_DEVICE_NAME = 116;
    public static final int MESSAGE_TOAST = 117;
    public static final int MESSAGE_TIMEOUT = 118;
    public static final int IMAGESIZE_152_200 = 15200;
    public static final int IMAGESIZE_256_288 = 36864;
    public static final int IMAGESIZE_256_360 = 46080;
    public static final int DEVTYPE_BFM = 16914177;
    public static final int DEVTYPE_IMD = 16913921;
    public static final int DEVTYPE_FPC = 16912641;
    public static final int DEVTYPE_OPT = 131585;
    public static final int DEVTYPE_BFMF = 84023041;
    public static final int DEVTYPE_IMDF = 84022785;
    public static final int DEVTYPE_FPCF = 84021505;
    public static final int DEVTYPE_BFML = 100800257;
    public static final int DEVTYPE_IMDL = 100800001;
    public static final int DEVTYPE_FPCL = 100798721;
    public static final byte CMD_PASSWORD = 1;
    public static final byte CMD_ENROLID = 2;
    public static final byte CMD_VERIFY = 3;
    public static final byte CMD_IDENTIFY = 4;
    public static final byte CMD_DELETEID = 5;
    public static final byte CMD_CLEARID = 6;
    public static final byte CMD_ENROLHOST = 7;
    public static final byte CMD_CAPTUREHOST = 8;
    public static final byte CMD_MATCH = 9;
    public static final byte CMD_GETSN = 16;
    public static final byte CMD_GETDEVTYPE = 17;
    public static final byte CMD_GETDEVINFO = 18;
    public static final byte CMD_SETDEVDELAY = 19;
    public static final byte CMD_GETBAT = 33;
    public static final byte CMD_GETVERSION = 34;
    public static final byte CMD_SHUTDOWNDEVICE = 35;
    public static final byte CMD_GETSTDIMAGE = 48;
    public static final byte CMD_GETSTDCHAR = 49;
    public static final byte CMD_GETRESIMAGE = 50;
    public static final byte CMD_GETRESCHAR = 51;
    public static final byte CMD_FLASHGETID = 80;
    public static final byte CMD_FLASHREAD = 81;
    public static final byte CMD_FLASHWRITE = 82;
    public static final byte CMD_CARD_GETSN = 64;
    public static final byte CMD_CARD_READ_DATA = 65;
    public static final byte CMD_CARD_WRITE_DATA = 66;
    public static final byte CMD_CARD_CARDSN = 67;
    public static final byte CMD_CARD_READ_SECTOR = 68;
    public static final byte CMD_CARD_WRITE_SECTOR = 69;
    public static final byte CMD_CARD_READ_BLOCK = 70;
    public static final byte CMD_CARD_WRITE_BLOCK = 71;
    public static final byte CMD_CARD_READ_VALUE = 72;
    public static final byte CMD_CARD_WRITE_VALUE = 73;
    private byte mDeviceCmd = 0;
    private byte[] mCmdData = new byte[102400];
    private int mCmdSize = 0;
    public byte[] mRefData = new byte[512];
    public int mRefSize = 0;
    public byte[] mMatData = new byte[512];
    public int mMatSize = 0;
    public byte[] mCardSn = new byte[8];
    public byte[] mCardReadData = new byte[4096];
    public int mCardReadSize = 0;
    public byte[] mCardWriteData = new byte[4096];
    public byte[] mCardWriteTemp = new byte[1024];
    public int mCardWriteSize = 0;
    public int mCardWritePos = 0;
    public byte[] mUpImage = new byte[73728];
    public int mUpImageSize = 0;
    public int mUpImageCount = 0;
    public int mUpImageTotal = 0;
    public byte[] mRefCoord = new byte[512];
    public byte[] mMatCoord = new byte[512];
    public byte[] mIsoData = new byte[378];
    public byte[] mBat = new byte[2];
    public byte[] mFlashID = new byte[8];
    public byte[] mVersion = new byte[8];
    public byte[] mFlashReadData = new byte['耀'];
    public int mFlashReadSize = 0;
    public byte[] mFlashWriteData = new byte['耀'];
    public byte[] mFlashWriteTemp = new byte[1024];
    public int mFlashWriteSize = 0;
    public int mFlashWritePos = 0;
    public boolean mbGetImgDat = false;
    public int mDevType = 0;
    public DeviceInfo mDeviceInfo = new DeviceInfo();
    private static final String NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Handler mHandler;
    private BluetoothReader.AcceptThread mAcceptThread;
    private BluetoothReader.ConnectThread mConnectThread;
    private BluetoothReader.ConnectedThread mConnectedThread;
    private int mState = 0;
    private InputStream mInStream;
    private OutputStream mOutStream;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothReader(Context context, Handler handler) {
        this.mHandler = handler;
    }

    private synchronized void setState(int state) {
        Log.d("BluetoothChatService", "setState() " + this.mState + " -> " + state);
        this.mState = state;
        this.mHandler.obtainMessage(113, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d("BluetoothChatService", "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mAcceptThread == null) {
            this.mAcceptThread = new BluetoothReader.AcceptThread();
            this.mAcceptThread.start();
        }

        this.setState(1);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d("BluetoothChatService", "connect to: " + device);
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectThread = new BluetoothReader.ConnectThread(device);
        this.mConnectThread.start();
        this.setState(2);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d("BluetoothChatService", "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }

        this.mConnectedThread = new BluetoothReader.ConnectedThread(socket);
        this.mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(116);
        Bundle bundle = new Bundle();
        bundle.putString("Bluetooth Reader", device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        SystemClock.sleep(1000L);
        this.setState(3);
        this.GetDeviceInfo();
    }

    public synchronized void stop() {
        Log.d("BluetoothChatService", "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }

        this.setState(0);
    }

    public void write(byte[] out) {
        BluetoothReader.ConnectedThread r;
        synchronized(this) {
            if (this.mState != 3) {
                return;
            }

            r = this.mConnectedThread;
        }

        r.write(out);
    }

    private void connectionFailed() {
        this.setState(1);
        Message msg = this.mHandler.obtainMessage(117);
        Bundle bundle = new Bundle();
        bundle.putString("Hint", "Unable to connect device");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        this.setState(1);
        Message msg = this.mHandler.obtainMessage(117);
        Bundle bundle = new Bundle();
        bundle.putString("Hint", "Device connection was lost");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    public String toHexString(byte[] data, int size) {
        StringBuffer buffer = new StringBuffer();

        for(int i = 0; i < size; ++i) {
            String s = Integer.toHexString(data[i] & 255);
            if (s.length() == 1) {
                buffer.append("0" + s);
            } else {
                buffer.append(s);
            }
        }

        return buffer.toString();
    }

    public boolean writestream(byte[] buffer) {
        boolean ret = false;
        if (this.mState == 3) {
            try {
                this.mOutStream.write(buffer);
                ret = true;
            } catch (IOException var4) {
                Log.e("BluetoothChatService", "Exception during write", var4);
            }
        }

        return ret;
    }

    public int readstream(byte[] buffer) {
        int bytes = 0;
        if (this.mState == 3) {
            try {
                bytes = this.mInStream.read(buffer);
            } catch (IOException var4) {
                Log.e("BluetoothChatService", "Exception during read", var4);
            }
        }

        return bytes;
    }

    private byte[] changeByte(int data) {
        byte b4 = (byte)(data >> 24);
        byte b3 = (byte)(data << 8 >> 24);
        byte b2 = (byte)(data << 16 >> 24);
        byte b1 = (byte)(data << 24 >> 24);
        byte[] bytes = new byte[]{b1, b2, b3, b4};
        return bytes;
    }

    private byte[] toBmpByte(int width, int height, byte[] data) {
        byte[] buffer = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            int bfType = 16973;
            int bfSize = 1078 + width * height;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            int bfOffBits = 1078;
            dos.writeShort(bfType);
            dos.write(this.changeByte(bfSize), 0, 4);
            dos.write(this.changeByte(bfReserved1), 0, 2);
            dos.write(this.changeByte(bfReserved2), 0, 2);
            dos.write(this.changeByte(bfOffBits), 0, 4);
            int biSize = 40;
            int biHeight = -height;
            int biPlanes = 1;
            int biBitcount = 8;
            int biCompression = 0;
            int biSizeImage = width * height;
            int biXPelsPerMeter = 0;
            int biYPelsPerMeter = 0;
            int biClrUsed = 256;
            int biClrImportant = 0;
            dos.write(this.changeByte(biSize), 0, 4);
            dos.write(this.changeByte(width), 0, 4);
            dos.write(this.changeByte(biHeight), 0, 4);
            dos.write(this.changeByte(biPlanes), 0, 2);
            dos.write(this.changeByte(biBitcount), 0, 2);
            dos.write(this.changeByte(biCompression), 0, 4);
            dos.write(this.changeByte(biSizeImage), 0, 4);
            dos.write(this.changeByte(biXPelsPerMeter), 0, 4);
            dos.write(this.changeByte(biYPelsPerMeter), 0, 4);
            dos.write(this.changeByte(biClrUsed), 0, 4);
            dos.write(this.changeByte(biClrImportant), 0, 4);
            byte[] palatte = new byte[1024];

            for(int i = 0; i < 256; ++i) {
                palatte[i * 4] = (byte)i;
                palatte[i * 4 + 1] = (byte)i;
                palatte[i * 4 + 2] = (byte)i;
                palatte[i * 4 + 3] = 0;
            }

            dos.write(palatte);
            dos.write(data);
            dos.flush();
            buffer = baos.toByteArray();
            dos.close();
            baos.close();
        } catch (Exception var25) {
            var25.printStackTrace();
        }

        return buffer;
    }

    public byte[] getFingerprintImage(byte[] data, int width, int height, int offset) {
        if (data == null) {
            return null;
        } else {
            byte[] imageData = new byte[width * height];

            for(int i = 0; i < width * height / 2; ++i) {
                imageData[i * 2] = (byte)(data[i + offset] & 240);
                imageData[i * 2 + 1] = (byte)(data[i + offset] << 4 & 240);
            }

            byte[] bmpData = this.toBmpByte(width, height, imageData);
            return bmpData;
        }
    }

    public void memcpy(byte[] dstbuf, int dstoffset, byte[] srcbuf, int srcoffset, int size) {
        for(int i = 0; i < size; ++i) {
            dstbuf[dstoffset + i] = srcbuf[srcoffset + i];
        }

    }

    private int calcCheckSum(byte[] buffer, int size) {
        int sum = 0;

        for(int i = 0; i < size; ++i) {
            sum += buffer[i];
        }

        return sum & 255;
    }

    private void SendCommand(byte cmdid, byte[] data, int size) {
        int sendsize = 9 + size;
        byte[] sendbuf = new byte[sendsize];
        sendbuf[0] = 70;
        sendbuf[1] = 84;
        sendbuf[2] = 0;
        sendbuf[3] = 0;
        sendbuf[4] = cmdid;
        sendbuf[5] = (byte)size;
        sendbuf[6] = (byte)(size >> 8);
        int i;
        if (size > 0) {
            for(i = 0; i < size; ++i) {
                sendbuf[7 + i] = data[i];
            }
        }

        i = this.calcCheckSum(sendbuf, 7 + size);
        sendbuf[7 + size] = (byte)i;
        sendbuf[8 + size] = (byte)(i >> 8);
        this.mDeviceCmd = cmdid;
        this.mCmdSize = 0;
        this.write(sendbuf);
    }

    private void SendCommandEx(byte cmdid, byte[] data, int size, byte flag) {
        int sendsize = 9 + size;
        byte[] sendbuf = new byte[sendsize];
        sendbuf[0] = 70;
        sendbuf[1] = 84;
        sendbuf[2] = 0;
        sendbuf[3] = flag;
        sendbuf[4] = cmdid;
        sendbuf[5] = (byte)size;
        sendbuf[6] = (byte)(size >> 8);
        int i;
        if (size > 0) {
            for(i = 0; i < size; ++i) {
                sendbuf[7 + i] = data[i];
            }
        }

        i = this.calcCheckSum(sendbuf, 7 + size);
        sendbuf[7 + size] = (byte)i;
        sendbuf[8 + size] = (byte)(i >> 8);
        this.mDeviceCmd = cmdid;
        this.mCmdSize = 0;
        this.write(sendbuf);
    }

    private void ReceiveCommand(byte[] databuf, int datasize) {
        if (this.mDeviceCmd == 48) {
            this.memcpy(this.mUpImage, this.mUpImageSize, databuf, 0, datasize);
            this.mUpImageSize += datasize;
            if (this.mUpImageSize >= this.mUpImageTotal) {
                this.mHandler.obtainMessage(48, 1, this.mUpImageTotal, this.mUpImage).sendToTarget();
            }
        } else if (this.mDeviceCmd == 50) {
            this.memcpy(this.mUpImage, this.mUpImageSize, databuf, 0, datasize);
            this.mUpImageSize += datasize;
            if (this.mUpImageSize >= this.mUpImageTotal) {
                this.mHandler.obtainMessage(50, 1, this.mUpImageTotal, this.mUpImage).sendToTarget();
                if (this.mbGetImgDat) {
                    this.GetResTemplate();
                    this.mbGetImgDat = false;
                }
            }
        } else {
            this.memcpy(this.mCmdData, this.mCmdSize, databuf, 0, datasize);
            this.mCmdSize += datasize;
            if (this.mCmdData[0] == 70 && this.mCmdData[1] == 84) {
                int mCmdTotal = (this.mCmdData[5] & 255) + (this.mCmdData[6] << 8 & '\uff00') + 9;
                Log.e("BluetoothChatService", "Size:" + String.valueOf(this.mCmdSize) + "/" + mCmdTotal);
                if (this.mCmdSize >= mCmdTotal) {
                    this.ProecessCmd(this.mCmdData, this.mCmdSize);
                    this.mCmdSize = 0;
                    boolean var4 = false;
                }
            }
        }

    }

    public static int byteArrayToInt(byte[] b) {
        return b[0] & 255 | (b[1] & 255) << 8 | (b[2] & 255) << 16 | (b[3] & 255) << 24;
    }

    public void ProecessCmd(byte[] cmddata, int cmdsize) {
        if (cmddata[0] == 70 && cmddata[1] == 84) {
            int size;
            byte[] tmpbuf;
            switch(cmddata[4]) {
                case 1:
                default:
                    break;
                case 2:
                    if (cmddata[7] == 1) {
                        size = (cmddata[8] & 255) + (byte)(cmddata[9] << 8 & '\uff00');
                        this.mHandler.obtainMessage(2, 1, size).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(2, 0, 0).sendToTarget();
                    }
                    break;
                case 3:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(3, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(3, 0, 0).sendToTarget();
                    }
                    break;
                case 4:
                    if (cmddata[7] == 1) {
                        size = cmddata[8] + (byte)(cmddata[9] << 8 & '\uff00');
                        this.mHandler.obtainMessage(4, 1, size).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(4, 0, 0).sendToTarget();
                    }
                    break;
                case 5:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(5, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(5, 0, 0).sendToTarget();
                    }
                    break;
                case 6:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(6, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(6, 0, 0).sendToTarget();
                    }
                    break;
                case 7:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        this.memcpy(this.mRefData, 0, cmddata, 8, size);
                        this.mRefSize = size;
                        this.mHandler.obtainMessage(7, 1, size, this.mRefData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(7, 0, 0).sendToTarget();
                    }
                    break;
                case 8:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        this.memcpy(this.mMatData, 0, cmddata, 8, size);
                        this.mMatSize = size;
                        this.mHandler.obtainMessage(8, 1, size, this.mMatData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(8, 0, 0).sendToTarget();
                    }
                    break;
                case 9:
                    if (cmddata[7] == 1) {
                        size = (byte)(cmddata[8] & 255) + (cmddata[9] << 8 & '\uff00');
                        this.mHandler.obtainMessage(9, 1, size).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(9, 0, 0).sendToTarget();
                    }
                    break;
                case 16:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        tmpbuf = new byte[32];
                        this.memcpy(tmpbuf, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(16, 1, size, tmpbuf).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(16, 0, 0).sendToTarget();
                    }
                    break;
                case 17:
                    size = cmddata[5] + (cmddata[6] << 8 & 240) - 1;
                    if (size > 0) {
                        tmpbuf = new byte[4];
                        this.memcpy(tmpbuf, 0, cmddata, 8, size);
                        this.mDevType = byteArrayToInt(tmpbuf);
                        this.mHandler.obtainMessage(17, 1, this.mDevType).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(17, 0, 0).sendToTarget();
                    }
                    break;
                case 18:
                    size = cmddata[5] + (cmddata[6] << 8 & 240) - 1;
                    if (size > 0) {
                        tmpbuf = new byte[32];
                        this.memcpy(tmpbuf, 0, cmddata, 8, size);
                        this.mDeviceInfo.formBytes(tmpbuf);
                        this.mDevType = this.mDeviceInfo.DeviceType;
                        this.mHandler.obtainMessage(18, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(18, 0, 0).sendToTarget();
                    }
                    break;
                case 33:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mBat, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(33, 1, size, this.mBat).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(33, 0, 0).sendToTarget();
                    }
                    break;
                case 34:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        ++size;
                        this.memcpy(this.mVersion, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(34, 1, size, this.mVersion).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(34, 0, 0).sendToTarget();
                    }
                    break;
                case 35:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(35, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(35, 0, 0).sendToTarget();
                    }
                    break;
                case 49:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        this.memcpy(this.mMatData, 0, cmddata, 8, size);
                        this.mMatSize = size;
                        this.mHandler.obtainMessage(49, 1, size, this.mMatData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(49, 0, 0).sendToTarget();
                    }
                    break;
                case 51:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        this.memcpy(this.mMatData, 0, cmddata, 8, size);
                        this.mMatSize = size;
                        this.mHandler.obtainMessage(51, 1, size, this.mMatData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(51, 0, 0).sendToTarget();
                    }
                    break;
                case 64:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & 240) - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardSn, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(64, 1, size, this.mCardSn).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(64, 0, 0).sendToTarget();
                    }
                    break;
                case 65:
                    size = (this.mCmdData[5] & 255) + (this.mCmdData[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardReadData, 0, cmddata, 8, size);
                        this.mCardReadSize = size;
                        this.mHandler.obtainMessage(65, 1, this.mCardReadSize, this.mCardReadData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(65, 0, 0).sendToTarget();
                    }
                    break;
                case 66:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(66, 1, 1).sendToTarget();
                    } else if (cmddata[7] == 2) {
                        this.mCardWritePos = 0;
                        System.arraycopy(this.mCardWriteData, this.mCardWritePos, this.mCardWriteTemp, 0, 512);
                        this.mCardWritePos += 512;
                        this.SendCommandEx((byte)66, this.mCardWriteTemp, 512, (byte)1);
                    } else if (cmddata[7] == 3) {
                        if (this.mCardWritePos >= this.mCardWriteSize) {
                            this.SendCommandEx((byte)66, (byte[])null, 0, (byte)2);
                        } else {
                            System.arraycopy(this.mCardWriteData, this.mCardWritePos, this.mCardWriteTemp, 0, 512);
                            this.mCardWritePos += 512;
                            this.SendCommandEx((byte)66, this.mCardWriteTemp, 512, (byte)1);
                        }
                    } else {
                        this.mHandler.obtainMessage(66, 0, 0).sendToTarget();
                    }
                    break;
                case 67:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardSn, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(67, 1, size, this.mCardSn).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(67, 0, 0).sendToTarget();
                    }
                    break;
                case 68:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardReadData, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(68, 1, size, this.mCardReadData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(68, 0, 0).sendToTarget();
                    }
                    break;
                case 69:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(69, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(69, 0, 0).sendToTarget();
                    }
                    break;
                case 70:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardReadData, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(70, 1, size, this.mCardReadData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(70, 0, 0).sendToTarget();
                    }
                    break;
                case 71:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(71, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(71, 0, 0).sendToTarget();
                    }
                    break;
                case 72:
                    size = (byte)(cmddata[5] & 255) + (cmddata[6] << 8 & '\uff00') - 1;
                    if (size > 0) {
                        this.memcpy(this.mCardReadData, 0, cmddata, 8, size);
                        int val = this.mCardReadData[0] & 255 | (this.mCardReadData[1] & 255) << 8 | (this.mCardReadData[2] & 255) << 16 | (this.mCardReadData[3] & 255) << 24;
                        this.mHandler.obtainMessage(72, 1, val, (Object)null).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(72, 0, 0).sendToTarget();
                    }
                    break;
                case 73:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(73, 1, 0).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(73, 0, 0).sendToTarget();
                    }
                    break;
                case 80:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        if (size > 8) {
                            size = 8;
                        }

                        this.memcpy(this.mFlashID, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(80, 1, size, this.mFlashID).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(80, 0, 0).sendToTarget();
                    }
                    break;
                case 81:
                    size = cmddata[5] + (cmddata[6] << 8 & '\uff00') - 1;
                    if (cmddata[7] == 1) {
                        this.mFlashReadSize = size;
                        this.memcpy(this.mFlashReadData, 0, cmddata, 8, size);
                        this.mHandler.obtainMessage(81, 1, this.mFlashReadSize, this.mFlashReadData).sendToTarget();
                    } else {
                        this.mHandler.obtainMessage(81, 0, 0).sendToTarget();
                    }
                    break;
                case 82:
                    if (cmddata[7] == 1) {
                        this.mHandler.obtainMessage(82, 1, 1).sendToTarget();
                    } else if (cmddata[7] == 2) {
                        this.mFlashWritePos = 0;
                        System.arraycopy(this.mFlashWriteData, this.mFlashWritePos, this.mFlashWriteTemp, 0, 512);
                        this.mFlashWritePos += 512;
                        this.SendCommandEx((byte)82, this.mFlashWriteTemp, 512, (byte)1);
                    } else if (cmddata[7] == 3) {
                        if (this.mFlashWritePos >= this.mFlashWriteSize) {
                            this.SendCommandEx((byte)82, (byte[])null, 0, (byte)2);
                        } else {
                            System.arraycopy(this.mFlashWriteData, this.mFlashWritePos, this.mFlashWriteTemp, 0, 512);
                            this.mFlashWritePos += 512;
                            this.SendCommandEx((byte)82, this.mFlashWriteTemp, 512, (byte)1);
                        }
                    } else {
                        this.mHandler.obtainMessage(82, 0, 0).sendToTarget();
                    }
            }
        }

    }

    public void EnrolToHost() {
        this.SendCommand((byte)7, (byte[])null, 0);
    }

    public void CaptureToHost() {
        this.SendCommand((byte)8, (byte[])null, 0);
    }

    public void MatchInDevice(byte[] mRefData, byte[] mMatData) {
        byte[] buf = new byte[1024];
        this.memcpy(buf, 0, mRefData, 0, 512);
        this.memcpy(buf, 512, mMatData, 0, 512);
        System.arraycopy(mRefData, 0, buf, 0, 512);
        System.arraycopy(mMatData, 0, buf, 512, 256);
        this.SendCommand((byte)9, buf, 1024);
    }

    public int InitMatch() {
        return FPMatch.getInstance().InitMatch(0);
    }

    public int MatchTemplate(byte[] mRefData, byte[] mMatData) {
        if (FPFormat.getInstance().GetDataType(mRefData) == FPFormat.STD_TEMPLATE) {
            return FPMatch.getInstance().MatchTemplate(mRefData, mMatData);
        } else {
            byte[] fa = new byte[512];
            byte[] fb = new byte[512];
            FPFormat.getInstance().AnsiIsoToStd(mRefData, fa, FPFormat.ISO_19794_2005);
            FPFormat.getInstance().AnsiIsoToStd(mMatData, fb, FPFormat.ISO_19794_2005);
            return FPMatch.getInstance().MatchTemplate(fa, fb);
        }
    }

    public void CardGetSN() {
        this.SendCommand((byte)64, (byte[])null, 0);
    }

    public void ReadCardData(byte[] cardsn, byte ctype, byte sector, byte[] pw, int size) {
        byte[] data = new byte[20];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = ctype;
        data[15] = sector;
        data[16] = (byte)(size & 255);
        data[17] = (byte)(size >> 8 & 255);
        this.SendCommand((byte)65, data, 18);
    }

    public void WriteCardData(byte[] cardsn, byte ctype, byte sector, byte[] pw, byte[] data, int size) {
        if (size <= 3072) {
            byte[] cmddat = new byte[1200];
            System.arraycopy(cardsn, 0, cmddat, 0, 8);
            System.arraycopy(pw, 0, cmddat, 8, 6);
            cmddat[14] = ctype;
            cmddat[15] = sector;
            cmddat[16] = (byte)(size & 255);
            cmddat[17] = (byte)(size >> 8 & 255);
            System.arraycopy(data, 0, cmddat, 18, size);
            this.SendCommand((byte)66, cmddat, size + 18);
        }
    }

    public void CardSerialNumber() {
        this.SendCommand((byte)67, (byte[])null, 0);
    }

    public void CardReadSector(byte[] cardsn, byte sector, byte[] pw) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = sector;
        this.SendCommand((byte)68, data, 15);
    }

    public void CardWriteSector(byte[] cardsn, byte sector, byte[] pw, byte[] dat) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = sector;
        System.arraycopy(dat, 0, data, 15, 48);
        this.SendCommand((byte)69, data, 63);
    }

    public void CardReadBlock(byte[] cardsn, byte block, byte[] pw) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = block;
        this.SendCommand((byte)70, data, 15);
    }

    public void CardWriteBlock(byte[] cardsn, byte block, byte[] pw, byte[] dat) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = block;
        System.arraycopy(dat, 0, data, 15, 16);
        this.SendCommand((byte)71, data, 31);
    }

    public void CardReadValue(byte[] cardsn, byte block, byte[] pw) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = block;
        this.SendCommand((byte)72, data, 15);
    }

    public void CardWriteValue(byte[] cardsn, byte block, byte[] pw, int val) {
        byte[] data = new byte[15];
        System.arraycopy(cardsn, 0, data, 0, 8);
        System.arraycopy(pw, 0, data, 8, 6);
        data[14] = block;
        byte[] bvs = new byte[]{(byte)(val >> 24 & 255), (byte)(val >> 16 & 255), (byte)(val >> 8 & 255), (byte)(val & 255)};
        System.arraycopy(bvs, 0, data, 15, 4);
        this.SendCommand((byte)73, (byte[])null, 0);
    }

    public void GetDeviceSn() {
        this.SendCommand((byte)16, (byte[])null, 0);
    }

    public void GetBatVal() {
        this.SendCommand((byte)33, (byte[])null, 0);
    }

    public void GetDeviceVer() {
        this.SendCommand((byte)34, (byte[])null, 0);
    }

    public void CloseDevvice() {
        this.SendCommand((byte)35, (byte[])null, 0);
    }

    public void GetImageTemplate(int imagesize) {
        this.mUpImageSize = 0;
        this.GetImageData(imagesize);
    }

    public void GetImageData(int imagesize) {
        this.mUpImageSize = 0;
        this.mUpImageTotal = imagesize;
        this.SendCommand((byte)48, (byte[])null, 0);
    }

    public void GetTemplate() {
        this.SendCommand((byte)49, (byte[])null, 0);
    }

    public void GetResImage() {
        this.mUpImageSize = 0;
        switch(this.mDevType) {
            case 131585:
                this.mUpImageTotal = 36864;
                break;
            case 16912641:
            case 84021505:
            case 100798721:
                this.mUpImageTotal = 15200;
                break;
            case 16913921:
            case 16914177:
            case 84022785:
            case 84023041:
            case 100800001:
            case 100800257:
                this.mUpImageTotal = 46080;
                this.mUpImageTotal = 46080;
                break;
            default:
                this.mUpImageTotal = 46080;
        }

        this.SendCommand((byte)50, (byte[])null, 0);
    }

    public void GetResTemplate() {
        this.SendCommand((byte)51, (byte[])null, 0);
    }

    public void GetImageAndDate() {
        this.mbGetImgDat = true;
        this.GetResImage();
    }

    public void EnrolInModule(int id) {
        byte[] buf = new byte[]{(byte)id, (byte)(id >> 8)};
        this.SendCommand((byte)2, buf, 2);
    }

    public void VerifyInModule(int id) {
        byte[] buf = new byte[]{(byte)id, (byte)(id >> 8)};
        this.SendCommand((byte)3, buf, 2);
    }

    public void SearchInModule() {
        this.SendCommand((byte)4, (byte[])null, 0);
    }

    public void DeleteInModule(int id) {
        byte[] buf = new byte[]{(byte)id, (byte)(id >> 8)};
        this.SendCommand((byte)5, buf, 2);
    }

    public void ClearModule() {
        this.SendCommand((byte)6, (byte[])null, 0);
    }

    public void GetDeviceType() {
        this.SendCommand((byte)17, (byte[])null, 0);
    }

    public void GetDeviceInfo() {
        this.SendCommand((byte)18, (byte[])null, 0);
    }

    public void GetFlashID() {
        this.SendCommand((byte)80, (byte[])null, 0);
    }

    public void ReadFlashData(int address, int size) {
        byte[] buf = new byte[]{(byte)(address & 255), (byte)(address >> 8 & 255), (byte)(address >> 16 & 255), (byte)(address >> 24 & 255), (byte)(size & 255), (byte)(size >> 8 & 255), (byte)(size >> 16 & 255), (byte)(size >> 24 & 255)};
        this.SendCommand((byte)81, buf, 8);
    }

    public void WriteFlashData(int address, int size, byte[] data) {
        byte[] buf = new byte[]{(byte)(address & 255), (byte)(address >> 8 & 255), (byte)(address >> 16 & 255), (byte)(address >> 24 & 255), (byte)(size & 255), (byte)(size >> 8 & 255), (byte)(size >> 16 & 255), (byte)(size >> 24 & 255)};
        this.mFlashWriteSize = size;
        System.arraycopy(data, 0, this.mFlashWriteData, 0, this.mFlashWriteSize);
        this.mFlashWritePos = 0;
        this.SendCommandEx((byte)82, buf, 8, (byte)0);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = BluetoothReader.this.mAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", BluetoothReader.MY_UUID);
            } catch (IOException var4) {
                Log.e("BluetoothChatService", "listen() failed", var4);
            }

            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d("BluetoothChatService", "BEGIN mAcceptThread" + this);
            this.setName("AcceptThread");
            BluetoothSocket socket = null;

            while(BluetoothReader.this.mState != 3) {
                try {
                    socket = this.mmServerSocket.accept();
                } catch (IOException var6) {
                    Log.e("BluetoothChatService", "accept() failed", var6);
                    break;
                }

                if (socket != null) {
                    synchronized(BluetoothReader.this) {
                        switch(BluetoothReader.this.mState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                } catch (IOException var4) {
                                    Log.e("BluetoothChatService", "Could not close unwanted socket", var4);
                                }
                                break;
                            case 1:
                            case 2:
                                BluetoothReader.this.connected(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }

            Log.i("BluetoothChatService", "END mAcceptThread");
        }

        public void cancel() {
            Log.d("BluetoothChatService", "cancel " + this);

            try {
                this.mmServerSocket.close();
            } catch (IOException var2) {
                Log.e("BluetoothChatService", "close() of server failed", var2);
            }

        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(BluetoothReader.MY_UUID);
            } catch (IOException var5) {
                Log.e("BluetoothChatService", "create() failed", var5);
            }

            this.mmSocket = tmp;
        }

        public void run() {
            Log.i("BluetoothChatService", "BEGIN mConnectThread");
            this.setName("ConnectThread");
            BluetoothReader.this.mAdapter.cancelDiscovery();

            try {
                this.mmSocket.connect();
            } catch (IOException var5) {
                BluetoothReader.this.connectionFailed();

                try {
                    this.mmSocket.close();
                } catch (IOException var3) {
                    Log.e("BluetoothChatService", "unable to close() socket during connection failure", var3);
                }

                BluetoothReader.this.start();
                return;
            }

            synchronized(BluetoothReader.this) {
                BluetoothReader.this.mConnectThread = null;
            }

            BluetoothReader.this.connected(this.mmSocket, this.mmDevice);
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e("BluetoothChatService", "close() of connect socket failed", var2);
            }

        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d("BluetoothChatService", "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException var6) {
                Log.e("BluetoothChatService", "temp sockets not created", var6);
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
            BluetoothReader.this.mInStream = tmpIn;
            BluetoothReader.this.mOutStream = tmpOut;
        }

        public void run() {
            Log.i("BluetoothChatService", "BEGIN mConnectedThread");
            byte[] buffer = new byte[256];

            while(true) {
                try {
                    int bytes = this.mmInStream.read(buffer);
                    BluetoothReader.this.ReceiveCommand(buffer, bytes);
                } catch (IOException var4) {
                    Log.e("BluetoothChatService", "disconnected", var4);
                    BluetoothReader.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
            } catch (IOException var3) {
                Log.e("BluetoothChatService", "Exception during write", var3);
            }

        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e("BluetoothChatService", "close() of connect socket failed", var2);
            }

        }
    }
}
