package g_ele.com.rdmanager.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import g_ele.com.rdmanager.Constants;
import g_ele.com.rdmanager.Pedometer;
import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.SportAnalyser;
import g_ele.com.rdmanager.listeners.AnalyserDataListener;
import g_ele.com.rdmanager.ui.fragment.RouteFragment;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public class OutDoorRunningActivity extends AppCompatActivity implements View.OnClickListener {

    public static void start(Context context) {
        Intent intent = new Intent(context, OutDoorRunningActivity.class);
        context.startActivity(intent);
    }

    private static final int MIN_TRIGGER_DISTANCE = 5;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_running);
        initPedometer();
        initView();
        mRoute = new ArrayList<>(5);
        start();
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
        mDistanceText = (TextView) findViewById(R.id.distance);
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
        }

        @Override
        public void onStepChange(int steps) {
            super.onStepChange(steps);
            mStepsText.setText(String.valueOf(steps));
        }

        @Override
        public void onLocationChanged(AMapLocation oldLocation, AMapLocation newLocation) {
            if (oldLocation != null) {
                // 计算距离, 单位m
                float distance = oldLocation.distanceTo(newLocation);
                if (distance >= MIN_TRIGGER_DISTANCE) {
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
        public void onCalorieChange(int calorie) {
            mCalorieText.setText(String.valueOf(calorie));
        }

        @Override
        public void onDistanceChange(double distance) {
            // mDistanceText.setText(String.valueOf(distance));
        }
    };

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
                    mRouteFragment.setRoute(fakeData());
                    mRouteFragment.setRoute(mRoute);

                    mIsMapShowing = true;
                    transaction.show(mRouteFragment);
                }
                transaction.commit();
                break;
        }
    }

    @NonNull
    private List<List<LatLng>> fakeData() {
        List<List<LatLng>> fakeRoute = new ArrayList<>();
        List<LatLng> latLngs = new ArrayList<>();
        fakeRoute.add(latLngs);

        latLngs.add(new LatLng(39.99475,116.465957));
        latLngs.add(new LatLng(39.99472,116.466262));
        latLngs.add(new LatLng(39.994586,116.466445));
        latLngs.add(new LatLng(39.994415,116.466522));
        latLngs.add(new LatLng(39.99419,116.466522));
        latLngs.add(new LatLng(39.993999,116.466552));
        latLngs.add(new LatLng(39.993934,116.466392));
        latLngs.add(new LatLng(39.993881,116.46627));
        latLngs.add(new LatLng(39.993801,116.466171));
        latLngs.add(new LatLng(39.993755,116.466041));
        latLngs.add(new LatLng(39.993785,116.465827));
        latLngs.add(new LatLng(39.993732,116.465736));
        latLngs.add(new LatLng(39.993679,116.465698));
        latLngs.add(new LatLng(39.993724,116.465614));
        latLngs.add(new LatLng(39.993705,116.465423));
        latLngs.add(new LatLng(39.993679,116.465278));
        return fakeRoute;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPedometer.release();
    }

}
