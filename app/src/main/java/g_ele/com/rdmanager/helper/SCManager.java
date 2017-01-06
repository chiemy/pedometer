package g_ele.com.rdmanager.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import g_ele.com.rdmanager.listeners.StepChangeListener;

/**
 * Using Sensor to record distance and step data
 * Created by aki on 1/9/2016.
 */

class SCManager implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    StepChangeListener stepsDelegate;

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
            stepsDelegate.onStepChange(event.values.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
