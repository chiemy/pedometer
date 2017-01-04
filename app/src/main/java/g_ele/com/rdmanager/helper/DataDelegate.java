package g_ele.com.rdmanager.helper;

import android.location.Location;

/**
 * DataDelegate
 * Created by aki on 1/9/2016.
 */

interface DataDelegate {
    void distanceChanged(Double distance);
    void stepsChanged(Integer steps);
    void coordinateChanged(Location location);
}
