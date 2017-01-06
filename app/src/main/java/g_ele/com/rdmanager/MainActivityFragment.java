package g_ele.com.rdmanager;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import g_ele.com.rdmanager.helper.SportAnalyser;
import g_ele.com.rdmanager.listeners.AnalyserDataListener;

import static g_ele.com.rdmanager.R.id.location;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private Pedometer rdManager;

    private TextView mDurationText;
    private TextView mDistanceText;
    private TextView mStepsText;
    private TextView mPaceText;
    private TextView mCalorieText;
    private TextView mLocationText;
    private Button mToggleButton;
    private Button mStopButton;
    private RadioGroup mModeRg;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int defaultMode = Constants.MODE_INDOOR;
        Pedometer.Config config = new Pedometer.Config.Builder()
                .setMode(defaultMode)
                .setCountStepInBackgroundEnable(true)
                .build();

        rdManager = Pedometer.getInstance(getActivity(), config);
        rdManager.addDataChangeListener(mDataListener);
        rdManager.start();

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mDurationText = (TextView) view.findViewById(R.id.duration);
        mDistanceText = (TextView) view.findViewById(R.id.distance);
        mStepsText = (TextView) view.findViewById(R.id.steps);
        mPaceText = (TextView) view.findViewById(R.id.pace);
        mCalorieText = (TextView) view.findViewById(R.id.calorie);
        mLocationText = (TextView) view.findViewById(location);
        mToggleButton = (Button) view.findViewById(R.id.toggleButton);
        mStopButton = (Button) view.findViewById(R.id.stopButton);

        mModeRg = (RadioGroup) view.findViewById(R.id.rg_mode);
        mModeRg.check(mModeRg.findViewWithTag(String.valueOf(defaultMode)).getId());
        mModeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View checkedView = group.findViewById(checkedId);
                changeMode(Integer.valueOf(checkedView.getTag().toString()));
            }
        });

        if (rdManager.isRunning()) {
            mToggleButton.setText("暂停");
            mStopButton.setEnabled(true);
        } else {
            mToggleButton.setText("开始");
            mStopButton.setEnabled(true);
        }

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rdManager.isRunning()) {
                    rdManager.stop();
                    mToggleButton.setText("开始");
                } else {
                    rdManager.start();
                    mToggleButton.setText("暂停");
                    mStopButton.setEnabled(true);
                }
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rdManager.stop();
                mToggleButton.setText("开始");
                mStopButton.setEnabled(false);
            }
        });

        return view;
    }

    private void changeMode(int mode) {
        rdManager.changeMode(mode);
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
        public void onLocationChanged(Location oldLocation, Location newLocation) {
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
        rdManager.release();
    }

}
