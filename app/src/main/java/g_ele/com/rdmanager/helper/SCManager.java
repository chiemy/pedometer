package g_ele.com.rdmanager.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

/**
 * Using Sensor to record distance and step data
 * Created by aki on 1/9/2016.
 */

class SCManager implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    DataDelegate distanceDelegate;
    DataDelegate stepsDelegate;

    SCManager(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }
    }

    void start() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepsDelegate != null) {
            stepsDelegate.stepsChanged(event.values.length);
        }
        if (distanceDelegate != null) {
            // TODO: use user's weight and height
            distanceDelegate.distanceChanged((double)event.values.length * 0.84);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
