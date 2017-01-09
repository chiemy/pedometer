package g_ele.com.rdmanager.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import g_ele.com.rdmanager.listeners.LocationChangeListener;

/**
 * Using gps to record distance data
 * Created by aki on 1/9/2016.
 */

class GPSManager implements AMapLocationListener {
    private AMapLocationClient mLocationClient;
    private Context mContext;
    LocationChangeListener delegate;
    GPSManager(final Context context) {
        mContext = context;
        mLocationClient = new AMapLocationClient(context);
        //初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(false);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.setLocationListener(this);
    }

    void start() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationClient.startLocation();
        }
    }

    void stop() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    private AMapLocation oldLocation;
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (delegate != null) {
            delegate.onLocationChanged(oldLocation, aMapLocation);
            oldLocation = aMapLocation;
        }
    }
}
