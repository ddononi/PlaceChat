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
 *	���� ��Ƽ��Ƽ
 *	�� ��ġ�� �ֺ����� ģ�� ����� ���������� ��Ÿ����.
 *	�������� ���ý� ģ�� ������ �����ָ� ��ȭ�� �Ҽ� �ִ�.
 *	�����ֱ⸦ �������� ģ������ �� �޽����� Ȯ���Ѵ�.
 */
public class MapMainActivity extends BaseActivity implements
OpenAPIKeyAuthenticationResultListener, MapViewEventListener,
CurrentLocationEventListener, POIItemEventListener {
	private MapView mMapView = null;
	private List<FriendData> friendList = null;
	private List<FriendData> showingFriendList = null;	// ������ ��Ÿ�� ģ�� ����Ʈ
	private ProgressDialog dialog = null;
	private CheckMessageThread cmThread = null;
	private MapPoint.GeoCoordinate mapPointGeo;	// ����ġ�� ���� point ��ü
	private FriendThread friendThread;
	private String selectedLecture = "";	// ���õ� ������
	// ui ó���� ���� �ڵ鷯
	private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case 1:
    			final int userIndex = msg.arg1;
    			 AlertDialog.Builder builder = new AlertDialog.Builder(MapMainActivity.this);
    			 builder.setMessage("���ο� �޼����� �ֽ��ϴ�.").setPositiveButton("����", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MapMainActivity.this, ChatActivity.class);
						intent.putExtra("userIndex", userIndex);
						startActivity(intent);
					}
				}).setCancelable(false);
    			 /*
    			 .setNegativeButton("���", new OnClickListener() {
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
	 * �� Ű ���� , �̺�Ʈ ���� , ��Ÿ �ʱ�ȭ ����
	 */
	private void initLayout() {
		// TODO Auto-generated method stub
		LinearLayout linearLayout = new LinearLayout(this);
		// map ����
		mMapView = new MapView(this);
		mMapView.setDaumMapApiKey(DAUM_MMAPS_ANDROID_APIKEY);
		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
		mMapView.setMapViewEventListener(this);
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		mMapView.setZoomLevel(14, true);			// �ܼ���
		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.570705,126.958008), true);	// �ʱ� ���� ��ġ ����
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		linearLayout.addView(mMapView);
		
		setContentView(linearLayout);
	}

	/**
	 * ģ������ �� ������ ������ �� ����
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// �޽����� �ִ��� üũ
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
	 *��� ģ����� �����۵��� �����.
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// ��� ģ����� �����۵��� �����.
		mMapView.removeAllPOIItems();
	}

	/**
	 * �ݰ� �ֺ� ģ���������� ������ POIitem�� �����Ѵ�.
	 * ģ�� ������ ������ ����ó���ϰ� ������ �ֺ��� �ִ��� Ȯ���ϰ�
	 * �ʺ信 �������� ������ش�.
	 */
	private void addPoiItem(List<FriendData> list){
		
		// ģ���� ������ ���� ó��
		if(list == null || list.size() == 0){
        	MapMainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(MapMainActivity.this, "������ ģ���� �����ϴ�.", Toast.LENGTH_SHORT).show();
				}
			});	
        	return;
		}
		showingFriendList = new ArrayList<FriendData>();
		// poi �迭
		MapPOIItem poiItem;
		int i = 0;
		for(FriendData data : list){	// ����Ʈ �� ��ŭ POI�������� ����
			double lat = Double.parseDouble(data.getLat());
			double lng = Double.parseDouble(data.getLng());
			// �����Ÿ� �̻��̸� ȭ�鿡 ������ �ʴ´�.
			MapPoint.GeoCoordinate coord;
			if(mapPointGeo == null){
				coord = mMapView.getMapCenterPoint().getMapPointGeoCoord();
			}else{
				coord = mapPointGeo;
			}
			double distance = getDistanceArc(lat,  lng, coord.latitude, coord.longitude);
				SLog.i("distance-->" + distance);
			if(distance > 1){	// 1km �̻��̸�
				continue;
			}else if(data.getLecture().contains(selectedLecture) == false){	// ���� �������� Ȯ��
				continue;
			}
			
			// poi ����
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
		//	���� ȭ�鿡 �߰��� ��� POI(Point Of Interest) Item���� ȭ�鿡 ��Ÿ������ ���� ȭ�� �߽ɰ� Ȯ��/��� ������ �ڵ����� �����Ѵ�.
		mMapView.fitMapViewAreaToShowAllPOIItems();
		
	}
	
	/**
	 * �ش� ����ڰ� ��ǳ���� Ŭ��������� ����� �ƹ�Ÿ �̹����� ����ڸ��� �����ִ�
	 * ���̾�α׸� ����� ��ȭ���� ���θ� Ȯ���Ѵ�.
	 * ��ȭ�ϱⰡ ���õǸ� 1:1 ä�ù� ��Ƽ��� �ѱ��.
	 */
	public void onCalloutBalloonOfPOIItemTouched(final MapView v, final MapPOIItem item) {
		final FriendData data = showingFriendList.get(item.getTag());	// tag�� �̿��Ͽ� ģ��������ü�� ���´�.
		
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.dialog, 
                (ViewGroup)findViewById(R.id.dialog_root));
        
        // ������ ���� �並 ���̾�α� �ڽ��� �����մϴ�.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // custom view���� ����� �������� ������ �����մϴ�.
        TextView message = (TextView) view.findViewById(R.id.user_name);
        TextView lecture = (TextView) view.findViewById(R.id.user_lecture);
        ImageView avataIv = (ImageView) view.findViewById(R.id.avata);
        new ImageLoadThread(avataIv, data.getImageFile()).start();

        message.setText("�̸� : " + data.getUserName());
        lecture.setText("���� : " + data.getLecture());
        //message.setText("���� : " + item.getLece());
        builder.setView(view);
        // ��ȭ�ϱ⸦ �����ϸ� ��ȭ��Ƽ��Ƽȭ������ �ѱ��.
        builder.setPositiveButton("��ȭ�ϱ�", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MapMainActivity.this, ChatActivity.class);
				intent.putExtra("userIndex", data.getIndex());	// ģ�� �ε����� �����Ѵ�.
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
	 *	�ܸ��� ����ġ ��ǥ���� �뺸���� �� �ִ�. MapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode) 
	 *	�޼ҵ带 ���� ����� ����ġ Ʈ��ŷ ����� ���� ���(CurrentLocationTrackingMode.TrackingModeOnWithoutHeading,
	 *	 CurrentLocationTrackingMode.TrackingModeOnWithHeading) �ܸ��� ��ġ�� �ش��ϴ� ���� ��ǥ�� ��ġ ��Ȯ���� �ֱ������� delegate ��ü�� �뺸�ȴ�.
	 */
	public void onCurrentLocationUpdate(final MapView mapView, final MapPoint currentLocation, final float arg2) {
		// ���� ��ġ�� ����
		mapPointGeo = currentLocation.getMapPointGeoCoord();
		if(mMapView != null && friendThread == null){
			friendThread = new FriendThread();
			friendThread.start();	// ģ�� ��� ��������
		}
		// ������ �ֱ������� �� ��ġ�� �����Ѵ�.
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Vector<NameValuePair> vars = new Vector<NameValuePair>();
				while(true){				
					try {
						// HTTP post �޼��带 �̿��Ͽ� ������ ���ε� ó��
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
		                final String responseBody = client.execute(request, responseHandler);	// ����
		        		 SLog.i(responseBody);
		                if ( responseBody.trim().contains("done")  ) {	// �˸��� ������ üũ
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
        // XML�� ���ǵ� �並 ����ϴ�.
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.dialog, 
                (ViewGroup)findViewById(R.id.dialog_root));
        
        // ������ ���� �並 ���̾�α� �ڽ��� �����մϴ�.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alert = builder.create();
        // custom view���� ����� �������� ������ �����մϴ�.
        TextView message = (TextView) view.findViewById(R.id.user_name);
     //   message.setText(R.string.dialog_exit_text);
        return alert;
    }
	
	
	/**
	 * Ű ���� ó�� ���
	 * ���� ������ ����ġ�� �˻��ϰ� �ֺ� ģ������ �������н� �����Ѵ�.
	 */
	public void onDaumMapOpenAPIKeyAuthenticationResult(final MapView arg0, final int result,
			final String arg2) {
		// ���� �����ÿ� ����ġ �˻�
		if(result == API_RESULT_OK){	// ���� ����
			// �� ����ġ�� �O�´�.
			mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
			mMapView.setZoomLevel(4, true);	

		}else{
			finish();	// ������
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
	 *  get������� �������� ��ȸ���� ����  �������� ������ xml�� parsing�Ͽ�
	 *  �����ϰ� �ִ� ��ü ģ������Ʈ�� �����´�.
	 *  
	 * @return
	 * 	���ӵ� ��ü ���� ����Ʈ
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<FriendData> processXML() throws XmlPullParserException, IOException {
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    XmlPullParser parser = factory.newPullParser();
	    InputStreamReader isr = null;
	    BufferedReader br = null;
		List<FriendData> list = new ArrayList<FriendData>();
	    // namespace ����
	    factory.setNamespaceAware(true);
	    // url ����
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
			while(eventType != XmlResourceParser.END_DOCUMENT){	// ������ �������� �ƴҶ�����
				if(eventType == XmlResourceParser.START_TAG){	// �̺�Ʈ�� �����±׸�
					String strName = parser.getName();
					if(strName.contains("friend")){				// userName �����̸� ��ü����
						friend = new FriendData();
						SLog.i("add~~~!");
					}else if(strName.equals("userName")){				// userName �����̸� ��ü����
						friend.setUserName(parser.nextText());	
					}else if(strName.equals("lat")){			// lat ������	
						friend.setLat(parser.nextText());	  
					}else if(strName.equals("lng")){			// lng ������	
						friend.setLng(parser.nextText());	  
					}else if(strName.equals("imageFile")){		// imageFile ������	
						friend.setImageFile(parser.nextText());	  						
					}else if(strName.equals("lecture")){			
						friend.setLecture(parser.nextText());
						list.add(friend);
					}
	
				}
				eventType = parser.next();	// �����̺�Ʈ��..
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
	 * ģ�� ����� �ҷ����� ������ Ŭ����
	 */
	private class FriendThread extends Thread{
		
		public FriendThread(){
			/*
        	MapMainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					dialog = ProgressDialog.show(MapMainActivity.this, "�ε���", "�ֺ� ģ�� ����� �ҷ��������Դϴ�.", true);
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
	 *	������ �� �޼����� �ִ��� üũ�� �� �޽�����  �ڵ鷯���� ������ ��� �˸���.
	 *	Ȯ���� �ϸ� ����� ��ȭ�ϱ� â���� �̵��Ѵ�.
	 */
	private class CheckMessageThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int userIndex = 0;
			while(true){
				// 5�ʸ��� ������ �ִ��� �����´�.
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
		 * ������ Ȯ������ ���� �޽����� �ִ��� üũ���� �޽����� ������
		 * true �ƴϸ� false
		 * @return
		 */
		private int checkMessage(){
			int userIndex = 0;
			// http �� ���� �̸� �� �� �÷���
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			try {

				// HTTP post �޼��带 �̿��Ͽ� ������ ���ε� ó��
	            vars.add(new BasicNameValuePair("idx", "36"));			// �̸�
	            String url = "http://" + SERVER_URL + CHECK_MSG;
	            HttpPost request = new HttpPost(url);
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                final String responseBody = client.execute(request, responseHandler);	// ����
        		 SLog.i(responseBody);
                if ( responseBody.trim().contains("noti")  ) {	// �˸��� ������ üũ
                		  String[] arr =responseBody.trim().split("noti");
        				  userIndex = Integer.valueOf(arr[1]);
                }
            } catch (ClientProtocolException e) {
            	SLog.e("Failed to get playerId (protocol): ", e);
            } catch (IOException e) {
            	SLog.e("Failed to get playerId (io): ", e);
			} catch (Exception e) {
				dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
				SLog.e( "���� ���ε� ����", e);
			}
			
			return userIndex;
		}
		
	}


	@Override
	public void onBackPressed() {	//  �ڷ� �����ư Ŭ���� ���� ����
		finishDialog(this);
	}
	
	
	
	
	/**
	 * �� ����� ������ �α׾ƿ��� �˸���.
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
     * �ɼ� �޴� ����
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "���Ǽ���").setIcon(android.R.drawable.ic_menu_info_details);  
    	menu.add(0,2,0, "��������").setIcon(android.R.drawable.ic_menu_help);  
    	//item.setIcon();
    	return true;
    }


    /**
     * �ɼ� �޴� ���ý� ó�� 
     * 1�� ���ý� ��������,
     * 2�� ���ý� �ֺ� ģ�� ���� ����
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    		case 1:
    			final String[] arr = lecture.split(",");
    			if(arr.length <=0){	// �������� ������ ���� ó��
    				return false;
    			}
				new AlertDialog.Builder(this)
				.setTitle("��������")
				.setSingleChoiceItems(arr, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selectedLecture = arr[which];
								dialog.dismiss();
								// ���� �������� ����� ģ�� ��� ���� ����
								mMapView.removeAllPOIItems();
								friendThread = new FriendThread();
								friendThread.start();
							}
						}).setNegativeButton("���", null).show();
    			return true;
    		case 2:
    			//appInfoDialog(this);
    			return true;
    	}
    	return false;
    }	


	/**
	 *	�̹����� �������� �޾ƿ��� ���� ������ Ŭ����
	 */
	private class ImageLoadThread extends Thread{
		private ImageView imageView;
		private String filename;
		private Bitmap bitmap = null;
		/**
		 * @param imageView
		 * 	�ٿ�ε��� �̹����� ���� �̹�����
		 * @param filename
		 * 	���ϸ�
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
	 *	�α׾ƿ� ������ Ŭ����
	 *	�������� �α׾ƿ��� �˷� �ٸ� ģ���鿡�� ������ �ȵ��� �Ѵ�.
	 */
	private class LogoutThread extends Thread{
		
		@Override
		public void run(){
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			while(true){
				try {
					// HTTP post �޼��带 �̿��Ͽ� ������ ���ε� ó��
		            vars.add(new BasicNameValuePair("user_index", String.valueOf(myIndex)));			
		            String url = "http://" + SERVER_URL + LOGOUT_URL;// + "?" + URLEncodedUtils.format(vars, null);
		            HttpPost request = new HttpPost(url);
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, "UTF-8");
					request.setEntity(entity);
		            ResponseHandler<String> responseHandler = new BasicResponseHandler();
		            HttpClient client = new DefaultHttpClient();
		            final String responseBody = client.execute(request, responseHandler);	// ����
		    		 SLog.i(responseBody);
		            if (responseBody.trim().contains("ok")) {	// ���� �޼����϶��� ����
						  break;
		            }
		        } catch (ClientProtocolException e) {
		        	SLog.e("Failed to get playerId (protocol): ", e);
		        } catch (IOException e) {
		        	SLog.e("Failed to get playerId (io): ", e);
				} catch (Exception e) {
					dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
					SLog.e( "���� ���ε� ����", e);
				}		
			}
		}
	}
	
	
	/**
	 * image url���� ��Ʈ���� �����Ѵ�.
	 * @param address
	 * 	�̹��� url �ּ�
	 * @return
	 * @throws Exception
	 */
	public Bitmap getBitmapByUrl(final String address) throws Exception { // url�κ��� ������ �ٿ�ε��Ѵ�.
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

			return img; // �̹����� ��ȯ

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

