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
       	this.init();	// 초기화
      //  animate();

    }

    /**
     *	초기설정
     *  전화번호 가져오기 및 전화번호 없을시 인증 하기
     */
    private void init(){
        DeviceInfo di = DeviceInfo.setDeviceInfo((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)) ;
        this.cellNum = di.getDeviceNumber();
        SLog.i("tel :" + di.getDeviceNumber());
    }


	/**
	 *  공유설정 환경을 가져와 inserted 항목이 있으면 데이터가 들어 있는걸로 체크
	 * @return
	 * 		data inserted 여부
	 */
	private boolean checkJoin() {
		// TODO Auto-generated method stub
		// 공유환경 설정 가져오기
        SharedPreferences settings = getSharedPreferences(PREFER, MODE_PRIVATE);
        // 회원 가입이 되었는지 체크
        if( !settings.contains("joined")) {
        	return false;

        }
        return settings.getBoolean("joined", true);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {	//화면 터치시
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
