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
 *	�������� ģ��ã�� ��Ƽ��Ƽ
 *	�� ��ġ�� �������� �Ͽ� �ֺ� ģ������ ã�´�.
 *	POI Item �� ���̸� �ش� ģ���� ���� �����ϰ� �ִ°����� �Ǻ�
 *	ģ�� �������� ���ý� ��ȭ���θ� Ȯ���� 1:1 ��ȭ�� �����Ѵ�.
 *	�����ϴ� ���� ���� ģ������ ���͸���ɵ� �߰��Ѵ�.
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
		// map ����
		mMapView = new MapView(this);
		mMapView.setDaumMapApiKey(DAUM_MMAPS_ANDROID_APIKEY);
		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
		mMapView.setMapViewEventListener(this);
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setPOIItemEventListener(this);
		mMapView.setZoomLevel(14, true);			// �ܼ���
		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.570705,126.958008), true);	// �ʱ� ���� ��ġ ����

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
	 * Ű ���� ó�� ���
	 * ���� ������ ����ġ �˻�
	 */
	public void onDaumMapOpenAPIKeyAuthenticationResult(final MapView arg0, final int result,
			final String arg2) {
		// ���� �����ÿ� ����ġ �˻�
		if(result == API_RESULT_OK){	// ���� ����
			mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
			mMapView.setZoomLevel(4, true);
		}else{
			finish();	// ������
		}
	}

}
