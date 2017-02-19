package com.cmst.cache.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.cmst.cache.util.CacheConstants;
import com.cmst.cache.util.ResyncDataResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class to request for sync with the
 * 
 * web server
 * 
 * 
 * @author test
 * 
 */
public class SyncInitiator {

  /**
   * Account related constants.
   */
  private Account account;

  private static final String ACCOUNT_TYPE = "cmst.com";

  private static final String AUTHORITY = "com.cmst.cache.database.provider";

  private static final String ACCOUNT_NAME = "acountName";

  /**
   * Variable to hold the prefernces object.
   */

  public SyncInitiator(Context context) {

    account = createSyncAccount(context);

  }

  /**
   * Method to start request for sync with the
   * 
   * web server.
   * 
   * @param data
   * @param synctype
   */
  public void startSync(String cmstIp, String cmstId, long sessionId, int synctype,
      ResyncDataResponse data) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String resyncData = gson.toJson(data, ResyncDataResponse.class);

    Bundle bundleData = new Bundle();
    bundleData.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    bundleData.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    bundleData.putString(CacheConstants.KEY_CMST_ID, cmstId);
    bundleData.putString(CacheConstants.KEY_CMST_IP, cmstIp);
    bundleData.putLong(CacheConstants.KEY_SESSION_ID, sessionId);
    bundleData.putString(CacheConstants.KEY_RESYNC_DATA, resyncData);
    bundleData.putInt(CacheConstants.KEY_SYNC_TYPE, synctype);
    ContentResolver.requestSync(account, AUTHORITY, bundleData);

  }

  /**
   * Create a new sync account if it does not exist.
   * 
   * @param context
   * @return newly created account or the same account if it exists.
   */
  private Account createSyncAccount(Context context) {

    Account dummyAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

    if (manager.addAccountExplicitly(dummyAccount, null, null)) {
      return dummyAccount;
    } else {
      Account[] listOfAccounts = manager.getAccountsByType(ACCOUNT_TYPE);
      if (listOfAccounts.length > 0) {

        return listOfAccounts[0];

      }
    }

    return null;
  }

}
