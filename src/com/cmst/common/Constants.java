package com.cmst.common;

import java.io.File;

import android.content.Context;
import android.content.res.Configuration;

import com.cmst.cmstapp.R;

/**
 * Class to hold constant values
 * 
 * 
 * @author
 * 
 */
public class Constants {
  /**
   * To store session Id
   */
  public static String sessionId = "";
  // public static ImageView imageZoom = null;
  /**
   * Session timeout period
   */
  public static long sessionTimeout = 3600000; // 3600000; // "600000";

  /**
   * repeat timeout period
   */
  public static final long REPEAT_TIME = 1000 * 30;

  /**
   * Default port no of the node js server.
   */
  public static String ipAddress = "http://192.168.1.110"; // "110 or 127";

  /**
   * CMST name
   */
  public static String cmstName = "CMST Name";

  /**
   * Login API
   */
  public static String apiLogin = "/api/acnt/login";

  /**
   * Logout API
   */
  public static String apiLogout = "/api/acnt/logout?sessId=";

  /**
   * Set session API
   */
  public static String apiSetSessId = "/api/stg/setsestmr?sessId=";
  /**
   * Reset session API
   */
  public static String apiResetSessId = "/api/stg/rstsestmr?sessId=";
  /**
   * Get Album count
   */
  public static String apiGetAlbmCnt = "/api/stg/getalbmcnt?sessId=";

  /**
   * Get Album thumb list API
   */
  public static String apiGetThmbLst = "/api/stg/getalbmthmlstlast?sessId=";

  /**
   * Get Item Count API
   */
  public static String apiGetItmCnt = "/api/stg/getitmcnt?sessId=";

  /**
   * Get item thumb list API
   */
  public static String apiGetItmLst = "/api/stg/getitmthmlstlast?sessId=";

  /**
   * Lock server import API
   */
  public static String apiSetLock = "/api/stg/lckimpt?sessId=";

  /**
   * Unlock server import API
   */
  public static String apiUnLock = "/apt/stg/unlckimpt?sessId=";

  /**
   * Set upload group flag API
   */
  public static String apiSetGrpFlg = "/api/stg/setupldgrpflg?flg=1&sessId=";

  /**
   * Add item to server API
   */
  public static String apiAddItm = "/api/stg/additm?sessId=";

  /**
   * Play album side on CMST
   */
  public static String apiAblmPlybck = "api/stg/albmplybck?sessId=";

  /**
   * Set upload group flag API
   */
  public static String apiItmPlybck = "/api/stg/itmplybck?sessId=";

  /**
   * Get item information by provide id to server
   * 
   * @param: sessId, itemId, itemRawDate
   */
  public static String apiItmById = "/api/stg/getitmbyid?sessId=";

  /**
   * Get Album meta data from CMST
   * 
   * @param: sessId=1234567890&albmId=1
   */
  public static String apiGetMeta = "/api/stg/getalbminf?sessId=";

  /**
   * Set Album meta data on CMST
   * 
   * @param: sessId=1234567890&albmId=1&albmName=ALBUMTITLE&albmCmnt=COMMENT
   */
  public static String apiSetMeta = "/api/stg/setalbminf?sessId=";

  /**
   * Get Storage data on the CMST device
   * 
   * @param: sessId=1234567890
   */
  public static String apiGetStorageInfo = "/api/stg/getstginfo?sessId=";

  /**
   * Play individual item on CMST
   * 
   * @param: sessId, albmId, itemId, itemRawDate
   */
  public static String apiItemPlyBck = "/api/stg/itmplybck?sessId=";

  /**
   * Play Album on CMST
   * 
   * @param: sessId, albmId, itemId, itemRawDate
   */
  public static String apiAlbmPlyBck = "/api/stg/albmplybck?sessId=";

  /**
   * Set to true if the device is tablet
   */
  public static Boolean isTablet = false;

  /**
   * Selected Album Id
   */
  public static String albumId = "";

  /**
   * Stores CMST device count
   */
  public static int cmstCount = 0;
  /**
   * Filename of bonjour Ip
   */
  public static String bonjourIp = "bonjourIp";

  /**
   * True when application exits
   */
  public static Boolean isLogout = false;

  public static void setTablet(Context context) {
    isTablet = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
  }

