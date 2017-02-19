package com.cmst.cache.provider;

import android.database.sqlite.SQLiteDatabase;

/**
 * This is the table that contains the details of each individual photo across all albums.
 * 
 * @author Shobha Deepak
 * 
 */

public class SyncStatusTable {

  // Table name
  public static final String TABLE_SYNC_STATUS = "syncstatus";

  // table fields
  public static final String _ID = "_id";// auto incremented primary key

  public static final String CMST_ID = "cmst_id"; // unique cmst id provided by CMST
  public static final String STATUS = "status"; //

  // table creation query
  private static final String DATABASE_CREATE = "create table " + TABLE_SYNC_STATUS + "(" + _ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT , " + CMST_ID + " TEXT , " + STATUS + " INTEGER "
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    database.execSQL("DROP TABLE IF EXISTS" + TABLE_SYNC_STATUS);
    database.execSQL(DATABASE_CREATE);
  }
}
