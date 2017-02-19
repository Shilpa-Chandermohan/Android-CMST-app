package com.cmst.cache.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * The DataProvider file extends the content provider and overrides the query,update,insert and
 * delete methods.
 * 
 * @author Shobha Deepak
 * 
 */
public class CacheContentProvider extends ContentProvider {

  private DatabaseHelper database;
  static final String URL = "content://" + Contract.AUTHORITY + "/cmst_contents";
  public static final Uri CONTENT_URI = Uri.parse(URL);

  /* Contents table path : "com.tata.elxsi.app.contentprovider/contents" */
  private static final String ALBUM_PATH = "albums";
  private static final String SYNC_STATUS_PATH = "syncstatus";
  private final String TAG = CacheContentProvider.this.getClass().getSimpleName();

  // contenst table uri match values
  private static final int ALBUMS = 1;
  private static final int SYNCSTATUS = 2;
  private static final int ALBUM_ID = 11;

  private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    // For contents table
    sUriMatcher.addURI(Contract.AUTHORITY, ALBUM_PATH, ALBUMS);
    sUriMatcher.addURI(Contract.AUTHORITY, SYNC_STATUS_PATH, SYNCSTATUS);
  }

  // Called when it is being created
  @Override
  public boolean onCreate() {
    // create all the tables [the creation of all the table lies within the helper class]
    database = new DatabaseHelper(getContext());
    return true; // successfully loaded
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {

    int uritype = sUriMatcher.match(uri); // get the uri type
    Cursor result = null; // result cursor

    switch (uritype) {
    case ALBUMS:
      /* get all the contents */
      result = getTableData(AlbumsTable.TABLE_ALBUMS, projection, selection, selectionArgs,
          sortOrder);
      break;
    case SYNCSTATUS:
      /* get all the contents */
      result = getTableData(SyncStatusTable.TABLE_SYNC_STATUS, projection, selection,
          selectionArgs, sortOrder);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI" + uri);
    }// end of switch
    try {
      result.setNotificationUri(getContext().getContentResolver(), uri);

    } catch (NullPointerException e) {
      return null;
    }

    return result;

  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uritype = sUriMatcher.match(uri);
    Uri _uri = null;
    long rowId = 0;
    switch (uritype) {
    case ALBUMS:
      rowId = addTableData(AlbumsTable.TABLE_ALBUMS, values);
      _uri = ContentUris.withAppendedId(
          Uri.parse(Contract.AUTHORITY + "/" + AlbumsTable.TABLE_ALBUMS), rowId);
      break;
    case SYNCSTATUS:
      rowId = addTableData(SyncStatusTable.TABLE_SYNC_STATUS, values);
      _uri = ContentUris.withAppendedId(
          Uri.parse(Contract.AUTHORITY + "/" + SyncStatusTable.TABLE_SYNC_STATUS), rowId);
      break;

    /* invalid uri */
    default:
      throw new IllegalArgumentException("Unknown URI" + uri);
    }// end of switch
    if (rowId != -1) {
      // getContext().getContentResolver().notifyChange(uri, null);
      return _uri;
    } else {
      return null;
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uritype = sUriMatcher.match(uri);
    int rowsAffected = 0;
    String albumId = "", cmstId = "";

    switch (uritype) {
    case ALBUMS:
      rowsAffected = deleteTableData(AlbumsTable.TABLE_ALBUMS, selection, selectionArgs);
      break;
    case SYNCSTATUS:
      rowsAffected = deleteTableData(SyncStatusTable.TABLE_SYNC_STATUS, selection, selectionArgs);
      break;
    case ALBUM_ID:
      cmstId = uri.getPathSegments().get(1);
      albumId = uri.getPathSegments().get(2);

      rowsAffected = deleteTableData(AlbumsTable.TABLE_ALBUMS, AlbumsTable.CMST_ID + " = " + cmstId
          + " AND " + AlbumsTable.ALBUM_ID + " = " + albumId, selectionArgs);
      break;

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    if (rowsAffected > 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsAffected;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int uritype = sUriMatcher.match(uri);
    int rowsAffected = 0;
    String id = "";

    switch (uritype) {

    /* update a row of contents with custom parameters */
    case ALBUMS:
      rowsAffected = updateTableData(AlbumsTable.TABLE_ALBUMS, values, selection, selectionArgs);
      break;
    case SYNCSTATUS:
      rowsAffected = updateTableData(SyncStatusTable.TABLE_SYNC_STATUS, values, selection,
          selectionArgs);
      break;
    /* update a row of contents with uri to the particular id */
    case ALBUM_ID:
      id = uri.getPathSegments().get(1);
      // rowsAffected = updateTableData(AlbumsTable.TABLE_CONTENTS, values, AlbumsTable.CMST_ID
      // + " = " + cmstId + " AND " + AlbumsTable.ALBUM_ID + " = " + albumId, selectionArgs);
      break;

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);

    }
    if (rowsAffected > 0) {
      // getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsAffected;
  }

  /**
   * This function is used to query the table and return the cursor object
   * 
   * @param table
   * @param projection
   * @param selection
   * @param selectionArgs
   * @param sortOrder
   * @return cursor object
   */
  private Cursor getTableData(String table, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    SQLiteDatabase db = database.getReadableDatabase();
    Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    return cursor;
  }

  /**
   * This function is used to add a row in the table
   * 
   * @param table
   * @param values
   * @return rowId of the inserted row
   */
  private long addTableData(String table, ContentValues values) {
    SQLiteDatabase db = database.getWritableDatabase();
    long rowId = db.insert(table, null, values);
    return rowId;
  }

  /**
   * This function is used to delete a row in the table
   * 
   * @param table
   * @param selection
   * @param selectionArgsselection
   * @return row Id of the deleted row
   */
  private int deleteTableData(String table, String selection, String[] selectionArgsselection) {
    SQLiteDatabase db = database.getWritableDatabase();
    return db.delete(table, selection, selectionArgsselection);

  }

  /**
   * This function is used to update a row in the table
   * 
   * @param table
   * @param values
   * @param selection
   * @param selectionArgs
   * @return rowId of the updated row
   */
  private int updateTableData(String table, ContentValues values, String selection,
      String[] selectionArgs) {
    SQLiteDatabase db = database.getWritableDatabase();
    return db.update(table, values, selection, selectionArgs);
  }

}