  public static String allMediaTitle = "All Media";

  public static String blockCharacterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_0123456789";

  public static int maxAlbumCount = 100;

  public static Boolean isFilter = false;

  public static int prevOffset = -30;

  public static int nextOffset = 30;

  // public static String errorTitle = "Error";

  public static String getErrorMsg(Context context, int errorCode, String errorStr) {
    String errorMsg = "";
    switch (errorCode) {
    case 1001:
      errorMsg = context.getString(R.string.incorrect_param);
      break;
    case 1002:
      errorMsg = context.getString(R.string.main_unit);
      break;
    case 1003:
      errorMsg = context.getString(R.string.import_lock);
      break;
    case 1004:
      errorMsg = context.getString(R.string.cannot_upload);
      break;
    case 1005:
      errorMsg = context.getString(R.string.uploadFailed);
      break;
    case 1006:
      errorMsg = context.getString(R.string.playback);
      break;
    case 2001:
      errorMsg = context.getString(R.string.login_maxcount);
      break;
    case 2002:
      errorMsg = context.getString(R.string.forced_timeout);
      break;
    case 2003:
      errorMsg = context.getString(R.string.cmst_connection_lost);
      break;
    case 2004:
      errorMsg = context.getString(R.string.session_closed);
      break;
    case 3001:
      errorMsg = context.getString(R.string.incorrect_query);
      break;
    case 3002:
      errorMsg = context.getString(R.string.no_albums);
      break;
    case 3003:
      errorMsg = context.getString(R.string.update_failed);
      break;
    case 0:
      if (errorStr.equalsIgnoreCase("wifi")) {
        errorMsg = context.getString(R.string.enable_wifi);
      } else if (errorStr.equalsIgnoreCase("noAlbums")) {
        errorMsg = context.getString(R.string.noAlbum_date);
      } else if (errorStr.equalsIgnoreCase("network")) {
        errorMsg = context.getString(R.string.network_error);
      } else if (errorStr.equalsIgnoreCase("cmstError")) {
        errorMsg = context.getString(R.string.cmstError);
      } else if (errorStr.equalsIgnoreCase("syncError")) {
        errorMsg = context.getString(R.string.syncError);
      }
      break;
    }
    return errorMsg;
  }

  // public static String getStringInfo(int stringNum) {
  // String stringMsg = "";
  // switch (stringNum) {
  // case 0:
  // stringMsg = "Information";// U t
  // break;
  // case 1:
  // stringMsg = "Album name cannot be empty.";// U t
  // break;
  // case 2:
  // stringMsg = "Special characters are not allowed.";// U t
  // break;
  // case 3:
  // stringMsg = "No albums found.";
  // break;
  // case 4:
  // stringMsg = "Failed to fetch data."; // download exception -> cmst polling
  // break;
  // case 5:
  // stringMsg = "Sync in progress. Please wait.";
  // break;
  // // case 6:
  // // stringMsg = "You are successfully logged out";
  // // break;
  // case 7:
  // stringMsg = "Album meta information saved.";
  // break;
  // case 8:
  // stringMsg = "Please enter valid IP address."; // U manual
  // break;
  // // case 9:
  // // stringMsg = "Please wait until file type is known.";
  // // break;
  // case 10:
  // stringMsg = "Video file cannot be downloaded.";
  // break;
  // case 11:
  // stringMsg = "Image is successfully downloaded.";
  // break;
  // case 12:
  // stringMsg = "Image cannot be downloaded.";
  // break;
  // case 13:
  // stringMsg = "No album exists for the given date. Please input a different date.";
  // break;
  // }
  // return stringMsg;
  // }

  // public static String getInfoMsg(String info) {
  // String infoMsg = "";
  // if (info.equalsIgnoreCase("metaSaved")) {
  // infoMsg = "Meta Information saved.";
  // }
  // return infoMsg;
  // }

  public static Boolean getServerStatus(int errorCode) {
    if (errorCode == 1000 || errorCode == 3002)
      return true;
    else
      return false;
  }

  public static Boolean enableDiscovery = true;

  public static File albumDir = null;

  public static Boolean syncDone = false;

  public static String ALARM_ID = "alarm";
  public static String TAG = "CMSTApp";
}
