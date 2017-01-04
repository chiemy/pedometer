package g_ele.com.rdmanager.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Using accelerometer to record distance and steps
 * Created by aki on 1/9/2016.
 */

class ACPoint {
    private double x;
    private double y;
    private double z;

    ACPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    double absValue() {
        return Math.sqrt(x * x + y * y + z * z);
    }

//    public ACPoint minus(ACPoint p) {
//        return new ACPoint(p.x - this.x, p.y - this.y, p.z - this.z);
//    }
}

class ACManager implements SensorEventListener {
    public Double userHeight = 0.0;
    public Double userWeight = 0.0;
    public Integer userGender = 0; // 0 for female, 1 for male
    public Integer userAge = 0;

    private ArrayList<Number> data;
    private ArrayList<ACPoint> tempData;
    private Integer tempStepCount;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    DataDelegate distanceDelegate;
    DataDelegate stepsDelegate;

    ACManager(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    void start() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        this.data = new ArrayList<>();
        this.tempData = new ArrayList<>();
        this.tempStepCount = 0;
    }

    void stop() {
        mSensorManager.unregisterListener(this);
        this.data = null;
        this.tempData = null;
        this.tempStepCount = 0;
    }

    private double var() {
        double var1 = 0;
        double var2 = 0;
        for (int i = 0; i < this.data.size(); i++) {
            double v = this.data.get(i).doubleValue();
            var1 += v * v;
            var2 += v;
        }
        var1 = var1 / (double)(this.data.size());
        var2 = var2 / (double)(this.data.size());
        var2 = var2 * var2;
        return var1 - var2;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double a = Math.sqrt(x * x + y * y + z * z);
        this.data.add(a);
        ACPoint point = new ACPoint(x, y, z);
        this.tempData.add(point);
        Integer count = this.tempData.size();
        if (count >= 5) {
            ACPoint p1 = this.tempData.get(count - 5);
            ACPoint p2 = this.tempData.get(count - 4);
            ACPoint p3 = this.tempData.get(count - 3);
            ACPoint p4 = this.tempData.get(count - 2);
            ACPoint p5 = this.tempData.get(count - 1);

            if (p3.absValue() > p1.absValue() && p3.absValue() > p2.absValue() && p3.absValue() > p4.absValue() && p3.absValue() > p5.absValue()) {
                this.tempStepCount += 1;
                this.tempData.clear();
                this.tempData.add(p4);
                this.tempData.add(p5);
            } else {
                this.tempData.remove(0);
            }
        }

        if (this.data.size() >= 30) {
            double var = this.var();
            if (var >= 0.02) {
                if (this.distanceDelegate != null) {
                    // TODO 更精确的步幅计算公式
                    this.distanceDelegate.distanceChanged(this.tempStepCount * this.userHeight * 0.47);
                }
                if (this.stepsDelegate != null) {
                    this.stepsDelegate.stepsChanged(this.tempStepCount);
                }
                this.tempStepCount = 0;
            } else {
                this.tempStepCount = 0;
            }
            this.data.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
