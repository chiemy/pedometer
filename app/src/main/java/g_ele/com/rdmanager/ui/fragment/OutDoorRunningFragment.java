package g_ele.com.rdmanager.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;

import g_ele.com.rdmanager.Constants;
import g_ele.com.rdmanager.Pedometer;
import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.SportAnalyser;
import g_ele.com.rdmanager.listeners.AnalyserDataListener;

import static g_ele.com.rdmanager.R.id.location;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public class OutDoorRunningFragment extends Fragment {

    private TextView mDurationText;
    private TextView mDistanceText;
    private TextView mStepsText;
    private TextView mPaceText;
    private TextView mCalorieText;
    private TextView mLocationText;

    private Pedometer mPedometer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int defaultMode = Constants.MODE_OUTDOOR;
        initPedometer(defaultMode);
    }

    private void initPedometer(int defaultMode) {
        Pedometer.Config config = new Pedometer.Config.Builder()
                .setMode(defaultMode)
                .setCountStepInBackgroundEnable(true)
                .build();

        mPedometer = Pedometer.getInstance(getActivity(), config);
        mPedometer.addDataChangeListener(mDataListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_outdoor_running, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        mPedometer.start();
    }

    private void initView(View view) {
        mDurationText = (TextView) view.findViewById(R.id.duration);
        mDistanceText = (TextView) view.findViewById(R.id.distance);
        mStepsText = (TextView) view.findViewById(R.id.steps);
        mPaceText = (TextView) view.findViewById(R.id.pace);
        mCalorieText = (TextView) view.findViewById(R.id.calorie);
        mLocationText = (TextView) view.findViewById(location);
    }


    private AnalyserDataListener mDataListener = new AnalyserDataListener(new SportAnalyser.Builder().build()) {
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
            mLocationText.setText("latitude:" + String.valueOf(newLocation.getLatitude()) + " longtitude:" + String.valueOf(newLocation.getLongitude()));
        }

        @Override
        public void onCalorieChange(int calorie) {
            mCalorieText.setText(String.valueOf(calorie));
        }

        @Override
        public void onDistanceChange(double distance) {
            mDistanceText.setText(String.valueOf(distance));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPedometer.release();
    }
}
