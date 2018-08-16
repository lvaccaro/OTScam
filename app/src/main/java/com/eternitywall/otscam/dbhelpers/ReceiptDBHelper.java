package com.eternitywall.otscam.dbhelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eternitywall.otscam.models.Receipt;

import java.util.ArrayList;
import java.util.List;

public class ReceiptDBHelper extends DBHelper {


    private ReceiptDBHelper(final Context context) {
        super(context);
    }

    public static ReceiptDBHelper createReceiptDBHelper(final Context context) {
        return new ReceiptDBHelper(context);
    }

    public long create(final Receipt receipt) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(KEY_PATH, receipt.path);
        if (receipt.hash != null)
            values.put(KEY_HASH, Receipt.bytesToHex(receipt.hash));
        if (receipt.ots != null)
            values.put(KEY_OTS, receipt.ots);
        // insert row
        return db.insert(TABLE_RECEIPTS, null, values);
    }

    public Receipt get(final long id) {
        final SQLiteDatabase db = getReadableDatabase();
        final String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE "
                + KEY_ID + " = " + id;
        final Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
            c.moveToFirst();
        final Receipt receipt = new Receipt();
        receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
        receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
        receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
        receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
        c.close();
        return receipt;
    }

    public Receipt getByHash(final byte[] hash) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE "
                + KEY_HASH + " = '" + Receipt.bytesToHex(hash) + "'";
        final Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
            c.moveToFirst();
        final Receipt receipt = new Receipt();
        receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
        receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
        receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
        receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
        c.close();
        return receipt;
    }

    public List<Receipt> getAll() {
        final List<Receipt> folders = new ArrayList<Receipt>();
        final String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS;
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst())
            do {
                final Receipt receipt = new Receipt();
                receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
                receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
                receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
                receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
                folders.add(receipt);
            } while (c.moveToNext());
        c.close();
        return folders;
    }

    public List<Receipt> getAllNullable() {
        final List<Receipt> folders = new ArrayList<Receipt>();
        final String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE " + KEY_OTS + " IS NULL OR " + KEY_HASH + " IS NULL";
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst())
            do {
                final Receipt receipt = new Receipt();
                receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
                receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
                receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
                receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
                folders.add(receipt);
            } while (c.moveToNext());
        c.close();
        return folders;
    }

    public int update(final Receipt receipt) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(KEY_PATH, receipt.path);
        values.put(KEY_HASH, Receipt.bytesToHex(receipt.hash));
        if (receipt.ots != null)
            values.put(KEY_OTS, receipt.ots);
        // updating row
        return db.update(TABLE_RECEIPTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(receipt.id) });
    }
}
