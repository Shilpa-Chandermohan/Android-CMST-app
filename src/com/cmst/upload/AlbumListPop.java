package com.cmst.upload;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.AlbumList;
import com.cmst.cache.util.Cache;
import com.cmst.cmstapp.R;
import com.cmst.common.Constants;

public class AlbumListPop extends DialogFragment implements OnItemClickListener {

  private static final String TAG=AlbumListPop.class.getSimpleName();
  private View dialogView;
  private ProgressBar progressBar;
  private TextView errorLabel;
  private AlbumNameAdapter adapter;
  private ListView listView;
  private AlbumDetails album;
  private Task task;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater=(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    dialogView= inflater.inflate(R.layout.dialog_layout, null);
    adapter=new AlbumNameAdapter(inflater);
    listView=(ListView) dialogView.findViewById(R.id.listView);
    listView.setVisibility(View.INVISIBLE);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);
    progressBar=(ProgressBar) dialogView.findViewById(R.id.dialogProgressbar);
    errorLabel=(TextView) dialogView.findViewById(R.id.errorOfDialog);
    builder.setView(dialogView);
    builder.setNegativeButton("Cancel", new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {

        Log.d(TAG, "U cancelled album Selection");

        ((AlbumSelectionListener)getActivity()).albumSelectionCancelled();
      }
    });
    task=new Task(getActivity().getApplicationContext());
    task.execute();
    return builder.create();
  }

  private class Task extends AsyncTask<Void, Void, AlbumList>
  {

    private Context context;

    public Task(Context appContext) {
      this.context=appContext;
    }

    protected void onPreExecute()
    {
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected AlbumList doInBackground(Void... params) {

      Log.d(TAG, "DoinBackground");
      return Cache.getInstance().getListOfAlbums(Constants.cmstName, context);
    }

    protected void onPostExecute(AlbumList response)
    {
      Log.d(TAG, "onPostExecute");
      if(isCancelled())
      {
        Log.d(TAG, "async task cancelled ");
        return;
      }

      progressBar.setVisibility(View.INVISIBLE);
      if(response!=null)
      {
        ArrayList<AlbumDetails> list = response.getAlbmThmbList();
        if(list!=null)
        {
          if(list.size()>0)
          {
            listView.setVisibility(View.VISIBLE);
            adapter.setAlbumList(list);
          }
          else
          {
            errorLabel.setText("No Albums at CMST, sorry");
            errorLabel.setVisibility(View.VISIBLE);
          }
        }
        else
        {
          errorLabel.setText("Unable to fetch album list, sorry");
          errorLabel.setVisibility(View.VISIBLE);
        }
      }
      else
      {
        errorLabel.setText("Unable to fetch album list, sorry");
        errorLabel.setVisibility(View.VISIBLE);
      }
    }

  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {

    album=adapter.getItem(postion);
    if(album!=null)
    {
      ((AlbumSelectionListener)getActivity()).albumClicked(album);
    }

  }


}
