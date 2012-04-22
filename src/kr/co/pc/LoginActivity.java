package kr.co.pc;

import java.io.IOException;
import java.util.Vector;

import kr.co.pc.common.SLog;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *	로그인 엑티비티
 */
public class LoginActivity extends BaseActivity {
	private EditText nameEt;
	private EditText pwdEt;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		initLayout();
	}

	/**
	 * 레이아웃 설정
	 */
	private void initLayout() {
		nameEt = (EditText)findViewById(R.id.user_id);
		pwdEt = (EditText)findViewById(R.id.user_pwd);
		Button btn = (Button)findViewById(R.id.login_btn);
		final AsyncTaskLogin atl = new AsyncTaskLogin();
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				// TODO Auto-generated method stub
				//atl.cancel(false);
				// 로그인 정보를 서버에 전송
				atl.execute(nameEt.getText().toString(), pwdEt.getText().toString() );
			}
		});

		setInfo();

	}

	/**
	 * 회원 가입후 넘어온 정보가 있으면 로그인 정보를 채워준다.
	 */
	private void setInfo() {
		// TODO Auto-generated method stub
		// 회원 가입후 넘어온 정보가 있으면 로그인 정보를 채워준다.
		Intent intent = getIntent();
		String userName = "";
		if(intent.hasExtra("user_name")){
			userName = intent.getStringExtra("user_name");
		}
		String userPwd = "";
		if(intent.hasExtra("user_pwd")){
			userPwd = intent.getStringExtra("user_pwd");
		}

		nameEt.setText(userName);
		pwdEt.setText(userPwd);
	}


	/**
	 *	서버에 로그인 정보를 보내 유효 확인후 친구 찾기(맵)엑티비티로 넘긴다.
	 */
	private class AsyncTaskLogin extends
			AsyncTask<String, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// 전송 완료후
			// 모든 파일이 전송이 완료되면 다이얼로그를 닫는다.
			dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			// 파일 전송 결과를 출력
			if (result) { // 파일 전송이 정상이면
				Intent intent = new Intent(LoginActivity.this, MapMainActivity.class);
				// 다음 엑티비티에 유저 정보를 넘겨준다.
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(LoginActivity.this, "로그인 정보를 확인하세요",
						Toast.LENGTH_LONG).show();
				// 전송 실패 처리 해야됨
			}
			// 화면 고정 해제
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		/**
		 * @see android.os.AsyncTask#onPreExecute() 파일 전송중 로딩바 나타내기
		 */
		@Override
		protected void onPreExecute() {	// 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			dialog = ProgressDialog.show(LoginActivity.this, "전송중", "잠시 기다려주세요", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(final String... values) {
		}

		/**
		 * @see android.os.AsyncTask#doInBackground(Params[]) 비동기 모드로 전송
		 */
		@Override
		protected Boolean doInBackground(final String... params) {	// 전송중

			// TODO Auto-generated method stub
			boolean result = false;
			if (!checkNetWork(true)) { // 네트워크 상태 체크
				return false;
			}
			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// 디바이스 정보 얻어괴
			try {

				// HTTP post 메서드를 이용하여 데이터 업로드 처리
	            vars.add(new BasicNameValuePair("user_name", params[0]));			// 이름
	            vars.add(new BasicNameValuePair("user_pwd", params[1]));			// 비밀번호
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	// 전화번호
	            String url = "http://" + SERVER_URL + LOGIN_URL;// + "?" + URLEncodedUtils.format(vars, null);
	            HttpPost request = new HttpPost(url);
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                final String responseBody = client.execute(request, responseHandler);	// 전송
        		 SLog.i(responseBody);
        		 String[] arr =responseBody.trim().split(",");
                if (responseBody.contains("ok")  ) {	// 정상이면 내 인덱스 번호 추출
   				  myIndex = Integer.valueOf(arr[2]);
   				  result = true;
                }else if (arr[1].contains("fail")) {
                	LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(LoginActivity.this, "단말기 정보가 있습니다.", Toast.LENGTH_SHORT).show();
						}
					});
                }else{
                	LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(LoginActivity.this, "zz" +  responseBody.length(), Toast.LENGTH_SHORT).show();
						}
					});
                }
            } catch (ClientProtocolException e) {
            	SLog.e("Failed to get playerId (protocol): ", e);
            } catch (IOException e) {
            	SLog.e("Failed to get playerId (io): ", e);
			} catch (Exception e) {
				dialog.dismiss(); // 프로그레스 다이얼로그 닫기
				SLog.e( "파일 업로드 에러", e);
			}

			return result;
		}

	}

	@Override
	public void onBackPressed() {	//  뒤로 가기버튼 클릭시 종료 여부
		finishDialog(this);

	}

}
