package g_ele.com.rdmanager.listeners;

import com.amap.api.location.AMapLocation;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public interface LocationChangeListener {

    void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation);
}
