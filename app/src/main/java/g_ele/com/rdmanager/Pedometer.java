package g_ele.com.rdmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.amap.api.location.AMapLocation;

import java.util.ArrayList;
import java.util.List;

import g_ele.com.rdmanager.helper.Database;
import g_ele.com.rdmanager.helper.GPSManager;
import g_ele.com.rdmanager.helper.SCService;
import g_ele.com.rdmanager.helper.Utils;
import g_ele.com.rdmanager.listeners.LocationChangeListener;
import g_ele.com.rdmanager.listeners.PedometerListener;

import static android.content.Context.BIND_AUTO_CREATE;
import static g_ele.com.rdmanager.Constants.MODE_OUTDOOR;
import static g_ele.com.rdmanager.Constants.MSG_DURATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_LOCATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_STEP_CHANGE;

/**
 * Created: chiemy
 * Date: 17/1/3
 * Description:
 */

public class Pedometer implements LocationChangeListener {

    @IntDef({Constants.MODE_INDOOR, Constants.MODE_OUTDOOR})
    public @interface Mode {
    }

    private static Pedometer instance = null;
    private Context mContext;

    private Config mConfig;

    private boolean mIsRunning;

    private List<PedometerListener> mPedometerListeners;

    private GPSManager mGPSManager;

    private Database mDatabase;

    private Pedometer(Context context, Config config){
        mContext = context.getApplicationContext();
        mConfig = config;
        mPedometerListeners = new ArrayList<>(2);
        mDatabase = Database.getInstance(mContext);
    }

    public static Pedometer getInstance(Context context, Config config) {
        if (instance == null) {
            synchronized (Pedometer.class) {
                if (instance == null) {
                    instance = new Pedometer(context, config);
                }
            }
        }
        return instance;
    }

    public void addDataChangeListener(PedometerListener listener) {
        mPedometerListeners.add(listener);
    }

    public void removeDataChangeLisetener(PedometerListener listener) {
        mPedometerListeners.remove(listener);
    }

    public void clearAllDataChangeLisetener() {
        mPedometerListeners.clear();
    }

    private void bindService() {
        Intent intent = new Intent(mContext, SCService.class);
        intent.putExtras(mConfig.getData());
        mContext.startService(intent);
        if (!bindService) {
            mContext.bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
        bindService = true;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int size = mPedometerListeners.size();
            switch (msg.what) {
                case MSG_DURATION_CHANGE:
                    for (int i = 0; i < size; i++) {
                        mPedometerListeners.get(i).onDurationChanged(msg.arg1);
                    }
                    break;
                case MSG_STEP_CHANGE:
                    for (int i = 0; i < size; i++) {
                        mPedometerListeners.get(i).onStepChange(msg.arg1);
                    }
                    break;
                case MSG_LOCATION_CHANGE:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(mContext.getClassLoader());
                    AMapLocation oldLocation = bundle.getParcelable("old_location");
                    AMapLocation newLocation = bundle.getParcelable("new_location");
                    for (int i = 0; i < size; i++) {
                        mPedometerListeners.get(i).onLocationChanged(oldLocation, newLocation);
                    }
                    break;
                default:
            }
        }
    };

