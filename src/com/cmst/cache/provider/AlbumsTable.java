package com.cmst.cache.provider;

import android.database.sqlite.SQLiteDatabase;

/**
 * This is the table that contains the details of each individual photo across all albums.
 * 
 * @author Shobha Deepak
 * 
 */

public class AlbumsTable {

  // Table name
  public static final String TABLE_ALBUMS = "albums";

  // table fields
  public static final String _ID = "_id";// auto incremented primary key
  public static final String ALBUM_ID = "album_id"; // unique album id provided by CMST
  public static final String CMST_ID = "cmst_id"; // unique cmst id provided by CMST
  public static final String NAME = "name"; // album name
  public static final String ITEMS_COUNT = "items_count"; // total number of items in each album
  public static final String JSON_PATH = "path"; // path where json is stored

  // table creation query
  private static final String DATABASE_CREATE = "create table " + TABLE_ALBUMS + "(" + _ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT , " + ALBUM_ID + " TEXT , " + CMST_ID + " TEXT, "
      + NAME + " TEXT, " + ITEMS_COUNT + " INTEGER, " + JSON_PATH + " TEXT " + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    database.execSQL("DROP TABLE IF EXISTS" + TABLE_ALBUMS);
    database.execSQL(DATABASE_CREATE);
  }
}
