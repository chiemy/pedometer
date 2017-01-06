package g_ele.com.rdmanager.listeners;

import android.location.Location;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public interface LocationChangeListener {
    void onLocationChanged(Location oldLocation, Location newLocation);
}
