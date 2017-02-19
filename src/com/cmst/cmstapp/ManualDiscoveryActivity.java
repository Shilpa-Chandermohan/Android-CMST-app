package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmst.common.Constants;

public class ManualDiscoveryActivity extends Activity {

  public Boolean bserverDone = true;
  private String tempIpAddress;
  EditText ipAaddress;
  ImageView view;
  ImageView refreshText;
  private SharedPreferences sh_Pref;
  private Editor toEdit;
  private Boolean exitDiscovery;

  /**
   * Called when activity is started.
   * 
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /**
     * changes orientation to landscape for Tablet.
     */
    // if (Constants.isTablet) {
    // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    // }
    exitDiscovery = false;
    setContentView(R.layout.edit_cmst_ip);
    ipAaddress = (EditText) findViewById(R.id.IP);
    ipAaddress.setText("10.75.17.120");

    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
    Button done = (Button) findViewById(R.id.DONE);
    done.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (DiscoveryActivity.checkForWiFi(ManualDiscoveryActivity.this)) {
          if (isIpAddress(ipAaddress.getText().toString())) {

            if (!bserverDone) {
              Toast.makeText(getBaseContext(), "Please wait..", Toast.LENGTH_SHORT).show();
            } else {
              bserverDone = false;
              Log.e("Manual Discovery", Constants.ipAddress);
              if (!Constants.ipAddress.equalsIgnoreCase(ipAaddress.getText().toString())
                  && !Constants.ipAddress.equalsIgnoreCase("")
                  && !Constants.sessionId.equalsIgnoreCase("")) {
                new DoLogout().execute("");
                tempIpAddress = "http://" + ipAaddress.getText().toString();
              } else {
                Log.e("temp2=", "" + ipAaddress.getText().toString());
                Constants.ipAddress = "http://" + ipAaddress.getText().toString();
                Constants.cmstName = ipAaddress.getText().toString();
                Intent intent = new Intent(ManualDiscoveryActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
              }
            }
          } else {
            DiscoveryActivity.errorDialog(ManualDiscoveryActivity.this, "Error Message",
                getString(R.string.valid_ip), false, "");

          }
        } else {
          DiscoveryActivity.errorDialog(ManualDiscoveryActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
        }
      }
    });

    setUpActionBarIcon();
  }

  /**
   * Method to store CMST name,IPAddress and port number using shared preference
   */
  public void sharedPrefernces(String cmstName, String cmstIp, int cmstPortNumber) {
    sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
    toEdit = sh_Pref.edit();
    toEdit.putString("ipAaddress", cmstIp);
    toEdit.putString("CMSTId", cmstName);
    toEdit.putString("CMSTPortNumber", Integer.toString(cmstPortNumber));
    toEdit.commit();
  }

  public void onBackPressed() {
    super.onBackPressed();
    exitDiscovery = true;
    new DoLogout().execute("");
    Constants.cmstName = "";
    Log.e("TAG", "ManualActivity Constants.sessionId = empty");
    Constants.sessionId = "";
  }

  /**
   * To customize the title bar.
   */
  void setUpActionBarIcon() {
    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header);
    ImageView actionLeft = (ImageView) findViewById(R.id.action_left);
    actionLeft.setVisibility(View.INVISIBLE);

    ImageView actionRight = (ImageView) findViewById(R.id.action_right);
    actionRight.setVisibility(View.INVISIBLE);

    TextView actionTitle = (TextView) findViewById(R.id.action_title);
    actionTitle.setText("Select CMST");
  }

  /**
   * Validate IP address.
   */
  public boolean isIpAddress(String ipAddress) {
    String ipAddressPATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    Pattern pattern = Pattern.compile(ipAddressPATTERN);
    Matcher matcher = pattern.matcher(ipAddress);
    return matcher.matches();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  protected void onResume() {
    super.onResume();
  }

  protected void onPause() {
    super.onPause();
  }

  /**
   * Called when activity is killed.
   */
  protected void onDestroy() {
    super.onDestroy();
  }

  private class DoLogout extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
      StringBuilder thumbListUrl = new StringBuilder();
      thumbListUrl.append(Constants.ipAddress).append(Constants.apiLogout)
          .append(Constants.sessionId);
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(thumbListUrl.toString());
      Log.e("Discovery Screen ", "%%%%%%%%%%%%% API: " + thumbListUrl);
      String line = "";
      String data = "";
      try {
        HttpResponse response = client.execute(get);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode == 200) {
          BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
              .getContent()));
          while ((line = rd.readLine()) != null) {
            data += line;
          }

          return data;
        } else {
          return null;

        }
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return data;
    }

    @Override
    protected void onPostExecute(String result) {
      if (result == null) {
        DiscoveryActivity.errorDialog(ManualDiscoveryActivity.this, getString(R.string.err),
            Constants.getErrorMsg(getBaseContext(), 0, "network"), false, "");
        return;
      }
      Log.e("Logged out", "logged out");
      JSONObject jsonObj = null;
      try {
        jsonObj = new JSONObject(result);
        if (!Constants.getServerStatus(jsonObj.getInt("res"))) {
          DiscoveryActivity.errorDialog(ManualDiscoveryActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), jsonObj.getInt("res"), ""), false, "");
        } else {
          if (!exitDiscovery) {
            bserverDone = true;
            Log.e("TAG", "ManualActivity Constants.sessionId = empty");
            Constants.sessionId = "";
            Log.e("temp1=", "" + tempIpAddress);
            Constants.ipAddress = tempIpAddress;
            Constants.cmstName = ipAaddress.getText().toString();
            Intent intent = new Intent(ManualDiscoveryActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
}
