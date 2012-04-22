package kr.co.pc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import kr.co.pc.common.SLog;
import kr.co.pc.data.FriendData;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapView.CurrentLocationEventListener;
import net.daum.mf.map.api.MapView.MapViewEventListener;
import net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener;
import net.daum.mf.map.api.MapView.POIItemEventListener;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 *	지도 엑티비티
 *	내 위치를 주변으로 친구 목록을 아이콘으로 나타낸다.
 *	아이콘을 선택시 친구 정보를 보여주며 대화를 할수 있다.
 *	일정주기를 간격으로 친구에게 온 메시지를 확인한다.
 */
public class MapMainActivity extends BaseActivity implements
OpenAPIKeyAuthenticationResultListener, MapViewEventListener,
CurrentLocationEventListener, POIItemEventListener {
	private MapView mMapView = null;
	private List<FriendData> friendList = null;
	private List<FriendData> showingFriendList = null;	// 지도에 나타낸 친구 리스트
	private ProgressDialog dialog = null;
	private CheckMessageThread cmThread = null;
	private MapPoint.GeoCoordinate mapPointGeo;	// 현위치를 받을 point 객체
	private FriendThread friendThread;
	private String selectedLecture = "";	// 선택된 수강명
	// ui 처리를 위한 핸들러
	private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case 1:
    			final int userIndex = msg.arg1;
    			 AlertDialog.Builder builder = new AlertDialog.Builder(MapMainActivity.this);
    			 builder.setMessage("새로운 메세지가 있습니다.").setPositiveButton("참여", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MapMainActivity.this, ChatActivity.class);
						intent.putExtra("userIndex", userIndex);
						startActivity(intent);
					}
				}).setCancelable(false);
    			 /*
    			 .setNegativeButton("취소", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						cmThread = new CheckMessageThread();
						cmThread.start();
					}
				}).setCancelable(false);
				*/
		        final AlertDialog alert = builder.create();
		        alert.show();
    			break;
    		}
    	}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.map_layout);
		initLayout();
        SharedPreferences settings = getSharedPreferences(PREFER, MODE_PRIVATE);		
        lecture = settings.getString("lecture", "");
	}

	/**
	 * 맵 키 설정 , 이벤트 설정 , 기타 초기화 설정
	 */
	private void initLayout() {
		// TODO Auto-generated method stub
		LinearLayout linearLayout = new LinearLayout(this);
		// map 설정
		mMapView = new MapView(this);
		mMapView.setDaumMapApiKey(DAUM_MMAPS_ANDROID_APIKEY);
		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
		mMapView.setMapViewEventListener(this);
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		mMapView.setZoomLevel(14, true);			// 줌설정
		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.570705,126.958008), true);	// 초기 지도 위치 설정
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		linearLayout.addView(mMapView);
		
		setContentView(linearLayout);
	}

	/**
	 * 친구한테 온 메지시 쓰레드 재 시작
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 메시지가 있는지 체크
		cmThread = new CheckMessageThread();
		cmThread.start();		
		//friendThread = new FriendThread();
	}
	
	

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		friendThread = new FriendThread();
		friendThread.start();
	}

	/**
	 *모든 친구목록 아이템들을 지운다.
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 모든 친구목록 아이템들을 지운다.
		mMapView.removeAllPOIItems();
	}

	/**
	 * 반경 주변 친구들정보를 가지고 POIitem을 생성한다.
	 * 친구 정보가 없으면 리턴처리하고 있으면 주변에 있는지 확인하고
	 * 맵뷰에 아이템을 등록해준다.
	 */
	private void addPoiItem(List<FriendData> list){
		
		// 친구가 없으면 리턴 처리
		if(list == null || list.size() == 0){
        	MapMainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(MapMainActivity.this, "접속한 친구가 없습니다.", Toast.LENGTH_SHORT).show();
				}
			});	
        	return;
		}
		showingFriendList = new ArrayList<FriendData>();
		// poi 배열
		MapPOIItem poiItem;
		int i = 0;
		for(FriendData data : list){	// 리스트 수 만큼 POI아이템을 생성
			double lat = Double.parseDouble(data.getLat());
			double lng = Double.parseDouble(data.getLng());
			// 일정거리 이상이면 화면에 보이지 않는다.
			MapPoint.GeoCoordinate coord;
			if(mapPointGeo == null){
				coord = mMapView.getMapCenterPoint().getMapPointGeoCoord();
			}else{
				coord = mapPointGeo;
			}
			double distance = getDistanceArc(lat,  lng, coord.latitude, coord.longitude);
				SLog.i("distance-->" + distance);
			if(distance > 1){	// 1km 이상이면
				continue;
			}else if(data.getLecture().contains(selectedLecture) == false){	// 같은 수업인지 확인
				continue;
			}
			
			// poi 셋팅
			poiItem = new MapPOIItem();
			poiItem.setItemName(data.getUserName());
			MapPoint p = MapPoint.mapPointWithGeoCoord(lat,lng);				
			poiItem.setMapPoint(p);
			poiItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
			poiItem.setShowAnimationType(MapPOIItem.ShowAnimationType.SpringFromGround);
			poiItem.setShowCalloutBalloonOnTouch(true);
			//poiItem.setDraggable(true);	
			poiItem.setTag(i);
			showingFriendList.add(data);
			mMapView.addPOIItem(poiItem);
			i++;
		}
		//	지도 화면에 추가된 모든 POI(Point Of Interest) Item들이 화면에 나타나도록 지도 화면 중심과 확대/축소 레벨을 자동으로 조정한다.
		mMapView.fitMapViewAreaToShowAllPOIItems();
		
	}
	
	/**
	 * 해당 사용자가 말풍선을 클릭했을경우 사용자 아바타 이미지와 사용자명을 보여주는
	 * 다이얼로그를 띄운후 대화참여 여부를 확인한다.
	 * 대화하기가 선택되면 1:1 채팅방 엑티비로 넘긴다.
	 */
	public void onCalloutBalloonOfPOIItemTouched(final MapView v, final MapPOIItem item) {
		final FriendData data = showingFriendList.get(item.getTag());	// tag를 이용하여 친구정보객체를 얻어온다.
		
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.dialog, 
                (ViewGroup)findViewById(R.id.dialog_root));
        
        // 위에서 얻은 뷰를 다이얼로그 박스에 적용합니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // custom view에서 사용한 위젯들의 동작을 정의합니다.
        TextView message = (TextView) view.findViewById(R.id.user_name);
        TextView lecture = (TextView) view.findViewById(R.id.user_lecture);
        ImageView avataIv = (ImageView) view.findViewById(R.id.avata);
        new ImageLoadThread(avataIv, data.getImageFile()).start();

        message.setText("이름 : " + data.getUserName());
        lecture.setText("수강 : " + data.getLecture());
        //message.setText("수업 : " + item.getLece());
        builder.setView(view);
        // 대화하기를 선택하면 대화엑티비티화면으로 넘긴다.
        builder.setPositiveButton("대화하기", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MapMainActivity.this, ChatActivity.class);
				intent.putExtra("userIndex", data.getIndex());	// 친구 인덱스를 저장한다.
				startActivity(intent);
			}
		});
        final AlertDialog alert = builder.create();
        alert.show();
	}

	public void onDraggablePOIItemMoved(final MapView arg0, final MapPOIItem arg1,
			final MapPoint arg2) {
		// TODO Auto-generated method stub

	}

	public void onPOIItemSelected(final MapView arg0, final MapPOIItem arg1) {
		// TODO Auto-generated method stub

	}

	public void onCurrentLocationDeviceHeadingUpdate(final MapView arg0, final float arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 *	단말의 현위치 좌표값을 통보받을 수 있다. MapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode) 
	 *	메소드를 통해 사용자 현위치 트래킹 기능이 켜진 경우(CurrentLocationTrackingMode.TrackingModeOnWithoutHeading,
	 *	 CurrentLocationTrackingMode.TrackingModeOnWithHeading) 단말의 위치에 해당하는 지도 좌표와 위치 정확도가 주기적으로 delegate 객체에 통보된다.
	 */
	public void onCurrentLocationUpdate(final MapView mapView, final MapPoint currentLocation, final float arg2) {
		// 현재 위치를 저장
		mapPointGeo = currentLocation.getMapPointGeoCoord();
		if(mMapView != null && friendThread == null){
			friendThread = new FriendThread();
			friendThread.start();	// 친구 목록 가져오기
		}
		// 서버에 주기적으로 내 위치를 갱신한다.
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Vector<NameValuePair> vars = new Vector<NameValuePair>();
				while(true){				
					try {
						// HTTP post 메서드를 이용하여 데이터 업로드 처리
			            vars.add(new BasicNameValuePair("user_index", String.valueOf(myIndex)));	
			            vars.add(new BasicNameValuePair("lat", String.valueOf(mapPointGeo.latitude)));	
			            vars.add(new BasicNameValuePair("lng", String.valueOf(mapPointGeo.longitude)));	
			            String url = "http://" + SERVER_URL + UPDATE_URL;
			            HttpPost request = new HttpPost(url);
						UrlEncodedFormEntity entity = null;
						entity = new UrlEncodedFormEntity(vars, "UTF-8");
						request.setEntity(entity);
		                ResponseHandler<String> responseHandler = new BasicResponseHandler();
		                HttpClient client = new DefaultHttpClient();
		                final String responseBody = client.execute(request, responseHandler);	// 전송
		        		 SLog.i(responseBody);
		                if ( responseBody.trim().contains("done")  ) {	// 알림이 잇으면 체크
		                		  break;
		                }
					} catch (Exception e) {}
				}
			}
		});
	}

	public void onCurrentLocationUpdateCancelled(final MapView arg0) {
		// TODO Auto-generated method stub

	}

	public void onCurrentLocationUpdateFailed(final MapView arg0) {
		// TODO Auto-generated method stub

	}

	public void onMapViewCenterPointMoved(final MapView arg0, final MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	public void onMapViewDoubleTapped(final MapView arg0, final MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	public void onMapViewLongPressed(final MapView arg0, final MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	public void onMapViewSingleTapped(final MapView arg0, final MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	public void onMapViewZoomLevelChanged(final MapView arg0, final int arg1) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
    protected Dialog onCreateDialog(int id) {
        // XML로 정의된 뷰를 얻습니다.
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.dialog, 
                (ViewGroup)findViewById(R.id.dialog_root));
        
        // 위에서 얻은 뷰를 다이얼로그 박스에 적용합니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alert = builder.create();
        // custom view에서 사용한 위젯들의 동작을 정의합니다.
        TextView message = (TextView) view.findViewById(R.id.user_name);
     //   message.setText(R.string.dialog_exit_text);
        return alert;
    }
	
	
	/**
	 * 키 인증 처리 결과
	 * 인증 성공시 현위치를 검색하고 주변 친구들을 인증실패시 종료한다.
	 */
	public void onDaumMapOpenAPIKeyAuthenticationResult(final MapView arg0, final int result,
			final String arg2) {
		// 인증 성공시에 현위치 검색
		if(result == API_RESULT_OK){	// 인증 성공
			// 내 현위치를 찿는다.
			mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
			mMapView.setZoomLevel(4, true);	

		}else{
			finish();	// 앱종료
		}
	}
	
	private static double getDistanceArc(double sLat, double sLong, double dLat, double dLong){  
        final int radius=6371009;

        double uLat=Math.toRadians(sLat-dLat);
        double uLong=Math.toRadians(sLong-dLong);
        double a = Math.sin(uLat/2) * Math.sin(uLat/2) + Math.cos(Math.toRadians(sLong)) * Math.cos(Math.toRadians(dLong)) * Math.sin(uLong/2) * Math.sin(uLong/2);  
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));  
        double distance = radius * c;
        return Double.parseDouble(String.format("%.3f", distance/1000));
    }
	
	
	
	/**
	 *  get방식으로 서버에서 조회값을 보내  서버에서 생성된 xml를 parsing하여
	 *  접속하고 있는 전체 친구리스트를 가져온다.
	 *  
	 * @return
	 * 	접속된 전체 유저 리스트
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<FriendData> processXML() throws XmlPullParserException, IOException {
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    XmlPullParser parser = factory.newPullParser();
	    InputStreamReader isr = null;
	    BufferedReader br = null;
		List<FriendData> list = new ArrayList<FriendData>();
	    // namespace 지원
	    factory.setNamespaceAware(true);
	    // url 설정
	    URL url = new URL("http://" + SERVER_URL + FRIEND_URL + "?me=" + myIndex);
	    URLConnection conn = url.openConnection();
	    conn.setConnectTimeout(5000);
	    conn.setDoInput(true);
	    conn.setDoOutput(true);	    
		try{
		    isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
		    br = new BufferedReader(isr);
		    StringBuilder friendsXMl = new StringBuilder();
		    String line = "";
		    while((line = br.readLine()) != null){
		    	friendsXMl.append(line);
		    }
		    String decodeXMl = URLDecoder.decode(friendsXMl.toString());
		    parser.setInput(new StringReader(decodeXMl));
			int eventType = -1;
	
			FriendData friend = null;
			SLog.i("decodeXMl " + decodeXMl);
			while(eventType != XmlResourceParser.END_DOCUMENT){	// 문서의 마지막이 아닐때까지
				if(eventType == XmlResourceParser.START_TAG){	// 이벤트가 시작태그면
					String strName = parser.getName();
					if(strName.contains("friend")){				// userName 시작이면 객체생성
						friend = new FriendData();
						SLog.i("add~~~!");
					}else if(strName.equals("userName")){				// userName 시작이면 객체생성
						friend.setUserName(parser.nextText());	
					}else if(strName.equals("lat")){			// lat 얻어오기	
						friend.setLat(parser.nextText());	  
					}else if(strName.equals("lng")){			// lng 얻어오기	
						friend.setLng(parser.nextText());	  
					}else if(strName.equals("imageFile")){		// imageFile 얻어오기	
						friend.setImageFile(parser.nextText());	  						
					}else if(strName.equals("lecture")){			
						friend.setLecture(parser.nextText());
						list.add(friend);
					}
	
				}
				eventType = parser.next();	// 다음이벤트로..
			}
		}finally{
			if(isr != null){
				isr.close();
			}
			
			if(br != null){
				br.close();
			}
		}
		return list;
	}	
	
	
	/**
	 * 친구 목록을 불러오는 쓰레드 클래스
	 */
	private class FriendThread extends Thread{
		
		public FriendThread(){
			/*
        	MapMainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					dialog = ProgressDialog.show(MapMainActivity.this, "로딩중", "주변 친구 목록을 불러오는중입니다.", true);
				}
			});
			*/
		}
		

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			friendList = null;
			try {
				friendList = processXML();
				if(friendList != null){
					addPoiItem(friendList);
				}
				
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				/*
				if(dialog.isShowing()){
					dialog.dismiss();
				}
				*/
			}
		}
		
	}
	
	
	/**
	 *	나에게 온 메세지가 있는지 체크한 후 메시지가  핸들러에게 정보를 담아 알린다.
	 *	확인을 하면 상대방과 대화하기 창으로 이동한다.
	 */
	private class CheckMessageThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int userIndex = 0;
			while(true){
				// 5초마다 내용이 있는지 가져온다.
				try {
					Thread.sleep(MESSAGE_LOOP_TIME);
					userIndex = checkMessage();
					if(userIndex > 0){
						Message msg = new Message();
						msg.what = 1;
						msg.arg1 = userIndex;
						handler.sendMessage(msg);
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 서버에 확인하지 않은 메시지가 있는지 체크한후 메시지가 있으면
		 * true 아니면 false
		 * @return
		 */
		private int checkMessage(){
			int userIndex = 0;
			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			try {

				// HTTP post 메서드를 이용하여 데이터 업로드 처리
	            vars.add(new BasicNameValuePair("idx", "36"));			// 이름
	            String url = "http://" + SERVER_URL + CHECK_MSG;
	            HttpPost request = new HttpPost(url);
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                final String responseBody = client.execute(request, responseHandler);	// 전송
        		 SLog.i(responseBody);
                if ( responseBody.trim().contains("noti")  ) {	// 알림이 잇으면 체크
                		  String[] arr =responseBody.trim().split("noti");
        				  userIndex = Integer.valueOf(arr[1]);
                }
            } catch (ClientProtocolException e) {
            	SLog.e("Failed to get playerId (protocol): ", e);
            } catch (IOException e) {
            	SLog.e("Failed to get playerId (io): ", e);
			} catch (Exception e) {
				dialog.dismiss(); // 프로그레스 다이얼로그 닫기
				SLog.e( "파일 업로드 에러", e);
			}
			
			return userIndex;
		}
		
	}


	@Override
	public void onBackPressed() {	//  뒤로 가기버튼 클릭시 종료 여부
		finishDialog(this);
	}
	
	
	
	
	/**
	 * 앱 종료시 서버에 로그아웃을 알린다.
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LogoutThread logoutThread = new LogoutThread();
		logoutThread.setDaemon(true);
		logoutThread.start();
		super.onDestroy();
	
	}
	

    /** 
     * 옵션 메뉴 생성
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "강의선택").setIcon(android.R.drawable.ic_menu_info_details);  
    	menu.add(0,2,0, "범위선택").setIcon(android.R.drawable.ic_menu_help);  
    	//item.setIcon();
    	return true;
    }


    /**
     * 옵션 메뉴 선택시 처리 
     * 1번 선택시 수강선택,
     * 2번 선택시 주변 친구 범위 선택
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    		case 1:
    			final String[] arr = lecture.split(",");
    			if(arr.length <=0){	// 수강명이 없으면 리턴 처리
    				return false;
    			}
				new AlertDialog.Builder(this)
				.setTitle("수업선택")
				.setSingleChoiceItems(arr, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selectedLecture = arr[which];
								dialog.dismiss();
								// 이전 아이템을 지운수 친구 목록 새로 갱신
								mMapView.removeAllPOIItems();
								friendThread = new FriendThread();
								friendThread.start();
							}
						}).setNegativeButton("취소", null).show();
    			return true;
    		case 2:
    			//appInfoDialog(this);
    			return true;
    	}
    	return false;
    }	


	/**
	 *	이미지를 서버에서 받아오기 위한 쓰레드 클래스
	 */
	private class ImageLoadThread extends Thread{
		private ImageView imageView;
		private String filename;
		private Bitmap bitmap = null;
		/**
		 * @param imageView
		 * 	다운로드한 이미지를 붙일 이미지뷰
		 * @param filename
		 * 	파일명
		 */
		public ImageLoadThread(ImageView imageView, String filename){
			this.imageView = imageView;
			this.filename = filename;
		}
		
		@Override
		public void run() {
			try {
				bitmap = getBitmapByUrl(IMAGE_DIR + filename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(bitmap != null){
				MapMainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageView.setImageBitmap(bitmap);
					}
				});
			}
		}
	}
	
	/**
	 *	로그아웃 쓰레드 클래스
	 *	서버에게 로그아웃을 알려 다른 친구들에게 보이지 안도록 한다.
	 */
	private class LogoutThread extends Thread{
		
		@Override
		public void run(){
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			while(true){
				try {
					// HTTP post 메서드를 이용하여 데이터 업로드 처리
		            vars.add(new BasicNameValuePair("user_index", String.valueOf(myIndex)));			
		            String url = "http://" + SERVER_URL + LOGOUT_URL;// + "?" + URLEncodedUtils.format(vars, null);
		            HttpPost request = new HttpPost(url);
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, "UTF-8");
					request.setEntity(entity);
		            ResponseHandler<String> responseHandler = new BasicResponseHandler();
		            HttpClient client = new DefaultHttpClient();
		            final String responseBody = client.execute(request, responseHandler);	// 전송
		    		 SLog.i(responseBody);
		            if (responseBody.trim().contains("ok")) {	// 정상 메세지일때만 종료
						  break;
		            }
		        } catch (ClientProtocolException e) {
		        	SLog.e("Failed to get playerId (protocol): ", e);
		        } catch (IOException e) {
		        	SLog.e("Failed to get playerId (io): ", e);
				} catch (Exception e) {
					dialog.dismiss(); // 프로그레스 다이얼로그 닫기
					SLog.e( "파일 업로드 에러", e);
				}		
			}
		}
	}
	
	
	/**
	 * image url에서 비트맵을 추출한다.
	 * @param address
	 * 	이미지 url 주소
	 * @return
	 * @throws Exception
	 */
	public Bitmap getBitmapByUrl(final String address) throws Exception { // url로부터 사진을 다운로드한다.
		InputStream           is   = null;
		ByteArrayOutputStream baos = null;

		try {
			URL url = new URL(address);
			Object content = url.getContent();
			is = (InputStream)content;

			baos = new ByteArrayOutputStream();
			byte[] byteBuffer = new byte[1024];
			byte[] byteData = null;
			int nLength = 0;
			while((nLength = is.read(byteBuffer)) > 0) {
				baos.write(byteBuffer, 0, nLength);
			}
			byteData = baos.toByteArray();
			Bitmap img = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);

			if(img == null) {
				throw new Exception("Bitmap Fail!" + " - [" + address + "]");
			}

			return img; // 이미지를 반환

		} catch(Exception e) {
			throw e;
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(Exception e) { }
			}

			if(baos != null) {
				try {
					baos.close();
				} catch(Exception e) { }
			}
		}
	}	
	
	
	

}

