package g_ele.com.rdmanager;

import android.location.Location;

/**
 * PedometerListener
 * Created by aki on 1/9/2016.
 */

public interface PedometerListener {
    void durationChanged(Integer duration); // in seconds
    void distanceChanged(Double distance);
    void stepsChanged(Integer steps);
    void paceChanged(Double pace);
    void calorieChanged(Integer calorie);
    void coordinateChanged(Location location);
}
