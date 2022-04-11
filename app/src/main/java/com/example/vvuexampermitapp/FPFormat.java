package com.example.vvuexampermitapp;

import android.util.Base64;

public class FPFormat {
    public static int STD_TEMPLATE = 0;
    public static int ANSI_378_2004 = 1;
    public static int ISO_19794_2005 = 2;
    public static int ISO_19794_2009 = 3;
    public static int ISO_19794_2011 = 4;
    public static int COORD_NOTCHANGE = 0;
    public static int COORD_MIRRORV = 1;
    public static int COORD_MIRRORH = 2;
    public static int COORD_ROTAING = 3;
    private static FPFormat mCom = null;

    static {
        System.loadLibrary("fpformat");
    }

    public FPFormat() {
    }

    public static FPFormat getInstance() {
        if (mCom == null) {
            mCom = new FPFormat();
        }

        return mCom;
    }

    public native int StdToAnsiIso(byte[] var1, byte[] var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public native int AnsiIsoToStd(byte[] var1, byte[] var2, int var3);

    public native int StdChangeCoord(byte[] var1, int var2, byte[] var3, int var4);

    public native int GetDataType(byte[] var1);

    public String ToAnsiIsoBase64(byte[] input, int type, int dk) {
        int dt = this.GetDataType(input);
        if (dt == STD_TEMPLATE) {
            byte[] output = new byte[512];
            byte[] tmpdat = new byte[512];
            this.StdChangeCoord(input, 256, tmpdat, dk);
            return this.StdToAnsiIso(tmpdat, output, 378, 260, 300, 199, 199, type) > 0 ? Base64.encodeToString(output, 0, 378, 0) : "";
        } else {
            return "";
        }
    }

    public byte[] ToAnsiIso(byte[] input, int type, int dk) {
        int dt = this.GetDataType(input);
        if (dt == STD_TEMPLATE) {
            byte[] output = new byte[512];
            byte[] tmpdat = new byte[512];
            this.StdChangeCoord(input, 256, tmpdat, dk);
            return this.StdToAnsiIso(tmpdat, output, 378, 260, 300, 199, 199, type) > 0 ? output : null;
        } else {
            return null;
        }
    }

    public String To_Ansi378_2004_Base64(byte[] input) {
        return this.ToAnsiIsoBase64(input, ANSI_378_2004, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2005_Base64(byte[] input) {
        return this.ToAnsiIsoBase64(input, ISO_19794_2005, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2009_Base64(byte[] input) {
        return this.ToAnsiIsoBase64(input, ISO_19794_2009, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2011_Base64(byte[] input) {
        return this.ToAnsiIsoBase64(input, ISO_19794_2011, COORD_NOTCHANGE);
    }

    public byte[] To_Ansi378_2004(byte[] input) {
        return this.ToAnsiIso(input, ANSI_378_2004, COORD_NOTCHANGE);
    }

    public byte[] To_Iso19794_2005(byte[] input) {
        return this.ToAnsiIso(input, ISO_19794_2005, COORD_NOTCHANGE);
    }

    public byte[] To_Iso19794_2009(byte[] input) {
        return this.ToAnsiIso(input, ISO_19794_2009, COORD_NOTCHANGE);
    }

    public byte[] To_Iso19794_2011(byte[] input) {
        return this.ToAnsiIso(input, ISO_19794_2011, COORD_NOTCHANGE);
    }
}
