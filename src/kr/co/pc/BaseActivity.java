package kr.co.pc;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 *	기본 설정 액티비티
 */
public class BaseActivity extends Activity implements iChatConstant {
	public static Typeface typeFace = null;
	public static Typeface typeFaceBold = null;
	public final static Calendar calendar = Calendar.getInstance();
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 화면 세로 고정
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 폰트 설정
		if(typeFace == null){
			// 나눔고딕 글꼴
			typeFace = Typeface.createFromAsset(getAssets(), "fonts/NanumGothic.ttf");
			typeFaceBold = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
			//typeFace = Typeface.createFromAsset(getAssets(), "fonts/HMKLA.TTF");
		}
	}

	/**
	 * 폰트 설정
	 */
	@Override
	public void setContentView(final int viewId) {
		View view = LayoutInflater.from(this).inflate(viewId, null);
		ViewGroup group = (ViewGroup)view;
		recursiveViewSetTypeFace(group);
		super.setContentView(view);
	}

	/**
	 * 뷰를 탐색하여 폰트를 적용하는 재귀 메소드
	 * tag를 가지고 있으면 볼드폰트로 적용
	 * @param view
	 * 	탐색할 뷰 그룹
	 */
	private void recursiveViewSetTypeFace(final ViewGroup group){
		int childCnt = group.getChildCount();
		TextView tv;
		Button b;
		EditText et;
		for(int i=0; i<childCnt; i++){
			View v = group.getChildAt(i);
			if(v instanceof TextView){
				tv = (TextView)v;
				tv.setTypeface( (tv.getTag()!= null) ? typeFaceBold:typeFace );
			}else if(v instanceof Button){
				b = (Button)v;
				b.setTypeface( (b.getTag()!= null) ? typeFaceBold:typeFace );
			}else if(v instanceof EditText){
				et =(EditText)v;
				et.setTypeface( (et.getTag()!= null) ? typeFaceBold:typeFace );
			}else if(v instanceof ViewGroup){
				recursiveViewSetTypeFace((ViewGroup)v);
			}
		}
	}

	/**
	 * 종료 confirm 다이얼로그 창
	 * @param context
	 */
	public void finishDialog(final Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage("프로그램을 종료하시겠습니까?")
		.setPositiveButton("종료", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				moveTaskToBack(true);moveTaskToBack(true);
                finish();
			}
		}).setNegativeButton("취소",null).show();
    }

	/**
	 * 회전이 다시 화면을 불러오는걸 방지하기위해 스크린을 잠가준다.
	 */
	public void mLockScreenRotation() {
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	/**
	 *	화면 고정 해제
	 */
	public void unLockScreenRotation(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}


	/**
	 * 선택된 메뉴의 버튼의 활성화
	 */
	public void setMenuButtonFoucs(final int where){
		switch(where){
		case MENU_CUSTOMER :
			break;
		case MENU_MAP :		// 지도버튼 활성화
			break;
		}

	}

	/**
	 * 네트워크망을 사용가능한지 혹은 연결되어있는지 확인한다.
	 * msgFlag가 false이면 현재 연결되어 있는 네트워크를 알려준다.
	 * 네트워크망 연결 불가시 사용자 에게 다이얼로그창을 띄어 알린다.
	 * @param msgFlag
	 * 		Toast 메세지  사용여부
	 * @return
	 *		네트워크 사용가능 여부
	 */
	public boolean checkNetWork(final boolean msgFlag) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// boolean isWifiAvail = ni.isAvailable();
		boolean isWifiConn = ni.isConnectedOrConnecting();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// boolean isMobileAvail = ni.isAvailable();
		boolean isMobileConn = ni.isConnectedOrConnecting();
		if (isWifiConn) {
			if (msgFlag == false) {
				Toast.makeText(this, "Wi-Fi망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (msgFlag == false) {
				Toast.makeText(this, "3G망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		}

		if (!isMobileConn && !isWifiConn) {
			/*
			 * 네트워크 연결이 되지 않을경우 이전 화면으로 돌아간다.
			 */
			new AlertDialog.Builder(this)
			.setTitle("알림")
			.setMessage(
					"Wifi 혹은 3G망이 연결되지 않았거나 "
							+ "원활하지 않습니다.네트워크 확인후 다시 접속해 주세요!")
			.setPositiveButton("닫기",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss(); // 닫기
							finish();
						}
					}).show();
			return false;
		}
		return true;

	}
}
