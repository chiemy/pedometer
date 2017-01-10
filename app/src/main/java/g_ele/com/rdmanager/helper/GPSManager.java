package g_ele.com.rdmanager.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import g_ele.com.rdmanager.listeners.LocationChangeListener;

/**
 * Using gps to record distance data
 * Created by aki on 1/9/2016.
 */

public class GPSManager implements AMapLocationListener {
    private AMapLocationClient mLocationClient;
    private Context mContext;
    private LocationChangeListener delegate;
    private long mInterval = 2000;

    public GPSManager(final Context context) {
        mContext = context;
    }

    @NonNull
    private AMapLocationClientOption initAMapLocationClientOption() {
        //初始化定位参数
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(false);
        //设置是否只定位一次,默认为false
        locationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        locationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        locationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        locationOption.setInterval(mInterval);
        return locationOption;
    }

    public void setDelegate(LocationChangeListener delegate) {
        this.delegate = delegate;
    }

    public void setLocationTriggerInterval(long interval) {
        mInterval = Math.min(interval, 2000);
    }

    public long getLocationTiggerInterval() {
        return mInterval;
    }

    public void start() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mLocationClient == null) {
                mLocationClient = new AMapLocationClient(mContext);
                AMapLocationClientOption locationOption = initAMapLocationClientOption();
                //给定位客户端对象设置定位参数
                mLocationClient.setLocationOption(locationOption);
                mLocationClient.setLocationListener(this);
            }
            mLocationClient.startLocation();
        }
    }

    public void stop() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mLocationClient != null) {
                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
            }
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
