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
 * 회원 가입 엑티비티
 * 회원정보는 공유 설정환경에 저장한다.
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
	private final int TAKE_PICTURE = 4637;	// 사진 촬영 결과 코드
	//	ftp
	private MyFTPClient mFtp = null;
	// 쓰레드 처리 핸들러
    Handler handler = new Handler(){
    	@Override
		public void handleMessage(final Message msg){
    		if(msg.what == OK) {	// 처리가 완료 됐을떄 다음 화면으로 넘긴다.
    			// 공유환경 설정 열기
    	        SharedPreferences.Editor prefEditor = settings.edit();
    	        prefEditor.putBoolean("inserted", true);	// inseted를 만들어 다음부터 삽입 방지
    	        prefEditor.commit();

    	        // 완료되면 메인페이지로
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
		// sdcard path 가져오기
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
	 * android.content.Intent) 파일 선택 액티비티 결과 전송
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		// 사진 촬영선택에 uri가 없을 경우 bitmap을 이용해 파일을 만들어 준다.
		if (requestCode == TAKE_PICTURE && data.getData() == null) {
			String tmpFile = getDateTime() + ".jpg";
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			try {
				// bitmap을 jpg로 압축해주자
				bm.compress(CompressFormat.JPEG, 100,
						openFileOutput(tmpFile, MODE_PRIVATE));
			} catch (FileNotFoundException e) {
				// 알림 다이얼로그
				new AlertDialog.Builder(this)
						.setMessage("선택한 파일을 가져올수가 없습니다.").setTitle("알림")
						.setPositiveButton("확인", null).show();
				selectedFile = null;
			}
			AvataIv.setImageURI(data.getData());
			selectedFile = this.getFilesDir() /* 앱 어플리케이션 저장 위치 */
					+ "/" + tmpFile;
		} else {
			selectedFile = getRealImagePath(data.getData());
			AvataIv.setImageURI(data.getData());
		}

		imageUri = data.getData().getPath();

		// 파일 길이 체크
		String file = selectedFile.substring(selectedFile.lastIndexOf("/") + 1);
		if (file.length() >= MAX_FILE_NAME_LENGTH) {
			new AlertDialog.Builder(this)
					.setMessage("이미지명이 너무 깁니다.\n 이미지 이름은 100자 이내로 해주세요.")
					.setTitle("알림").setPositiveButton("확인", null).show();
			selectedFile = null;
			return;
		}

		File tmpfile = null;
		tmpfile = new File(selectedFile); // File 객체 생성
		int fileSize = (int) tmpfile.length(); // File 객체의 length() 메서드로 파일 길이
												// 구하기
		if (fileSize > MAX_FILE_SIZE) {
			int size = MAX_FILE_SIZE / 1024 / 1024;
			new AlertDialog.Builder(this)
					.setMessage(
							"이미지 크기는 " + String.valueOf(size) + "MB 이내로 해주세요")
					.setTitle("알림").setPositiveButton("확인", null).show();
			selectedFile = null;
			return;
		}
	}

	/**
	 * 사진촬영 혹은 사진선택번호를 받아 해당 intent로 넘긴다.
	 * @param which
	 *
	 */
	public void attachFileFilter(final int which) {
		Intent intent = null;
		intent = new Intent();
		switch (which) {
		case 0: // 사진 촬영이면

			// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(intent, TAKE_PICTURE);
			break;
		case 1: // 사진 선택이면
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(Uri.parse("*.jpg"), "image/*");
			startActivityForResult(intent, ACTION_RESULT);
			break;
		}

	}

	/**
	 * 엘리먼트 설정
	 */
	private void initLayout(){
		// 엘리먼트 후킹
		nameEt = (EditText)findViewById(R.id.join_name);
		pwdEt = (EditText)findViewById(R.id.join_pwd);
		rePwdEt = (EditText)findViewById(R.id.join_re_pwd);
		AvataIv = (ImageView)findViewById(R.id.avata_image);
		ImageButton joinBtn = (ImageButton)findViewById(R.id.pic_reg_btn);
		Button regBtn = (Button)findViewById(R.id.register_btn);
		// 이벤트 설정
		joinBtn.setOnClickListener(this);
		regBtn.setOnClickListener(this);
	}

	public void onClick(final View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case  R.id.pic_reg_btn :
			new AlertDialog.Builder(this).setTitle("첨부파일 선택")
				.setItems(R.array.attach,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							// 첨부파일 선택 처리 메소드
							attachFileFilter(which);
						}
					}).setNegativeButton("취소", null).show();
			break;
		case  R.id.register_btn :
			if(checkForm() != false){
				registerUser();
			}
			break;
		}
	}

	/*
	 * 파일 이름에 붙여줄 날자
	 */
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * URI로 부터 실제 파일 경로를 가져온다.
	 *
	 * @param uriPath
	 *            URI : URI 경로
	 * @return String : 실제 파일 경로
	 */
	private String getRealImagePath(final Uri uriPath) {
		String[] proj = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uriPath, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String path = cursor.getString(index);
		// path = path.substring(5);
		// 일부 motorola등에서 시작 path에 '/mnt/' 가 없을수 있음..
		return path.replace("/mnt/", "");
	}

	/*
	 * 기존 파일의 확장자를 새로운 파일에 붙여줌
	 */
	private String getExtension(final String oldFile, final String sep) {
		// 확장자 가져오기
		int index = oldFile.lastIndexOf(sep);
		String ext = oldFile.substring(index).toLowerCase();
		return ext;
	}

	/**
	 * 폼 체크
	 */
	private boolean checkForm() {
		if(TextUtils.isEmpty(nameEt.getText())){
			Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(pwdEt.getText())){
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(rePwdEt.getText())){
			Toast.makeText(this, "비밀번호 확인을 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(rePwdEt.getText().toString() == pwdEt.getText().toString() ){
			Toast.makeText(this, "비밀번호 입력이 같은지 확인 하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(selectedFile)){
			Toast.makeText(this, "아바타 이미지를 등록하세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}


	/**
	 * 서버에 유저를 등록한다.
	 */
	private void registerUser() {
		// TODO Auto-generated method stub
		new AsyncTaskUserInfoUpload().execute(nameEt.getText().toString(), pwdEt.getText().toString(), selectedFile);
	}

	/*
	 * ftp 연결 설정
	 */
	private boolean connectFTP() {
		mFtp = new MyFTPClient(SERVER_IP, SERVER_FTP_PORT, FTP_NAME,
				FTP_PASSWORD);
		if (!mFtp.connect()) {
		//	Toast.makeText(PictureActivity.this,
		//			"서버연결 실패!\n네트워크 상태 체크 후  다시 시도해 주세요", Toast.LENGTH_SHORT)
		//			.show();
			return false;
		}

		if (!mFtp.login()) {
		//	Toast.makeText(PictureActivity.this, "로그인 실패!", Toast.LENGTH_SHORT)
		//			.show();
			mFtp.logout();
			return false;
		}

		mFtp.cd(FTP_PATH);
		return true;
	}

	/**
	 *	사용자 이미지 파일 및 사용자 정보 업로드 클래스
	 *	서버에 사용자 정보와 이미지를 쓰레드 형식으로
	 *  업로드 한다.
	 */
	private class AsyncTaskUserInfoUpload extends
			AsyncTask<String, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// 전송 완료후
			// 모든 파일이 전송이 완료되면 다이얼로그를 닫는다.
			dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			if( JoinActivity.this.mFtp.isConnected()){	// 연결이 되어 있으면
				JoinActivity.this.mFtp.logout(); // 로그 아웃
			}
			// 파일 전송 결과를 출력
			if (result) { // 파일 전송이 정상이면
				Intent intent = new Intent(JoinActivity.this,
						LoginActivity.class);
				// 다음 엑티비티에 유저 정보를 넘겨준다.
				intent.putExtra("user_name", nameEt.getText().toString());
				intent.putExtra("user_pwd", pwdEt.getText().toString());
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(JoinActivity.this, "회원 등록 실패!\n 네트워크 상태 및 서버상태를 체크하세요",
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
			dialog = ProgressDialog.show(JoinActivity.this, "전송중",
					"사용자 환경에 따라 전송 속도가 다를수 있습니다." + " 잠시 기다려주세요", true);
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

			if (!connectFTP()) { // ftp 연결이 안되면
				return false;
			}


			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// 디바이스 정보 얻어괴
			try {
				/* 파일 업로드 */
				String imageFile = params[2].substring(params[2].lastIndexOf("/") + 1); // 실제 파일명만	 가졍옴
				// 서버에 이미지 중복 방지를 위해 이름 바꾸기 yyyymmdd_hhmmss_Cellnum_01.xxx
				String receiveFiles = getDateTime() + "_" + di.getDeviceNumber() +  getExtension(imageFile, ".");

				// 파일 업로드
				if (!mFtp.upload(params[2],receiveFiles)) {
					// 업로드 에러시
					return false;
				} else {
					 vars.add(new BasicNameValuePair("user_image", imageFile));	// 파일이름
				}
				// HTTP post 메서드를 이용하여 데이터 업로드 처리
	            vars.add(new BasicNameValuePair("user_name", params[0]));			// 이름
	            vars.add(new BasicNameValuePair("user_pwd", params[1]));			// 비밀번호
	            vars.add(new BasicNameValuePair("avata_img", receiveFiles));		// 이미지명
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	// 전화번호
	            String url = "http://" + SERVER_IP + UPLOAD_URL;// + "?" + URLEncodedUtils.format(vars, null);
	            HttpPost request = new HttpPost(url);
	         // 한글깨짐을 방지하기 위해 utf-8 로 인코딩시키자
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 인코딩
				request.setEntity(entity);

	            try {
	                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                HttpClient client = new DefaultHttpClient();
	                final String responseBody = client.execute(request, responseHandler);	// 전송
            		 SLog.i(responseBody);
	                if (responseBody.trim().equals("ok")) {
	    				  SLog.i(responseBody);
	    				  result = true;
	                }else if (responseBody.trim().equals("fail")) {
	                	JoinActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(JoinActivity.this, "단말기 정보가 있습니다.", Toast.LENGTH_SHORT).show();
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
