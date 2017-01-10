package g_ele.com.rdmanager.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;

import g_ele.com.rdmanager.Constants;
import g_ele.com.rdmanager.Pedometer;
import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.SportAnalyser;
import g_ele.com.rdmanager.helper.Utils;
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
        int defaultMode = Constants.MODE_OUTDOOR;
        Pedometer.Config config = new Pedometer.Config.Builder()
                .setMode(defaultMode)
                .setCountStepInBackgroundEnable(true)
                .build();

        rdManager = Pedometer.getInstance(getActivity(), config);
        rdManager.addDataChangeListener(mDataListener);

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
        if (mode == Constants.MODE_OUTDOOR) {
            if (Utils.isGpsOpen(getActivity())) {
                rdManager.changeMode(mode);
            } else {
                openGPS();
            }
        } else {
            rdManager.changeMode(mode);
        }
    }

    private static final int REQUEST_OPEN_GPS = 0;
    private void openGPS() {
        new AlertDialog.Builder(getActivity())
                .setMessage("需要开启GPS")
                .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, REQUEST_OPEN_GPS);
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_GPS) {
            if (Utils.isGpsOpen(getActivity())) {
                changeMode(Constants.MODE_OUTDOOR);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rdManager.release();
    }

}
