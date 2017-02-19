package com.cmst.cache.provider;

import android.net.Uri;

/**
 * This is a Contract class for accessing database tables from content provider. Contains
 * information(CacheConstants) about table column names, content uri's to access the content providers
 * 
 * @author ShobhaDeepak
 * 
 */

public class Contract {

  public static final String AUTHORITY = "com.cmst.cache.database.provider";

  /**
   * Static class Contents provides information about contents table
   * 
   */
  public static class Albums {
    public static final String TABLE_ALBUMS = "albums";
    /*
     * Content uri to access the Contents table
     */

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ALBUMS);

    // table fields
    public static final String _ID = "_id";// auto incremented primary key
    public static final String ALBUM_ID = "album_id"; // unique album id provided by CMST
    public static final String CMST_ID = "cmst_id"; // unique cmst id provided by CMST
    public static final String NAME = "name"; // album name
    public static final String ITEMS_COUNT = "items_count"; // total number of items in each album
    public static final String JSON_PATH = "path"; // path where json is stored
  }

  /**
   * Static class Contents provides information about Sync Status
   * 
   */
  public static class SyncStatus {
    // Table name
    public static final String TABLE_SYNC_STATUS = "syncstatus";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
        + TABLE_SYNC_STATUS);

    // table fields
    public static final String _ID = "_id";// auto incremented primary key

    public static final String CMST_ID = "cmst_id"; // unique cmst id provided by CMST
    public static final String STATUS = "status"; //
  }

}
