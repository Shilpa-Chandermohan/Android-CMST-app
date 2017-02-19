package com.cmst.cache.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAdapterService  extends Service{

	private static final Object lock=new Object();
	
	private static SyncAdapter synAdapter;
	
	public void onCreate()
	{
		synchronized (lock) {
		
			if(synAdapter==null)
			{
				synAdapter=new SyncAdapter(getApplicationContext(), true);
			}
			
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {

		return synAdapter.getSyncAdapterBinder();
	}

}
