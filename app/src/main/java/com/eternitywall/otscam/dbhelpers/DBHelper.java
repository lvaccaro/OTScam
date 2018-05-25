package com.eternitywall.otscam.dbhelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    // Database
    protected static final String DATABASE_NAME = "opentimestamps.db";
    protected static final int DATABASE_VERSION = 4;

    // Table Names
    protected static final String TABLE_RECEIPTS = "receipts";

    // Column names
    protected static final String KEY_ID = "id";
    protected static final String KEY_PATH = "name";
    protected static final String KEY_HASH = "hash";
    protected static final String KEY_OTS = "ots";


    // table create statement
    protected static final String SQL_CREATE_RECEIPTS= "CREATE TABLE " + TABLE_RECEIPTS + " (" +
            " " + KEY_ID + " INTEGER PRIMARY KEY," +
            " " + KEY_PATH + " TEXT NOT NULL," +
            " " + KEY_OTS + " BLOB, " +
            " " + KEY_HASH + " VARCHAR(64) NOT NULL)";

    // table delete statement
    protected static final String SQL_DELETE_RECEIPTS = "DROP TABLE IF EXISTS " + TABLE_RECEIPTS + " ";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECEIPTS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_RECEIPTS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_RECEIPTS);
        db.execSQL(SQL_CREATE_RECEIPTS);
    }

}