package com.cmst.cmstapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.cmst.common.Constants;

public class SplashScreen extends Activity {
  private Runnable splashRunner;
  private Handler splashScreenHandler;
  SharedPreferences sh_Pref;
  private Editor toEdit;

  /**
   * Called when splash screen activity is created
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.splash);
    sh_Pref = this.getSharedPreferences("deviceList", Context.MODE_PRIVATE);
    toEdit = sh_Pref.edit();
    removeDevicesList();
    Constants.setTablet(this);
    if (Constants.isTablet) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      TextView cannonText = (TextView) findViewById(R.id.cannonText);
      // cannonText.setTextSize(R.dimen.cmst_splash_font_Tab);
      cannonText.setTextSize(24);
    }
    splashScreenHandler = new Handler();
    /**
     * Goto discovery screen after five seconds
     */
    splashRunner = new Runnable() {
      @Override
      public void run() {
//        Constants.enableDiscovery = false;
        Intent intent;
        if(Constants.enableDiscovery){
          intent = new Intent(SplashScreen.this, DiscoveryActivity.class);
        } else {
          intent = new Intent(SplashScreen.this, ManualDiscoveryActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
      }
    };
    splashScreenHandler.postDelayed(splashRunner, 2000);
  }

  /**
   * Method to remove all CMST device names
   */
  public void removeDevicesList() {
    toEdit.clear();
    toEdit.commit();
  }

  /**
   * Called when Splash screen is killed
   */
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

}
