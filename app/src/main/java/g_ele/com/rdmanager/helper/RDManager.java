package g_ele.com.rdmanager.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.amap.api.location.AMapLocation;

import java.util.Timer;
import java.util.TimerTask;

import g_ele.com.rdmanager.listeners.LocationChangeListener;
import g_ele.com.rdmanager.listeners.PedometerListener;
import g_ele.com.rdmanager.listeners.StepChangeListener;

import static g_ele.com.rdmanager.Constants.MODE_INDOOR;
import static g_ele.com.rdmanager.Constants.MSG_DURATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_LOCATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_STEP_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_TODAY_STEP_CHANGE;

/**
 * RDManager
 * Created by aki on 1/9/2016.
 */

class RDManager implements StepChangeListener, LocationChangeListener {
    public Integer duration = 0;
    public Double distance = 0.0;
    public Integer steps = 0;

    public Integer calorie = 0;
    public AMapLocation oldLocation;
    public AMapLocation newLocation;

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

    private Database mDatabase;
    private static boolean WAIT_FOR_VALID_STEPS = false;

    private int mTodaySteps;
    private int mSavedSteps;

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
                        delegate.onDurationChanged(duration);
                        break;
                    case MSG_STEP_CHANGE:
                        delegate.onStepChange(steps);
                        break;
                    case MSG_TODAY_STEP_CHANGE:
                        delegate.onTodayStepChange(mTodaySteps);
                        break;
                    case MSG_LOCATION_CHANGE:
                        delegate.onLocationChanged(oldLocation, newLocation);
                        break;
                    default:
                        delegate.onDurationChanged(duration);
                        delegate.onStepChange(steps);
                        if (mGpsManager != null) {
                            delegate.onLocationChanged(oldLocation, newLocation);
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
        mDatabase = Database.getInstance(mContext);
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
            // initGpsManager();
        }
        if (hasStepDetector()) {
            initSCManager();
        } else {
            initACManager();
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
            mGpsManager.setDelegate(this);
        }
    }

    private void initSCManager() {
        if (mSCManager == null) {
            mSCManager = new SCManager(mContext);
            mSCManager.stepsDelegate = this;
        }
    }

    private void initACManager() {
        if (mACManager == null) {
            mACManager = new ACManager(mContext);
            mACManager.userAge = userAge;
            mACManager.userGender = userGender;
            mACManager.userHeight = userHeight;
            mACManager.userWeight = userWeight;
            mACManager.stepsDelegate = this;
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
        WAIT_FOR_VALID_STEPS = true;
        mTimer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                duration += 1;
                syncUIHandler.obtainMessage(MSG_DURATION_CHANGE).sendToTarget();
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
        saveSteps();
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
        syncUIHandler.obtainMessage().sendToTarget();
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onStepChange(int steps) {
        this.steps += steps;
        syncUIHandler.obtainMessage(MSG_STEP_CHANGE).sendToTarget();
        if (WAIT_FOR_VALID_STEPS) {
            WAIT_FOR_VALID_STEPS = false;
            saveSteps();
        }
    }

    @Override
    public void onTodayStepChange(int steps) {
    }

    @Override
    public void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation) {
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        syncUIHandler.obtainMessage(MSG_LOCATION_CHANGE).sendToTarget();
    }

    private void saveSteps() {
        if (steps > 0) {
            int stepsChanged = steps - mSavedSteps;
            mSavedSteps = steps;
            int todaySavedSteps = mDatabase.getSteps(Utils.getToday());
            if (todaySavedSteps == Integer.MIN_VALUE) {
                mDatabase.insertNewDay(Utils.getToday(), steps);
            } else {
                mDatabase.addToLastEntry(stepsChanged);
            }
            mTodaySteps = todaySavedSteps + stepsChanged;
            syncUIHandler.obtainMessage(MSG_TODAY_STEP_CHANGE).sendToTarget();
        }
    }
}
