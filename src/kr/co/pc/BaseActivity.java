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
 *	�⺻ ���� ��Ƽ��Ƽ
 */
public class BaseActivity extends Activity implements iChatConstant {
	public static Typeface typeFace = null;
	public static Typeface typeFaceBold = null;
	public final static Calendar calendar = Calendar.getInstance();
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ȭ�� ���� ����
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// ��Ʈ ����
		if(typeFace == null){
			// ������� �۲�
			typeFace = Typeface.createFromAsset(getAssets(), "fonts/NanumGothic.ttf");
			typeFaceBold = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
			//typeFace = Typeface.createFromAsset(getAssets(), "fonts/HMKLA.TTF");
		}
	}

	/**
	 * ��Ʈ ����
	 */
	@Override
	public void setContentView(final int viewId) {
		View view = LayoutInflater.from(this).inflate(viewId, null);
		ViewGroup group = (ViewGroup)view;
		recursiveViewSetTypeFace(group);
		super.setContentView(view);
	}

	/**
	 * �並 Ž���Ͽ� ��Ʈ�� �����ϴ� ��� �޼ҵ�
	 * tag�� ������ ������ ������Ʈ�� ����
	 * @param view
	 * 	Ž���� �� �׷�
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
	 * ���� confirm ���̾�α� â
	 * @param context
	 */
	public void finishDialog(final Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage("���α׷��� �����Ͻðڽ��ϱ�?")
		.setPositiveButton("����", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				moveTaskToBack(true);moveTaskToBack(true);
                finish();
			}
		}).setNegativeButton("���",null).show();
    }

	/**
	 * ȸ���� �ٽ� ȭ���� �ҷ����°� �����ϱ����� ��ũ���� �ᰡ�ش�.
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
	 *	ȭ�� ���� ����
	 */
	public void unLockScreenRotation(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}


	/**
	 * ���õ� �޴��� ��ư�� Ȱ��ȭ
	 */
	public void setMenuButtonFoucs(final int where){
		switch(where){
		case MENU_CUSTOMER :
			break;
		case MENU_MAP :		// ������ư Ȱ��ȭ
			break;
		}

	}

	/**
	 * ��Ʈ��ũ���� ��밡������ Ȥ�� ����Ǿ��ִ��� Ȯ���Ѵ�.
	 * msgFlag�� false�̸� ���� ����Ǿ� �ִ� ��Ʈ��ũ�� �˷��ش�.
	 * ��Ʈ��ũ�� ���� �Ұ��� ����� ���� ���̾�α�â�� ��� �˸���.
	 * @param msgFlag
	 * 		Toast �޼���  ��뿩��
	 * @return
	 *		��Ʈ��ũ ��밡�� ����
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
				Toast.makeText(this, "Wi-Fi���� �������Դϴ�.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (msgFlag == false) {
				Toast.makeText(this, "3G���� �������Դϴ�.",
						Toast.LENGTH_SHORT).show();
			}
		}

		if (!isMobileConn && !isWifiConn) {
			/*
			 * ��Ʈ��ũ ������ ���� ������� ���� ȭ������ ���ư���.
			 */
			new AlertDialog.Builder(this)
			.setTitle("�˸�")
			.setMessage(
					"Wifi Ȥ�� 3G���� ������� �ʾҰų� "
							+ "��Ȱ���� �ʽ��ϴ�.��Ʈ��ũ Ȯ���� �ٽ� ������ �ּ���!")
			.setPositiveButton("�ݱ�",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss(); // �ݱ�
							finish();
						}
					}).show();
			return false;
		}
		return true;

	}
}
