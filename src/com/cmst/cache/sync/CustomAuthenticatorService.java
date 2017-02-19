package com.cmst.cache.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CustomAuthenticatorService extends Service{

	
	private CustomAuthenticator auth;

	public void onCreate()
	{
		super.onCreate();
		
		auth=new CustomAuthenticator(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {

		
		return auth.getIBinder();
	}

}
