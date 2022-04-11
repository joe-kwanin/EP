package com.example.vvuexampermitapp;

public class DeviceInfo {
    private static final int DEVMODEL_FUNCATION = 0;
    private static final int DEVMODEL_INTERFACE = 1;
    private static final int DEVMODEL_SENSOR = 2;
    private static final int DEVMODEL_DSP = 3;
    public int DeviceType = 0;
    public int SensorType = 0;
    public int Version = 0;
    public byte[] DeviceSN = new byte[8];
    private byte[] DeviceModel = new byte[4];

    public DeviceInfo() {
    }

    public void formBytes(byte[] data) {
        this.DeviceType = byteArrayToInt(data);
        this.SensorType = data[4];
        this.Version = data[5];
        System.arraycopy(data, 6, this.DeviceSN, 0, 8);
        System.arraycopy(data, 0, this.DeviceModel, 0, 4);
    }

    private static int byteArrayToInt(byte[] b) {
        return b[0] & 255 | (b[1] & 255) << 8 | (b[2] & 255) << 16 | (b[3] & 255) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{(byte)(a >> 24 & 255), (byte)(a >> 16 & 255), (byte)(a >> 8 & 255), (byte)(a & 255)};
    }

    public boolean IsSupportCard() {
        return this.DeviceModel[3] == 1;
    }

    public boolean IsSupportFlash() {
        if (this.DeviceModel[3] == 5) {
            return true;
        } else {
            return this.DeviceModel[3] == 6;
        }
    }

    public boolean IsSupportLcd() {
        return this.DeviceModel[3] == 6;
    }
}
