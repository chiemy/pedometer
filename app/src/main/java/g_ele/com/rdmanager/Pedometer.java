package g_ele.com.rdmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import g_ele.com.rdmanager.helper.SCService;

import static android.content.Context.BIND_AUTO_CREATE;
import static g_ele.com.rdmanager.Constants.MSG_CALORIE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_DISTANCE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_DURATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_LOCATION_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_PACE_CHANGE;
import static g_ele.com.rdmanager.Constants.MSG_STEP_CHANGE;

/**
 * Created: chiemy
 * Date: 17/1/3
 * Description:
 */

public class Pedometer {

    @IntDef({Constants.MODE_INDOOR, Constants.MODE_OUTDOOR})
    public @interface Mode {
    }

    private Handler syncUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (delegate == null) {
                return;
            }
            switch (msg.what) {
                case MSG_DURATION_CHANGE:
                    delegate.durationChanged(msg.arg1);
                    break;
                case MSG_DISTANCE_CHANGE:
                    delegate.distanceChanged(msg.getData().getDouble(Constants.KEY_DATA));
                    break;
                case MSG_STEP_CHANGE:
                    delegate.stepsChanged(msg.arg1);
                    break;
                case MSG_PACE_CHANGE:
                    delegate.paceChanged(msg.getData().getDouble(Constants.KEY_DATA));
                    break;
                case MSG_CALORIE_CHANGE:
                    delegate.calorieChanged(msg.arg1);
                    break;
                case MSG_LOCATION_CHANGE:
                    delegate.coordinateChanged((Location) msg.obj);
                    break;
                default:
            }
        }
    };

    private Messenger mMessenger = new Messenger(syncUIHandler);
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

    private static Pedometer instance = null;
    private Context mContext;

    private PedometerListener delegate;
    private Config mConfig;

    private boolean mIsRunning;

    private Pedometer(Context context, Config config){
        mContext = context.getApplicationContext();
        mConfig = config;
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

    public void setDataChangeListener(PedometerListener listener) {
        delegate = listener;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    private boolean bindService;
    public void start() {
        mIsRunning = true;
        bindService();
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

    /**
     * 改变计步模式
     * @param mode {@link Constants#MODE_INDOOR} or {@link Constants#MODE_OUTDOOR}
     */
    public void changeMode(@Mode int mode) {
        mConfig.mMode = mode;
        Message msg = getMessage(Constants.MSG_CHANGE_MODE);
        msg.setData(mConfig.getData());
        sendMessage(msg);
    }

    public void stop() {
        mIsRunning = false;
        sendMessage(Constants.MSG_STOP);
    }

    public void toggle() {
        mIsRunning = !mIsRunning;
        sendMessage(Constants.MSG_TOGGLE);
    }

    public void release() {
        unbindService();
        instance = null;
        delegate = null;
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
        public static final String MODE = "mode";
        public static final String WORK_IN_BG = "work_in_bg";

        private int mMode;
        private boolean mWorkInBackground;

        private Config(Builder builder) {
            mMode = builder.mMode;
            mWorkInBackground = builder.mWorkInBackground;
        }

        private Config(Bundle bundle) {
            mMode = bundle.getInt(Pedometer.Config.MODE);
            mWorkInBackground = bundle.getBoolean(Pedometer.Config.WORK_IN_BG);
        }

        public int getMode() {
            return mMode;
        }

        public boolean isWorkInBackground() {
            return mWorkInBackground;
        }

        public Bundle getData() {
            Bundle bundle = new Bundle(2);
            bundle.putInt(MODE, mMode);
            bundle.putBoolean(WORK_IN_BG, mWorkInBackground);
            return bundle;
        }

        public static Config create(Bundle data) {
            return new Config(data);
        }

        public static class Builder {
            private int mMode;
            private boolean mWorkInBackground;
            /**
             * 设置计步模式
             * @param mode {@link Constants#MODE_INDOOR} or {@link Constants#MODE_OUTDOOR}
             */
            public Builder setMode(@Mode int mode) {
                mMode = mode;
                return this;
            }

            /**
             * 是否后台运行(开机自启动)
             * @param background
             */
            public Builder setWorkInBackground(boolean background) {
                mWorkInBackground = background;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

}
