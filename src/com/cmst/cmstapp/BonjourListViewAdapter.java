package com.cmst.cmstapp;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cmst.common.Constants;

/**
 * Adapter class to load CMST device names after discovering
 * 
 * @Filename: BonjourAdapter.java
 * @author: Demo
 * @version: 0.1
 */
public class BonjourListViewAdapter extends BaseAdapter {
  /**
   * Application context which used this adapter.
   */
  private Context appContext;

  private ServiceInfo[] list;
  private LayoutInflater inflater;
  private ListView listView;

  /**
   * Adapter constructor
   * 
   * @param listView
   * 
   * @param: appContext - Application context which uses this adapter.
   */
  public BonjourListViewAdapter(Context appContext, Activity act, ListView listView) {
    this.appContext = appContext;
    this.listView = listView;
  }

  /**
   * Returns the total number of devices discovered.
   */
  @Override
  public int getCount() {
    if (list != null) {
      return list.length;
    } else {
      SharedPreferences prefs = appContext.getSharedPreferences("deviceList",
          Context.MODE_PRIVATE);
      return prefs.getInt("cmstCount", 0);
    }
  }

  /**
   * Returns the item for a given position.
   * 
   * @param: position
   */
  @Override
  public Object getItem(int position) {
    if (list != null) {
      return list[position];
    }
    return null;
  }

  /**
   * Returns the current position.
   * 
   * @param: position
   */
  @Override
  public long getItemId(int position) {
    return position;
  }

  /**
   * Set album details for each list item.
   * 
   * @param: position
   * @param: convertView
   * @param: parent
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View vi = convertView;
    SharedPreferences prefs = appContext.getSharedPreferences("deviceList", Context.MODE_PRIVATE);
    if (convertView == null) {
      inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      vi = inflater.inflate(R.layout.discovery_item, parent, false);
      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 150, 80.0f);
      LinearLayout itemLeftRight = (LinearLayout) vi.findViewById(R.id.discoveryLeft);
      itemLeftRight.setLayoutParams(param);
      param = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 20.0f);
      itemLeftRight = (LinearLayout) vi.findViewById(R.id.discoveryRight);
      itemLeftRight.setLayoutParams(param);
      TextView txtView = (TextView) vi.findViewById(R.id.action_title);
      ViewHolder holder = new ViewHolder();
      holder.setTextView(txtView);
      vi.setTag(holder);
      if (list == null) {
        txtView.setText(prefs.getString("cmstName_" + position, ""));
      } else {
        txtView.setText(list[position].getName());
      }
      if (Constants.isTablet) {
        txtView.setPadding(80, 0, 0, 0);
      }
      String matchCmstName = "";
      if (list == null) {
        matchCmstName = prefs.getString("cmstName_" + position, "");
      } else {
        matchCmstName = list[position].getName();
      }
      if (Constants.cmstName.equalsIgnoreCase(matchCmstName)) {
        Log.e("9...................", "1..................");
        vi.setBackgroundColor(0x5500C3FF);
        vi.invalidate();
        listView.setSelection(position);
        listView.setItemChecked(position, true);
      }
      return vi;
    }
    String matchCmstName = "";
    ViewHolder temp = (ViewHolder) convertView.getTag();
    if (list == null)
    {

      matchCmstName = prefs.getString("cmstName_" + position, "");

    } else {

      matchCmstName = list[position].getName();
    }

    temp.getTextView().setText(matchCmstName);
    if (Constants.cmstName.equalsIgnoreCase(matchCmstName)) {
      vi.setBackgroundColor(0x5500C3FF);
      vi.invalidate();
      listView.setSelection(position);
      listView.setItemChecked(position, true);
    } else {
      vi.setBackgroundColor(appContext.getResources().getColor(R.color.cmstWhite));
      vi.invalidate();
    }

    return convertView;
  }

  private class ViewHolder {

    private TextView textView;

    public TextView getTextView() {
      return textView;
    }

    public void setTextView(TextView textView) {
      this.textView = textView;
    }
  }

  public void setData(ServiceInfo[] list) {

    this.list = list;

  }

}
