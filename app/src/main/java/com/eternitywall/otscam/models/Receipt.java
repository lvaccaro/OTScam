package com.eternitywall.otscam.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Receipt {
    public long id;
    public String path;
    public byte[] ots;
    public byte[] hash;

    public static byte[] hexToBytes(String hex) {
        hex = hex.length()%2 != 0?"0"+hex:hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String resolveUri(Context context, Uri uri){
        Cursor cursor = context.getContentResolver().query(uri,
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("_data"));
    }
}
