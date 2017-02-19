package com.cmst.cache.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is used to create a database using SQLite It also creates tables such as the contents
 * table in the database
 * 
 * @author Shobha Deepak
 * 
 */

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "cmst_contents";
  private static final int DATABASE_VERSION = 1;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    AlbumsTable.onCreate(db);
    SyncStatusTable.onCreate(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    AlbumsTable.onUpgrade(db, oldVersion, newVersion);
    SyncStatusTable.onUpgrade(db, oldVersion, newVersion);
  }

}
