package com.eternitywall.otscam.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class Receipt {
    public long id;
    public String path;
    @Nullable
    public byte[] ots;
    public byte[] hash;

    public static byte[] hexToBytes(final String hex) {
        final String hex_ = hex.length() % 2 != 0 ? "0" + hex : hex;
        final byte[] buffer = new byte[hex_.length() / 2];
        for (int i = 0; i < buffer.length; i++) {
            final int index = i * 2;
            final int v = Integer.parseInt(hex_.substring(index, index + 2), 16);
            buffer[i] = (byte) v;
        }
        return buffer;
    }

    public static String bytesToHex(final byte[] bytes){
        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public static String resolveUri(final Context context, final Uri uri){
        final Cursor cursor = context.getContentResolver().query(uri,
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("_data"));
    }
}
