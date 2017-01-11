package g_ele.com.rdmanager.listeners;

import g_ele.com.rdmanager.helper.SportAnalyser;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public abstract class AnalyserDataListener implements PedometerListener {
    private static final int PACE_TRIGGER_INTERVAL = 3;
    private SportAnalyser mSportAnalyser;
    private int mDuration;
    private int mPaceTriggerDuration;
    private float mDistance;

    /**
     * @see SportAnalyser
     * @param analyser 运动分析工具
     */
    public AnalyserDataListener(SportAnalyser analyser) {
        mSportAnalyser = analyser;
    }

    @Override
    public void onDurationChanged(int seconds) {
        mDuration = seconds;
        if (mDuration - mPaceTriggerDuration >= PACE_TRIGGER_INTERVAL) {
            mPaceTriggerDuration = mDuration;
            paceChange();
        }
    }

    @Override
    public void onStepChange(int steps) {
        distanceChange(mSportAnalyser.getStepDistance(steps));
    }

    private void distanceChange(float distance) {
        mDistance = distance;
        paceChange();
        onCalorieChange(mSportAnalyser.getCalorieForDistance(distance));
        onDistanceChange(distance);
    }

    private void paceChange() {
        float pace = 0;
        if (mDistance > 0) {
            pace = 1000 * mDuration / mDistance;
        }
        onPaceChanged(pace);
    }

    public abstract void onPaceChanged(float pace);

    public abstract void onCalorieChange(float calorie);

    public abstract void onDistanceChange(float meters);
}
