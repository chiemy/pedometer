package g_ele.com.rdmanager.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import g_ele.com.rdmanager.listeners.LocationChangeListener;

/**
 * Using gps to record distance data
 * Created by aki on 1/9/2016.
 */

class GPSManager {
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Context mContext;
    private Location lastLocation;
    LocationChangeListener delegate;
    GPSManager(final Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location.getAccuracy() > 30) {
                    // 忽略精度过低的数据
                    return;
                }
                if (delegate != null) {
                    delegate.onLocationChanged(lastLocation, location);
                }
                lastLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    void start() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
        }
    }

    void stop() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
