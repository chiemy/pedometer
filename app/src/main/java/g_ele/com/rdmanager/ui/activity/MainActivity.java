package g_ele.com.rdmanager.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pl.wheelview.WheelView;

import java.util.ArrayList;

import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.Utils;

public class MainActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton typeTimeRb;
    private View layoutDistance;
    private EditText distanceEt;

    private int hour;
    private int minute = 5;
    private long targetTimeInSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutDistance = findViewById(R.id.distance_layout);
        distanceEt = (EditText) findViewById(R.id.et_distance);

        typeTimeRb = (RadioButton) findViewById(R.id.rb_type2);
        typeTimeRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerIDialog();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.rg_type);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                layoutDistance.setVisibility(checkedId == R.id.rb_type3 ? View.VISIBLE : View.GONE);
                if (checkedId != R.id.rb_type2) {
                    typeTimeRb.setText("目标时间");
                }
            }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isGpsOpen(getApplicationContext())) {
                    int checkedId = radioGroup.getCheckedRadioButtonId();
                    switch (checkedId) {
                        case R.id.rb_type1:
                            OutDoorRunningActivity.startForFree(MainActivity.this);
                            break;
                        case R.id.rb_type2:
                            OutDoorRunningActivity.startForTargetTime(MainActivity.this, targetTimeInSecond);
                            break;
                        case R.id.rb_type3:
                            String distance = distanceEt.getText().toString();
                            if (TextUtils.isEmpty(distance)) {
                                return;
                            }
                            float targetDistanceInKm = Float.valueOf(distance);
                            if (targetDistanceInKm <= 0) {
                                Toast.makeText(MainActivity.this, "距离必须大于0", Toast.LENGTH_SHORT).show();
                            }
                            OutDoorRunningActivity.startForTargetDistance(MainActivity.this, targetDistanceInKm);
                            break;
                    }
                } else {
                    openGPS();
                }
            }
        });
    }

    private void showTimePickerIDialog() {
        ArrayList<String> hourData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourData.add(String.valueOf(i));
        }
        ArrayList<String> minuteData = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minuteData.add(String.valueOf(i));
        }
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_time_picker, null);
        final WheelView hourView = (WheelView) view.findViewById(R.id.wheelView_hour);
        hourView.setData(hourData);
        hourView.setDefault(hour);

        final WheelView minuteView = (WheelView) view.findViewById(R.id.wheelView_minute);
        minuteView.setData(minuteData);
        minuteView.setDefault(minute);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.equals(hourView.getSelectedText(), "0")
                                && TextUtils.equals(minuteView.getSelectedText(), "0")) {
                            showTimePickerIDialog();
                            Toast.makeText(MainActivity.this, "时长不能为0", Toast.LENGTH_LONG).show();
                        } else {
                            hour = Integer.valueOf(hourView.getSelectedText());
                            minute = Integer.valueOf(minuteView.getSelectedText());

                            String timeStr = String.format("%d 小时 %d 分", hour, minute);
                            RadioButton rb = (RadioButton) radioGroup.findViewById(R.id.rb_type2);
                            rb.setText("目标时间    " + timeStr);

                            targetTimeInSecond = hour * 60 * 60 + minute * 60;
                        }
                    }
                })
                .setCancelable(false)
                .setView(view)
                .create();
        dialog.show();
    }

    private static final int REQUEST_OPEN_GPS = 0;
    private void openGPS() {
        new AlertDialog.Builder(this)
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_GPS) {
            if (Utils.isGpsOpen(getApplicationContext())) {

            }
        }
    }
}
