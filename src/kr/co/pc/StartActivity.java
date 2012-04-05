package kr.co.pc;

import kr.co.pc.common.SLog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;

public class StartActivity extends BaseActivity {
	String cellNum = "";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
       	this.init();	// �ʱ�ȭ
      //  animate();

    }

    /**
     *	�ʱ⼳��
     *  ��ȭ��ȣ �������� �� ��ȭ��ȣ ������ ���� �ϱ�
     */
    private void init(){
        DeviceInfo di = DeviceInfo.setDeviceInfo((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)) ;
        this.cellNum = di.getDeviceNumber();
        SLog.i("tel :" + di.getDeviceNumber());
    }


	/**
	 *  �������� ȯ���� ������ inserted �׸��� ������ �����Ͱ� ��� �ִ°ɷ� üũ
	 * @return
	 * 		data inserted ����
	 */
	private boolean checkJoin() {
		// TODO Auto-generated method stub
		// ����ȯ�� ���� ��������
        SharedPreferences settings = getSharedPreferences(PREFER, MODE_PRIVATE);
        // ȸ�� ������ �Ǿ����� üũ
        if( !settings.contains("joined")) {
        	return false;

        }
        return settings.getBoolean("joined", true);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {	//ȭ�� ��ġ��
		// TODO Auto-generated method stub
		  if ( event.getAction() == MotionEvent.ACTION_DOWN ){

			 Intent intent = null;
			 if( checkJoin() == true ) {
				intent =  new Intent(this, LoginActivity.class);
			} else {
				intent =  new Intent(this, JoinActivity.class);
			}

			 startActivity(intent);
			 return true;
		  }

		  return super.onTouchEvent(event);

	}


}
