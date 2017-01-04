package g_ele.com.rdmanager.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

import g_ele.com.rdmanager.PedometerListener;

import static g_ele.com.rdmanager.Constants.MODE_INDOOR;
import static g_ele.com.rdmanager.Constants.MSG_CALORIE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_DISTANCE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_DURATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_LOCATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_PACE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_STEP_CHANGE;

/**
 * RDManager
 * Created by aki on 1/9/2016.
 */

class RDManager implements DataDelegate {
    public Integer duration = 0;
    public Double distance = 0.0;
    public Integer steps = 0;
    public Double pace = 0.0;
    public Integer calorie = 0;
    public Location location;

    public Double userHeight = 1.7;
    public Double userWeight = 65.0;
    public Integer userGender = 1; // 0 for female, 1 for male
    public Integer userAge = 27;

    private Integer mode = 2; // 1 for indoor, 2 for outdoor

    private Timer mTimer;
    private Context mContext;
    private GPSManager mGpsManager;
    private ACManager mACManager;
    private SCManager mSCManager;
    private PedometerListener delegate;

    /*
    * 1: duration
    * 2: distance
    * 3: steps
    * 4: pace
    * 5: calorie
    * 6: location
    * */
    private Handler syncUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (delegate != null) {
                switch (msg.what) {
                    case MSG_DURATION_CHANGE:
                        delegate.durationChanged(duration);
                        break;
                    case MSG_DISTANCE_CHANGE:
                        delegate.distanceChanged(distance);
                        break;
                    case MSG_STEP_CHANGE:
                        delegate.stepsChanged(steps);
                        break;
                    case MSG_PACE_CHANGE:
                        delegate.paceChanged(pace);
                        break;
                    case MSG_CALORIE_CHANGE:
                        delegate.calorieChanged(calorie);
                        break;
                    case MSG_LOCATION_CHANGE:
                        delegate.coordinateChanged(location);
                        break;
                    default:
                        delegate.durationChanged(duration);
                        delegate.distanceChanged(distance);
                        delegate.stepsChanged(steps);
                        delegate.paceChanged(pace);
                        delegate.calorieChanged(calorie);
                        if (location != null) {
                            delegate.coordinateChanged(location);
                        }
                }
            }
        }
    };

    private static RDManager ourInstance = new RDManager();

    public static RDManager getInstance() {
        return ourInstance;
    }

    private RDManager() {

    }

    // 0:stop, 1: running, 2: paused
    public Integer getStatus() {
        if (mTimer != null) {
            return 1;
        } else {
            if (mACManager == null && mGpsManager == null && mSCManager == null) {
                return 0;
            } else {
                return 2;
            }
        }
    }

    public Integer getMode() {
        return this.mode;
    }

    public void setContext(Context c) {
        mContext = c;
    }

    public void setDelegate(PedometerListener d) {
        delegate = d;
    }

    public void setMode(int mode) {
        this.mode = mode;
        // mode = 1 => 室内模式
        // mode = 2 => 室外模式
        boolean needGpsManager = mode != MODE_INDOOR;
        if (needGpsManager) {
            initGpsManager();
        }
        if (hasStepDetector()) {
            initSCManager(!needGpsManager);
        } else {
            initACManager(!needGpsManager);
        }
    }

    private boolean hasStepDetector() {
        PackageManager pm = mContext.getPackageManager();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    private void initGpsManager() {
        if (mGpsManager == null) {
            mGpsManager = new GPSManager(mContext);
            mGpsManager.delegate = this;
        }
    }

    private void initSCManager(boolean asDistanceManager) {
        if (mSCManager == null) {
            mSCManager = new SCManager(mContext);
            mSCManager.stepsDelegate = this;
        }
        if (asDistanceManager) {
            mSCManager.distanceDelegate = this;
        } else {
            mSCManager.distanceDelegate = null;
        }
    }

    private void initACManager(boolean asDistanceManager) {
        if (mACManager == null) {
            mACManager = new ACManager(mContext);
            mACManager.userAge = userAge;
            mACManager.userGender = userGender;
            mACManager.userHeight = userHeight;
            mACManager.userWeight = userWeight;
            mACManager.stepsDelegate = this;
        }
        if (asDistanceManager) {
            mACManager.distanceDelegate = this;
        } else {
            mACManager.distanceDelegate = null;
        }
    }

    public void toggle() {
        if (mTimer == null) {
            start();
        } else {
            stop();
        }
    }

    public void start() {
        cancelTimer();
        mTimer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                duration += 1;
                if (distance != 0) {
                    pace = duration * (1000 / distance);
                }
                syncUIHandler.obtainMessage(MSG_DURATION_CHANGE).sendToTarget();
                syncUIHandler.obtainMessage(MSG_PACE_CHANGE).sendToTarget();
            }
        };
        mTimer.schedule(tt, 0, 1000);
        if (mACManager != null) {
            mACManager.start();
        }
        if (mSCManager != null) {
            mSCManager.start();
        }
        if (mGpsManager != null) {
            mGpsManager.start();
        }
    }

    public void triggerDelegate() {
        syncUIHandler.obtainMessage().sendToTarget();
    }

    public void stop() {
        cancelTimer();
        if (mGpsManager != null) {
            mGpsManager.stop();
        }

        if (mACManager != null) {
            mACManager.stop();
        }

        if (mSCManager != null) {
            mSCManager.stop();
        }
    }

    public void release() {
        stop();

        mGpsManager = null;
        mACManager = null;
        mSCManager = null;

        duration = 0;
        distance = 0.0;
        calorie = 0;
        steps = 0;
        pace = 0.0;
        location = null;
        syncUIHandler.obtainMessage().sendToTarget();
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void distanceChanged(Double distance) {
        this.distance += distance;
        if (this.distance != 0) {
            this.pace = this.duration * (1000 / this.distance);
            // https://www.zhihu.com/question/20354554
            this.calorie = (int) (this.userWeight * this.distance * 1.036 / 1000);
        }
        syncUIHandler.obtainMessage(MSG_DISTANCE_CHANGE).sendToTarget();
        if (this.distance != 0) {
            syncUIHandler.obtainMessage(MSG_PACE_CHANGE).sendToTarget();
            syncUIHandler.obtainMessage(MSG_CALORIE_CHANGE).sendToTarget();
        }
    }

    @Override
    public void stepsChanged(Integer steps) {
        this.steps += steps;
        syncUIHandler.obtainMessage(MSG_STEP_CHANGE).sendToTarget();
    }

    @Override
    public void coordinateChanged(Location location) {
        this.location = location;
        syncUIHandler.obtainMessage(MSG_LOCATION_CHANGE).sendToTarget();
    }
}
