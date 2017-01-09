package g_ele.com.rdmanager.helper;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amap.api.location.AMapLocation;

import g_ele.com.rdmanager.Constants;
import g_ele.com.rdmanager.Pedometer;
import g_ele.com.rdmanager.listeners.PedometerListener;

import static g_ele.com.rdmanager.Constants.INIT;

/**
 * Created: chiemy
 * Date: 17/1/3
 * Description:
 */

public class SCService extends Service implements PedometerListener {
    private final static int GRAY_SERVICE_ID = 1001;

    private Messenger mClientMessenger;
    private Pedometer.Config mConfig;

    private final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT:
                    mClientMessenger = msg.replyTo;
                    if (mRDManager != null) {
                        mRDManager.triggerDelegate();
                    }
                    break;
                case Constants.MSG_STOP:
                    mRDManager.stop();
                    break;
                case Constants.MSG_CHANGE_MODE:
                    getConfig(msg);
                    mRDManager.setMode(mConfig.getMode());
                    mRDManager.start();
                    break;
            }
        }

    });

    private void getConfig(Message msg) {
        Bundle config = msg.getData();
        mConfig = Pedometer.Config.create(config);
    }

    private RDManager mRDManager;
    private void initManager() {
        if (mRDManager == null) {
            mRDManager = RDManager.getInstance();
            mRDManager.setContext(this);
            mRDManager.setDelegate(this);
            mRDManager.setMode(mConfig.getMode());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setForeground();

        if (intent != null) {
            mConfig = Pedometer.Config.create(intent.getExtras());
        }

        initManager();
        mRDManager.start();
        return START_STICKY;
    }

    private void setForeground() {
        if (Build.VERSION.SDK_INT < 18) {
            //API < 18 ，此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
    }

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mClientMessenger = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRDManager != null) {
            mRDManager.release();
            mRDManager = null;
        }
    }

    private void sendMessage(Message msg) {
        if (mClientMessenger == null) {
            return;
        }
        try {
            mClientMessenger.send(msg);
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

    private void sendInteger(Integer value, int what) {
        Message msg = getMessage(what);
        msg.arg1 = value;
        sendMessage(msg);
    }

    @Override
    public void onDurationChanged(int duration) {
        sendInteger(duration, Constants.MSG_DURATION_CHANGE);
        // TODO 每隔一段时间保存一次数据
    }

    @Override
    public void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation) {
        Message msg = getMessage(Constants.MSG_LOCATION_CHANGE);
        Bundle bundle = new Bundle(2);
        bundle.putParcelable("old_location", oldLocation);
        bundle.putParcelable("new_location", newLocation);
        msg.setData(bundle);
        sendMessage(msg);
    }

    @Override
    public void onStepChange(int steps) {
        sendInteger(steps, Constants.MSG_STEP_CHANGE);
    }

}