    private Messenger mMessenger = new Messenger(mHandler);
    private Messenger mServiceMessenger;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            Message msg = Message.obtain();
            msg.replyTo = mMessenger;
            msg.what = Constants.INIT;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    };

    private void sendMessage(int what) {
        Message msg = getMessage(what);
        sendMessage(msg);
    }

    private void sendMessage(Message msg) {
        if (mServiceMessenger == null) {
            return;
        }
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private Message getMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    private boolean bindService;
    public void start() {
        mIsRunning = true;
        bindService();
        locationIfNecessary();
    }

    public void stop() {
        mIsRunning = false;
        sendMessage(Constants.MSG_STOP);
        stopLocation();
    }

    public void toggle() {
        mIsRunning = !mIsRunning;
        sendMessage(Constants.MSG_TOGGLE);
        locationIfNecessary();
    }

    /**
     * 改变计步模式
     * @param mode {@link Constants#MODE_INDOOR} or {@link Constants#MODE_OUTDOOR}
     */
    public void changeMode(@Mode int mode) {
        mConfig.mMode = mode;
        locationIfNecessary();
    }

    private void locationIfNecessary() {
        if (mConfig.mMode == MODE_OUTDOOR) {
            startLocation();
        } else {
            stopLocation();
        }
    }

    private void stopLocation() {
        if (mGPSManager != null) {
            mGPSManager.stop();
            mGPSManager = null;
        }
    }

    private void startLocation() {
        if (mGPSManager == null) {
            mGPSManager = new GPSManager(mContext);
            mGPSManager.setDelegate(this);
            mGPSManager.setLocationTriggerInterval(mConfig.mInterval);
        }
        mGPSManager.start();
    }

    public long getLocationTiggerInterval() {
        return mConfig.mInterval;
    }

    @Override
    public void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation) {
        int size = mPedometerListeners.size();
        for (int i = 0; i < size; i++) {
            mPedometerListeners.get(i).onLocationChanged(oldLocation, newLocation);
        }
    }

    /**
     * 获取今天步数, 不随 onStepChange 方法而实时增加, 在暂停计步或退出计步后才会增加
     * @return
     */
    private int getTodaySteps() {
        return Math.max(mDatabase.getSteps(Utils.getToday()), 0);
    }

    public void release() {
        unbindService();
        instance = null;
        mPedometerListeners.clear();
        stopLocation();
    }

    private void unbindService() {
        if (bindService) {
            bindService = false;
            mContext.unbindService(mConnection);
            if (!mConfig.isWorkInBackground()) {
                mContext.stopService(new Intent(mContext, SCService.class));
            }
        }
    }

    public static class Config {
        private static final String MODE = "mode";
        private static final String COUNT_STEP_IN_BG = "count_step_in_bg";
        private static final String LOCATION_IN_BG = "location_in_bg";
        private static final String INTERVAL = "interval";

        private int mMode;
        private boolean mWorkInBackground;
        private boolean mLocationInBackground;
        private long mInterval = 2000;

        private Config(Builder builder) {
            mMode = builder.mMode;
            mWorkInBackground = builder.mCountStepInBackground;
            mLocationInBackground = builder.mLocationInBackground;
            mInterval = builder.mInterval;
        }

        private Config(Bundle bundle) {
            mMode = bundle.getInt(MODE);
            mWorkInBackground = bundle.getBoolean(COUNT_STEP_IN_BG);
            mLocationInBackground = bundle.getBoolean(LOCATION_IN_BG);
            mInterval = bundle.getLong(INTERVAL);
        }

        public int getMode() {
            return mMode;
        }

        public boolean isWorkInBackground() {
            return mWorkInBackground;
        }

        public Bundle getData() {
            Bundle bundle = new Bundle(3);
            bundle.putInt(MODE, mMode);
            bundle.putBoolean(COUNT_STEP_IN_BG, mWorkInBackground);
            bundle.putBoolean(LOCATION_IN_BG, mLocationInBackground);
            bundle.putLong(INTERVAL, mInterval);
            return bundle;
        }

        public static Config create(Bundle data) {
            return new Config(data);
        }

        public static class Builder {
            private int mMode;
            private boolean mCountStepInBackground;
            private boolean mLocationInBackground;
            private long mInterval;

            /**
             * 设置计步模式
             * @param mode {@link Constants#MODE_INDOOR} or {@link Constants#MODE_OUTDOOR}
             */
            public Builder setMode(@Mode int mode) {
                mMode = mode;
                return this;
            }

            /**
             * 是否开启后台计步
             * @param enable
             */
            public Builder setCountStepInBackgroundEnable(boolean enable) {
                mCountStepInBackground = enable;
                return this;
            }

            public Builder setLocationTiggerInterval(long interval) {
                mInterval = interval;
                return this;
            }

            /**
             * 是否开启后台定位(待完成……)
             * @return
             */
            private Builder setLocationInBackgroundEnable(boolean enable) {
                mLocationInBackground = enable;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

}
