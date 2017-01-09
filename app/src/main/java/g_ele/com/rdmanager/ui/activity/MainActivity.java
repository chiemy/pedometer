package g_ele.com.rdmanager.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import g_ele.com.rdmanager.R;
import g_ele.com.rdmanager.helper.Utils;
import g_ele.com.rdmanager.ui.fragment.IndoorFragment;
import g_ele.com.rdmanager.ui.fragment.OutDoorFragment;

public class MainActivity extends AppCompatActivity {
    private String [] title = {"室外", "室内"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment fragment;
                if (position == 0) {
                    fragment = new OutDoorFragment();
                } else {
                    fragment = new IndoorFragment();
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 0) {
                    if (Utils.isGpsOpen(getApplicationContext())) {
                        OutDoorRunningActivity.start(MainActivity.this);
                    } else {
                        openGPS();
                    }
                }
            }
        });
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
