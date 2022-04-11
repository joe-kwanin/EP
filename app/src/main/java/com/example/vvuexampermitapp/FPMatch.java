package com.example.vvuexampermitapp;

public class FPMatch {
    private static FPMatch mMatch = null;

    static {
        System.loadLibrary("fpalgo");
        System.loadLibrary("fpcore");
    }

    public FPMatch() {
    }

    public static FPMatch getInstance() {
        if (mMatch == null) {
            mMatch = new FPMatch();
        }

        return mMatch;
    }

    public native int InitMatch(int var1);

    public native int CreateTemplate(byte[] var1, int var2, int var3, byte[] var4);

    public native int MatchTemplate(byte[] var1, byte[] var2);
}
