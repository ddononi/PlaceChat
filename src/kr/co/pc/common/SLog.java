package kr.co.pc.common;

import android.util.Log;

/**
 * release시 보안상 log 출력을 막는다.
 *
 */
public class SLog {
	public final static boolean DEBUG_MODE = true;	// realse mode or debug mode
	public final static String TAG = "placeChat";

	// info
	public static void i(final String msg){
		if(DEBUG_MODE){
			Log.i(TAG, msg);
		}
	}

	// debug
	public static void d(final String msg){
		if(DEBUG_MODE){
			Log.d(TAG, msg);
		}
	}

	// warning
	public static void w(final String msg){
		if(DEBUG_MODE){
			Log.w(TAG, msg);
		}
	}

	// error
	public static void e(final String msg, final Exception e){
		if(DEBUG_MODE){
			Log.e(TAG, msg, e);
		}
	}

	// verbose
	public static void v(final String msg){
		if(DEBUG_MODE){
			Log.v(TAG, msg);
		}
	}
}
