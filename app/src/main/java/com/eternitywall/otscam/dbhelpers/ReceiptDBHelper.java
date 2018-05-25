package com.eternitywall.otscam.dbhelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eternitywall.otscam.models.Receipt;

import java.util.ArrayList;
import java.util.List;

public class ReceiptDBHelper extends DBHelper {


    public ReceiptDBHelper(Context context) {
        super(context);
    }

    public long create(Receipt receipt) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PATH, receipt.path);
        values.put(KEY_HASH, Receipt.bytesToHex(receipt.hash));
        if(receipt.ots!=null) {
            values.put(KEY_OTS, receipt.ots);
        }

        // insert row
        long id = db.insert(TABLE_RECEIPTS, null, values);
        return id;
    }

    public Receipt get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE "
                + KEY_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Receipt receipt = new Receipt();
        receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
        receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
        receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
        receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));

        c.close();
        return receipt;
    }

    public Receipt getByHash(byte[] hash) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE "
                + KEY_HASH + " = '" + Receipt.bytesToHex(hash) + "'";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Receipt receipt = new Receipt();
        receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
        receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
        receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
        receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));

        c.close();
        return receipt;
    }

    public List<Receipt> getAll() {
        List<Receipt> folders = new ArrayList<Receipt>();
        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Receipt receipt = new Receipt();
                receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
                receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
                receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
                receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
                folders.add(receipt);
            } while (c.moveToNext());
        }

        c.close();
        return folders;
    }

    public List<Receipt> getAllNullable() {
        List<Receipt> folders = new ArrayList<Receipt>();
        String selectQuery = "SELECT  * FROM " + TABLE_RECEIPTS + " WHERE " + KEY_OTS + " IS NULL";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Receipt receipt = new Receipt();
                receipt.id = c.getInt(c.getColumnIndex(KEY_ID));
                receipt.path = c.getString(c.getColumnIndex(KEY_PATH));
                receipt.ots = c.getBlob(c.getColumnIndex(KEY_OTS));
                receipt.hash = Receipt.hexToBytes(c.getString(c.getColumnIndex(KEY_HASH)));
                folders.add(receipt);
            } while (c.moveToNext());
        }

        c.close();
        return folders;
    }

    public int update(Receipt receipt) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PATH, receipt.path);
        values.put(KEY_HASH, Receipt.bytesToHex(receipt.hash));
        if(receipt.ots!=null) {
            values.put(KEY_OTS, receipt.ots);
        }

        // updating row
        return db.update(TABLE_RECEIPTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(receipt.id) });
    }
}
