package com.yiwise.voip.vad;

public class AudioHandleUtils {


    public static short[] getShorts(byte[] b) {
        return getShorts(b, b.length);
    }

    public static short[] getShorts(byte[] b, int len) {
        short[] s = new short[len >> 1];
        for (int i = 0; i < len; i += 2) {
            s[i >> 1] = (short) (((b[i + 1] & 0x00FF) << 8) | (0x00FF & b[i]));
        }
        return s;
    }

    public static byte[] getBytes(short[] s) {
        if (s == null) {
            return new byte[0];
        } else {
            int nLen = s.length;
            if (nLen == 0) {
                return new byte[0];
            } else {
                byte[] buf = new byte[nLen + nLen];
                int nIndex = 0;

                for (short index : s) {
                    buf[nIndex++] = (byte) (index & 255);
                    buf[nIndex++] = (byte) (index >> 8 & 255);
                }
                return buf;
            }
        }
    }

}