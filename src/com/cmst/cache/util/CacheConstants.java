package com.cmst.cache.util;

public class CacheConstants {
	public static final int BYTE_SIZE = 1024;

	public static final String SYNC_FINISHED_ACTION = "sync123";

	public static final String EXTRA_SYNC_STATUS = "syncStatus";

	public static final String IMAGES_FOLDER = "imgFolder";

	public static final String PREF_FILE_NAME = "MyPref";

	public static final String KEY_RESYNC_DATA = "resyncData";

	public static final String KEY_CMST_ID = "cmstId";

	public static final String KEY_SESSION_ID = "sessionId";

	public static final String KEY_CMST_IP = "cmstIp";
	
	public static final String KEY_SYNC_TYPE = "syncType";

	public static final int SUCCESS = 1000;//success 

	public static final String API_GET_ALBUMS_LIST = "/api/stg/getalbmthmlstlast?sessId=";
	
	public static final String API_GET_ALBUM_COUNT = "/api/stg/getalbmcnt?sessId=";
	
	public static final String API_GET_ALBUM_ITEMS_COUNT = "/api/stg/getitmcnt?sessId=";
	
	public static final String API_GET_ALBUM_ITEMS_LIST = "/api/stg/getitmthmlstlast?sessId=";
	
	public static final String API_LOGIN="/api/acnt/login";
	
	public static final String CMST_IP = "http://192.168.1.110";
	
	public static final int TIME_OUT_IN_MILISEC = 100000;
	
//	public static String PROTOCOL = "http://";
	
	public static final int SYNC_SUCCESS = 1;

	public static final int SYNC_FAILURE = 0; 
	
	public static final int SYNC_NOTEXIST = 2;
	
	public static final int SYNC_FRESH = 3;
	
	public static final int SYNC_MODIFIED = 4;
}
