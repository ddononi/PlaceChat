package kr.co.pc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * ȸ�� ���� ��Ƽ��Ƽ
 * ȸ�������� ���� ����ȯ�濡 �����Ѵ�.
 */
public class JoinActivity extends BaseActivity implements OnClickListener {
	// element
	private EditText nameEt;
	private EditText pwdEt;
	private EditText rePwdEt;
	private ImageView AvataIv;
	private SharedPreferences settings;

	private String mSdcardPath;
	private String selectedFile = null;
	private String imageUri;
	private final int TAKE_PICTURE = 4637;	// ���� �Կ� ��� �ڵ�
	//	ftp
	private MyFTPClient mFtp = null;
	// ������ ó�� �ڵ鷯
    Handler handler = new Handler(){
    	@Override
		public void handleMessage(final Message msg){
    		if(msg.what == OK) {	// ó���� �Ϸ� ������ ���� ȭ������ �ѱ��.
    			// ����ȯ�� ���� ����
    	        SharedPreferences.Editor prefEditor = settings.edit();
    	        prefEditor.putBoolean("inserted", true);	// inseted�� ����� �������� ���� ����
    	        prefEditor.commit();

    	        // �Ϸ�Ǹ� ������������
    	        Intent intent =  new Intent(JoinActivity.this, SearchFriendActivity.class);
    	        finish();
    			startActivity(intent);
    		}else{

    		}
    	}
    };

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_layout);
		settings = getSharedPreferences(PREFER, MODE_PRIVATE);
		// sdcard path ��������
		String extState = Environment.getExternalStorageState();
		if (extState.equals(Environment.MEDIA_MOUNTED)) {
			mSdcardPath = Environment.getExternalStorageDirectory().getPath();
		} else {
			mSdcardPath = Environment.MEDIA_UNMOUNTED;
		}
		SLog.i(mSdcardPath);

		initLayout();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent) ���� ���� ��Ƽ��Ƽ ��� ����
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		// ���� �Կ����ÿ� uri�� ���� ��� bitmap�� �̿��� ������ ����� �ش�.
		if (requestCode == TAKE_PICTURE && data.getData() == null) {
			String tmpFile = getDateTime() + ".jpg";
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			try {
				// bitmap�� jpg�� ����������
				bm.compress(CompressFormat.JPEG, 100,
						openFileOutput(tmpFile, MODE_PRIVATE));
			} catch (FileNotFoundException e) {
				// �˸� ���̾�α�
				new AlertDialog.Builder(this)
						.setMessage("������ ������ �����ü��� �����ϴ�.").setTitle("�˸�")
						.setPositiveButton("Ȯ��", null).show();
				selectedFile = null;
			}
			AvataIv.setImageURI(data.getData());
			selectedFile = this.getFilesDir() /* �� ���ø����̼� ���� ��ġ */
					+ "/" + tmpFile;
		} else {
			selectedFile = getRealImagePath(data.getData());
			AvataIv.setImageURI(data.getData());
		}

		imageUri = data.getData().getPath();

		// ���� ���� üũ
		String file = selectedFile.substring(selectedFile.lastIndexOf("/") + 1);
		if (file.length() >= MAX_FILE_NAME_LENGTH) {
			new AlertDialog.Builder(this)
					.setMessage("�̹������� �ʹ� ��ϴ�.\n �̹��� �̸��� 100�� �̳��� ���ּ���.")
					.setTitle("�˸�").setPositiveButton("Ȯ��", null).show();
			selectedFile = null;
			return;
		}

		File tmpfile = null;
		tmpfile = new File(selectedFile); // File ��ü ����
		int fileSize = (int) tmpfile.length(); // File ��ü�� length() �޼���� ���� ����
												// ���ϱ�
		if (fileSize > MAX_FILE_SIZE) {
			int size = MAX_FILE_SIZE / 1024 / 1024;
			new AlertDialog.Builder(this)
					.setMessage(
							"�̹��� ũ��� " + String.valueOf(size) + "MB �̳��� ���ּ���")
					.setTitle("�˸�").setPositiveButton("Ȯ��", null).show();
			selectedFile = null;
			return;
		}
	}

	/**
	 * �����Կ� Ȥ�� �������ù�ȣ�� �޾� �ش� intent�� �ѱ��.
	 * @param which
	 *
	 */
	public void attachFileFilter(final int which) {
		Intent intent = null;
		intent = new Intent();
		switch (which) {
		case 0: // ���� �Կ��̸�

			// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(intent, TAKE_PICTURE);
			break;
		case 1: // ���� �����̸�
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(Uri.parse("*.jpg"), "image/*");
			startActivityForResult(intent, ACTION_RESULT);
			break;
		}

	}

	/**
	 * ������Ʈ ����
	 */
	private void initLayout(){
		// ������Ʈ ��ŷ
		nameEt = (EditText)findViewById(R.id.join_name);
		pwdEt = (EditText)findViewById(R.id.join_pwd);
		rePwdEt = (EditText)findViewById(R.id.join_re_pwd);
		AvataIv = (ImageView)findViewById(R.id.avata_image);
		ImageButton joinBtn = (ImageButton)findViewById(R.id.pic_reg_btn);
		Button regBtn = (Button)findViewById(R.id.register_btn);
		// �̺�Ʈ ����
		joinBtn.setOnClickListener(this);
		regBtn.setOnClickListener(this);
	}

	public void onClick(final View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case  R.id.pic_reg_btn :
			new AlertDialog.Builder(this).setTitle("÷������ ����")
				.setItems(R.array.attach,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							// ÷������ ���� ó�� �޼ҵ�
							attachFileFilter(which);
						}
					}).setNegativeButton("���", null).show();
			break;
		case  R.id.register_btn :
			if(checkForm() != false){
				registerUser();
			}
			break;
		}
	}

	/*
	 * ���� �̸��� �ٿ��� ����
	 */
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * URI�� ���� ���� ���� ��θ� �����´�.
	 *
	 * @param uriPath
	 *            URI : URI ���
	 * @return String : ���� ���� ���
	 */
	private String getRealImagePath(final Uri uriPath) {
		String[] proj = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uriPath, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String path = cursor.getString(index);
		// path = path.substring(5);
		// �Ϻ� motorola��� ���� path�� '/mnt/' �� ������ ����..
		return path.replace("/mnt/", "");
	}

	/*
	 * ���� ������ Ȯ���ڸ� ���ο� ���Ͽ� �ٿ���
	 */
	private String getExtension(final String oldFile, final String sep) {
		// Ȯ���� ��������
		int index = oldFile.lastIndexOf(sep);
		String ext = oldFile.substring(index).toLowerCase();
		return ext;
	}

	/**
	 * �� üũ
	 */
	private boolean checkForm() {
		if(TextUtils.isEmpty(nameEt.getText())){
			Toast.makeText(this, "�̸��� �Է��ϼ���.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(pwdEt.getText())){
			Toast.makeText(this, "��й�ȣ�� �Է��ϼ���.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(rePwdEt.getText())){
			Toast.makeText(this, "��й�ȣ Ȯ���� �Է��ϼ���.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(rePwdEt.getText().toString() == pwdEt.getText().toString() ){
			Toast.makeText(this, "��й�ȣ �Է��� ������ Ȯ�� �ϼ���.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(selectedFile)){
			Toast.makeText(this, "�ƹ�Ÿ �̹����� ����ϼ���", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}


	/**
	 * ������ ������ ����Ѵ�.
	 */
	private void registerUser() {
		// TODO Auto-generated method stub
		new AsyncTaskUserInfoUpload().execute(nameEt.getText().toString(), pwdEt.getText().toString(), selectedFile);
	}

	/*
	 * ftp ���� ����
	 */
	private boolean connectFTP() {
		mFtp = new MyFTPClient(SERVER_IP, SERVER_FTP_PORT, FTP_NAME,
				FTP_PASSWORD);
		if (!mFtp.connect()) {
		//	Toast.makeText(PictureActivity.this,
		//			"�������� ����!\n��Ʈ��ũ ���� üũ ��  �ٽ� �õ��� �ּ���", Toast.LENGTH_SHORT)
		//			.show();
			return false;
		}

		if (!mFtp.login()) {
		//	Toast.makeText(PictureActivity.this, "�α��� ����!", Toast.LENGTH_SHORT)
		//			.show();
			mFtp.logout();
			return false;
		}

		mFtp.cd(FTP_PATH);
		return true;
	}

	/**
	 *	����� �̹��� ���� �� ����� ���� ���ε� Ŭ����
	 *	������ ����� ������ �̹����� ������ ��������
	 *  ���ε� �Ѵ�.
	 */
	private class AsyncTaskUserInfoUpload extends
			AsyncTask<String, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// ���� �Ϸ���
			// ��� ������ ������ �Ϸ�Ǹ� ���̾�α׸� �ݴ´�.
			dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
			if( JoinActivity.this.mFtp.isConnected()){	// ������ �Ǿ� ������
				JoinActivity.this.mFtp.logout(); // �α� �ƿ�
			}
			// ���� ���� ����� ���
			if (result) { // ���� ������ �����̸�
				Intent intent = new Intent(JoinActivity.this,
						LoginActivity.class);
				// ���� ��Ƽ��Ƽ�� ���� ������ �Ѱ��ش�.
				intent.putExtra("user_name", nameEt.getText().toString());
				intent.putExtra("user_pwd", pwdEt.getText().toString());
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(JoinActivity.this, "ȸ�� ��� ����!\n ��Ʈ��ũ ���� �� �������¸� üũ�ϼ���",
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
			dialog = ProgressDialog.show(JoinActivity.this, "������",
					"����� ȯ�濡 ���� ���� �ӵ��� �ٸ��� �ֽ��ϴ�." + " ��� ��ٷ��ּ���", true);
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

			if (!connectFTP()) { // ftp ������ �ȵǸ�
				return false;
			}


			// http �� ���� �̸� �� �� �÷���
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// ����̽� ���� ��
			try {
				/* ���� ���ε� */
				String imageFile = params[2].substring(params[2].lastIndexOf("/") + 1); // ���� ���ϸ�	 ������
				// ������ �̹��� �ߺ� ������ ���� �̸� �ٲٱ� yyyymmdd_hhmmss_Cellnum_01.xxx
				String receiveFiles = getDateTime() + "_" + di.getDeviceNumber() +  getExtension(imageFile, ".");

				// ���� ���ε�
				if (!mFtp.upload(params[2],receiveFiles)) {
					// ���ε� ������
					return false;
				} else {
					 vars.add(new BasicNameValuePair("user_image", imageFile));	// �����̸�
				}
				// HTTP post �޼��带 �̿��Ͽ� ������ ���ε� ó��
	            vars.add(new BasicNameValuePair("user_name", params[0]));			// �̸�
	            vars.add(new BasicNameValuePair("user_pwd", params[1]));			// ��й�ȣ
	            vars.add(new BasicNameValuePair("avata_img", receiveFiles));		// �̹�����
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	// ��ȭ��ȣ
	            String url = "http://" + SERVER_IP + UPLOAD_URL;// + "?" + URLEncodedUtils.format(vars, null);
	            HttpPost request = new HttpPost(url);
	         // �ѱ۱����� �����ϱ� ���� utf-8 �� ���ڵ���Ű��
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 ���ڵ�
				request.setEntity(entity);

	            try {
	                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                HttpClient client = new DefaultHttpClient();
	                final String responseBody = client.execute(request, responseHandler);	// ����
            		 SLog.i(responseBody);
	                if (responseBody.trim().equals("ok")) {
	    				  SLog.i(responseBody);
	    				  result = true;
	                }else if (responseBody.trim().equals("fail")) {
	                	JoinActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(JoinActivity.this, "�ܸ��� ������ �ֽ��ϴ�.", Toast.LENGTH_SHORT).show();
							}
						});
	                }else{
	                	JoinActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(JoinActivity.this, responseBody, Toast.LENGTH_SHORT).show();
							}
						});
	                }
	            } catch (ClientProtocolException e) {
	            	SLog.e("Failed to get playerId (protocol): ", e);
	            } catch (IOException e) {
	            	SLog.e("Failed to get playerId (io): ", e);
	            }


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
