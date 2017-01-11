package g_ele.com.rdmanager.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import g_ele.com.rdmanager.Constants;
import g_ele.com.rdmanager.DataSimulator;
import g_ele.com.rdmanager.Pedometer;
import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.SportAnalyser;
import g_ele.com.rdmanager.helper.Utils;
import g_ele.com.rdmanager.listeners.AnalyserDataListener;
import g_ele.com.rdmanager.ui.fragment.RouteFragment;

import static g_ele.com.rdmanager.R.id.distance;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public class OutDoorRunningActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String EXTRA_TARGET_TIME = "target_time";
    private static final String EXTRA_TARGET_DISTANCE = "target_distance";

    /**
     * 开始自由训练
     * @param context
     */
    public static void startForFree(Context context) {
        Intent intent = getIntent(context);
        context.startActivity(intent);
    }

    /**
     * 开始目标时间训练
     * @param context
     * @param targetTimeInSecond
     */
    public static void startForTargetTime(Context context, long targetTimeInSecond) {
        if (targetTimeInSecond <= 0) {
            Toast.makeText(context, "目标时间必须大于0", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = getIntent(context);
        intent.putExtra(EXTRA_TARGET_TIME, targetTimeInSecond);
        context.startActivity(intent);
    }

    /**
     * 开始目标距离训练
     * @param context
     * @param targetDistanceInKM
     */
    public static void startForTargetDistance(Context context, float targetDistanceInKM) {
        if (targetDistanceInKM <= 0) {
            Toast.makeText(context, "目标距离必须大于0", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = getIntent(context);
        intent.putExtra(EXTRA_TARGET_DISTANCE, targetDistanceInKM);
        context.startActivity(intent);
    }

    @NonNull
    private static Intent getIntent(Context context) {
        return new Intent(context, OutDoorRunningActivity.class);
    }

    private static final int MIN_TRIGGER_DISTANCE = 1;
    private static final int MAX_SPEED = 13;

    private TextView mDurationText;
    private TextView mDistanceText;
    private TextView mStepsText;
    private TextView mPaceText;
    private TextView mCalorieText;
    private TextView mLocationText;

    private Pedometer mPedometer;

    private View mLayoutPause;
    private View mPauseBtn;

    private List<LatLng> mCurrentRoute;
    private List<List<LatLng>> mRoute;

    private RouteFragment mRouteFragment;
    private boolean mIsMapShowing;

    private long mTargetTimeInSecond;
    private float mTargetDistanceInMeter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_running);
        mTargetTimeInSecond = getIntent().getLongExtra(EXTRA_TARGET_TIME, 0);
        mTargetDistanceInMeter = getIntent().getFloatExtra(EXTRA_TARGET_DISTANCE, 0) * 1000;

        initPedometer();
        initView();
        mRoute = new ArrayList<>(5);
        start();
        DataSimulator.clear();
    }

    private void start() {
        mCurrentRoute = new ArrayList<>();
        mPedometer.start();

        mPauseBtn.setVisibility(View.VISIBLE);
        mLayoutPause.setVisibility(View.GONE);
    }

    private void stop() {
        mRoute.add(mCurrentRoute);
        mPedometer.stop();
        mPauseBtn.setVisibility(View.GONE);
        mLayoutPause.setVisibility(View.VISIBLE);
    }

    private void initPedometer() {
        int defaultMode = Constants.MODE_OUTDOOR;
        Pedometer.Config config = new Pedometer.Config.Builder()
                .setMode(defaultMode)
                .setCountStepInBackgroundEnable(false)
                .build();

        mPedometer = Pedometer.getInstance(this, config);

        mPedometer.addDataChangeListener(mDataListener);
    }

    private void initView() {
        mDurationText = (TextView) findViewById(R.id.duration);
        mDistanceText = (TextView) findViewById(distance);
        mStepsText = (TextView) findViewById(R.id.steps);
        mPaceText = (TextView) findViewById(R.id.pace);
        mCalorieText = (TextView) findViewById(R.id.calorie);
        mLocationText = (TextView) findViewById(R.id.location);
        mPauseBtn = findViewById(R.id.btn_pause);
        mPauseBtn.setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_map).setOnClickListener(this);
        mLayoutPause = findViewById(R.id.pause_layout);

        getSupportFragmentManager().beginTransaction().add(R.id.container, mRouteFragment = new RouteFragment()).commit();
        getSupportFragmentManager().beginTransaction().hide(mRouteFragment).commit();
    }


    private AnalyserDataListener mDataListener = new AnalyserDataListener(new SportAnalyser.Builder()
            .build()) {

        @Override
        public void onDurationChanged(int duration) {
            super.onDurationChanged(duration);
            mDurationText.setText(String.valueOf(duration));
            if (ifAchieveTargetTime(duration)) {
                onAchieveTargetTime();
            }
        }

        @Override
        public void onStepChange(int steps) {
            super.onStepChange(steps);
            mStepsText.setText(String.valueOf(steps));
        }

        @Override
        public void onPaceChanged(float pace) {
            mPaceText.setText(Utils.getFormatPace(pace));
        }

        @Override
        public void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation) {
            // TODO 根据定位精度决定是否绘制
            // 获取定位精度, 单位:米
            // newLocation.getAccuracy();
            // 获取卫星信号强度，仅在gps定位时有效,值为#GPS_ACCURACY_BAD，#GPS_ACCURACY_GOOD，#GPS_ACCURACY_UNKNOWN
            // newLocation.getGpsAccuracyStatus();

            if (oldLocation != null) {
                // 计算距离, 单位m
                float distance = oldLocation.distanceTo(newLocation);
                if (isValidLocation(distance)) {
                    mCurrentRoute.add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                }
            } else if (newLocation != null) {
                mCurrentRoute.add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
            }
            if (newLocation != null) {
                mLocationText.setText("latitude:" + String.valueOf(newLocation.getLatitude()) + " longtitude:" + String.valueOf(newLocation.getLongitude()));
            }
        }

        @Override
        public void onCalorieChange(float calorie) {
            mCalorieText.setText(String.valueOf(calorie));
        }

        @Override
        public void onDistanceChange(float meters) {
            mDistanceText.setText(String.valueOf(meters) + " m");
            if (ifAchieveTargetDistance(meters)) {
                onAchieveTargetDistance();
            }
        }
    };

    private boolean ifAchieveTargetTime(int duration) {
        return mTargetTimeInSecond != 0
                && duration >= mTargetTimeInSecond;
    }

    private void onAchieveTargetTime() {
        new AlertDialog.Builder(this)
                .setMessage("已达成训练时长")
                .setPositiveButton("确定", null)
                .show();
        stop();
    }

    private void onAchieveTargetDistance() {
        new AlertDialog.Builder(this)
                .setMessage("已达成训练距离")
                .setPositiveButton("确定", null)
                .show();
        stop();
    }

    private boolean ifAchieveTargetDistance(float distance) {
        return mTargetDistanceInMeter > 0
                && distance >= mTargetDistanceInMeter;
    }

    private boolean isValidLocation(float distance) {
        return distance >= MIN_TRIGGER_DISTANCE
                && distance <= MAX_SPEED * mPedometer.getLocationTriggerInterval();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                stop();
                break;
            case R.id.btn_resume:
                start();
                break;
            case R.id.btn_stop:
                finish();
                break;
            case R.id.btn_map:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if (mIsMapShowing) {
                    mIsMapShowing = false;
                    transaction.hide(mRouteFragment);
                } else {
                    // mRouteFragment.clear();
                    mRouteFragment.setRoute(DataSimulator.getSimulateData());
                    mRouteFragment.setRoute(mRoute);

                    mIsMapShowing = true;
                    transaction.show(mRouteFragment);
                }
                transaction.commit();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPedometer.release();
    }

    private Toast toast;
    @Override
    public void onBackPressed() {
        if (mPedometer.isRunning()) {
            if (toast != null && toast.getView().isShown()) {
                return;
            }
            toast = Toast.makeText(this, "请先暂停训练", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        super.onBackPressed();
    }
}
