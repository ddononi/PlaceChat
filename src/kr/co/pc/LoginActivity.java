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
 *	�α��� ��Ƽ��Ƽ
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
	 * ���̾ƿ� ����
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
				// �α��� ������ ������ ����
				atl.execute(nameEt.getText().toString(), pwdEt.getText().toString() );
			}
		});

		setInfo();

	}

	/**
	 * ȸ�� ������ �Ѿ�� ������ ������ �α��� ������ ä���ش�.
	 */
	private void setInfo() {
		// TODO Auto-generated method stub
		// ȸ�� ������ �Ѿ�� ������ ������ �α��� ������ ä���ش�.
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
	 *	������ �α��� ������ ���� ��ȿ Ȯ���� ģ�� ã��(��)��Ƽ��Ƽ�� �ѱ��.
	 */
	private class AsyncTaskLogin extends
			AsyncTask<String, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// ���� �Ϸ���
			// ��� ������ ������ �Ϸ�Ǹ� ���̾�α׸� �ݴ´�.
			dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
			// ���� ���� ����� ���
			if (result) { // ���� ������ �����̸�
				Intent intent = new Intent(LoginActivity.this, MapMainActivity.class);
				// ���� ��Ƽ��Ƽ�� ���� ������ �Ѱ��ش�.
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(LoginActivity.this, "�α��� ������ Ȯ���ϼ���",
						Toast.LENGTH_LONG).show();
				// ���� ���� ó�� �ؾߵ�
			}
			// ȭ�� ���� ����
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		/**
		 * @see android.os.AsyncTask#onPreExecute() ���� ������ �ε��� ��Ÿ����
		 */
		@Override
		protected void onPreExecute() {	// ������ ���α׷��� ���̾�α׷� ���������� ����ڿ��� �˸���.
			dialog = ProgressDialog.show(LoginActivity.this, "������", "��� ��ٷ��ּ���", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(final String... values) {
		}

		/**
		 * @see android.os.AsyncTask#doInBackground(Params[]) �񵿱� ���� ����
		 */
		@Override
		protected Boolean doInBackground(final String... params) {	// ������

			// TODO Auto-generated method stub
			boolean result = false;
			if (!checkNetWork(true)) { // ��Ʈ��ũ ���� üũ
				return false;
			}
			// http �� ���� �̸� �� �� �÷���
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// ����̽� ���� ��
			try {

				// HTTP post �޼��带 �̿��Ͽ� ������ ���ε� ó��
	            vars.add(new BasicNameValuePair("user_name", params[0]));			// �̸�
	            vars.add(new BasicNameValuePair("user_pwd", params[1]));			// ��й�ȣ
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	// ��ȭ��ȣ
	            String url = "http://" + SERVER_URL + LOGIN_URL;// + "?" + URLEncodedUtils.format(vars, null);
	            HttpPost request = new HttpPost(url);
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                final String responseBody = client.execute(request, responseHandler);	// ����
        		 SLog.i(responseBody);
        		 String[] arr =responseBody.trim().split(",");
                if (responseBody.contains("ok")  ) {	// �����̸� �� �ε��� ��ȣ ����
   				  myIndex = Integer.valueOf(arr[2]);
   				  result = true;
                }else if (arr[1].contains("fail")) {
                	LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(LoginActivity.this, "�ܸ��� ������ �ֽ��ϴ�.", Toast.LENGTH_SHORT).show();
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
				dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
				SLog.e( "���� ���ε� ����", e);
			}

			return result;
		}

	}

	@Override
	public void onBackPressed() {	//  �ڷ� �����ư Ŭ���� ���� ����
		finishDialog(this);

	}

}
