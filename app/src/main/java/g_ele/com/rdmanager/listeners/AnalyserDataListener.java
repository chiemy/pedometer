package g_ele.com.rdmanager.listeners;

import android.location.Location;

import g_ele.com.rdmanager.helper.SportAnalyser;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public abstract class AnalyserDataListener implements PedometerListener {
    private SportAnalyser mSportAnalyser;
    private double mDistance;

    /**
     * @see SportAnalyser
     * @param analyser 运动分析工具
     */
    public AnalyserDataListener(SportAnalyser analyser) {
        mSportAnalyser = analyser;
    }

    @Override
    public void onDurationChanged(int duration) {
    }

    @Override
    public void onLocationChanged(Location oldLocation, Location newLocation) {
        float distance = newLocation.distanceTo(oldLocation);
        if (distance != 0 && distance < 1000) {
            mDistance += distance;
            distanceChange(mDistance);
        }
    }

    @Override
    public void onStepChange(int steps) {
        distanceChange(mSportAnalyser.getStepDistance(steps));
    }

    private void distanceChange(double distance) {
        onCalorieChange(mSportAnalyser.getCalorieForDistance(distance));
        onDistanceChange(distance);
    }

    public abstract void onCalorieChange(int calorie);

    public abstract void onDistanceChange(double distance);
}
