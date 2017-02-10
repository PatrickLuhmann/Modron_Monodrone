package com.nerdyneutrino.modron_monodrone;

import android.util.Log;

public class MyDebug {

	public static void Print(String pre, String message)
	{
		if (BuildConfig.DEBUG) Log.d(pre, message);
	}
}
