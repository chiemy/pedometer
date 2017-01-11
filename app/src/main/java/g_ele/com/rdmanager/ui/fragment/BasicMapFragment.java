package g_ele.com.rdmanager.ui.fragment;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;

import g_ele.com.rdmanager.R;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public abstract class BasicMapFragment extends Fragment implements LocationSource, AMapLocationListener {
    private MapView mapView;
    private AMap aMap;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMap(savedInstanceState, view);
    }

    private void initMap(@Nullable Bundle savedInstanceState, View root) {
        mapView = getMapView(root);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    LatLng latLng = aMap.getProjection().fromScreenLocation(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
                    Log.d("chiemy", "onTouch: " + latLng);
                }

            }
        });
        setUpMap(mapView.getMap());
    }

    public AMap getaMap() {
        return aMap;
    }

    protected abstract MapView getMapView(View root);

    /**
     * 设置一些amap的属性
     */
    protected void setUpMap(AMap aMap) {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.setTrafficEnabled(false);// 实时交通状况
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 卫星地图模式
        aMap.setMyLocationEnabled(true);

        setupLocationStyle(getMyLocationStyle());
    }

    private void setupLocationStyle(MyLocationStyle myLocationStyle) {
        // 将自定义的 myLocationStyle 对象添加到地图上
        if (myLocationStyle != null) {
            aMap.setMyLocationStyle(myLocationStyle);
        }
    }

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    protected MyLocationStyle getMyLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        // 自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        return myLocationStyle;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                // 显示系统小蓝点
                mListener.onLocationChanged(amapLocation);
                aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ":" + amapLocation.getErrorInfo();
                Log.d("BasicMapFragment", "onLocationChanged: " + errText);
            }
        }
    }

    private AMapLocationClientOption createLocationClientOption() {
        return new AMapLocationClientOption();
    }

    protected boolean isOnceLocation() {
        return false;
    }

    private AMapLocationClient createLocationClient() {
        return new AMapLocationClient(getActivity());
    }

    private AMapLocationClient mLocationClient;
    private LocationSource.OnLocationChangedListener mListener;
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {

            AMapLocationClientOption locationClientOption = createLocationClientOption();
            setUpLocationClientOption(locationClientOption);


            mLocationClient = createLocationClient();
            setUpLocationClient(mLocationClient, locationClientOption);
        }
    }

    protected void setUpLocationClientOption(AMapLocationClientOption locationClientOption) {
        // 单次定位
        if (isOnceLocation()) {
            locationClientOption.setOnceLocation(true);
        }

        // 设置首次定位是否等待GPS定位结果
        // 只有在单次定位高精度定位模式下有效
        // 设置为true时，会等待GPS定位结果返回，最多等待30秒，若30秒后仍无GPS结果返回，返回网络定位结果
        // locationClientOption.setGpsFirst(true);

        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
    }

    protected void setUpLocationClient(AMapLocationClient locationClient, AMapLocationClientOption locationClientOption) {
        locationClient.setLocationListener(this);
        // 设置定位参数
        locationClient.setLocationOption(locationClientOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        locationClient.startLocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
