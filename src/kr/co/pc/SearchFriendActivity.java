package kr.co.pc;


import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapView.CurrentLocationEventListener;
import net.daum.mf.map.api.MapView.MapViewEventListener;
import net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener;
import net.daum.mf.map.api.MapView.POIItemEventListener;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 *	지도에서 친구찾기 엑티비티
 *	내 위치를 기준으로 하여 주변 친구들을 찾는다.
 *	POI Item 이 보이면 해당 친구도 앱을 실행하고 있는것으로 판별
 *	친구 아이템을 선택시 대화여부를 확인후 1:1 대화에 참여한다.
 *	수강하는 과목에 따라 친구들을 필터링기능도 추가한다.
 */
public class SearchFriendActivity extends BaseActivity implements
		OpenAPIKeyAuthenticationResultListener, MapViewEventListener,
		CurrentLocationEventListener, POIItemEventListener, iChatConstant {

	private MapView mMapView;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		initLayout();
	}

	private void initLayout() {
		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout)findViewById(R.id.map_root);
		// map 설정
		mMapView = new MapView(this);
		mMapView.setDaumMapApiKey(DAUM_MMAPS_ANDROID_APIKEY);
		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
		mMapView.setMapViewEventListener(this);
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		mMapView.setZoomLevel(14, true);			// 줌설정
		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.570705,126.958008), true);	// 초기 지도 위치 설정

		ll.addView(mMapView);
	}



	public void onCalloutBalloonOfPOIItemTouched(final MapView arg0, final MapPOIItem arg1) {
		// TODO Auto-generated method stub

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

	public void onCurrentLocationUpdate(final MapView mapView, final MapPoint currentLocation, final float arg2) {
		MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
		// TODO Auto-generated method stub

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

	/**
	 * 키 인증 처리 결과
	 * 인증 성공시 현위치 검색
	 */
	public void onDaumMapOpenAPIKeyAuthenticationResult(final MapView arg0, final int result,
			final String arg2) {
		// 인증 성공시에 현위치 검색
		if(result == API_RESULT_OK){	// 인증 성공
			mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
			mMapView.setZoomLevel(4, true);
		}else{
			finish();	// 앱종료
		}
	}

}
