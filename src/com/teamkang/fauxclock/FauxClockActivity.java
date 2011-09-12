
package com.teamkang.fauxclock;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FauxClockActivity extends Activity implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    public static String TAG = "faux";

    public boolean mAreCpuControlsVisible;
    public RelativeLayout cpuLayout;
    public LinearLayout gpuLayout;
    public ExpandingPreference cpuPref;
    public ExpandingPreference gpuPref;
    public SeekBar cpuMaxSeek;
    public SeekBar cpuMinSeek;
    public TextView currentCpuMaxClock;
    public TextView currentCpuMinClock;

    public HashMap<Integer, Integer> sensationTable;
    public int[][] cputable = {
            {
                    192000, 310500, 384000, 432000, 486000, 540000, 594000, 648000, 702000, 756000,
                    810000,
                    864000, 918000, 972000, 102600, 1080000, 1134000, 1118800, 1242000, 1296000,
                    1350000,
                    1404000, 1458000, 1512000, 1566000
            },
            {
                    812500, 812500, 812500, 812500, 837500, 580000, 862500, 875000, 900000, 925000,
                    937500, 962500, 962500, 962500, 975000, 987500, 1000000, 1012500, 1025000,
                    1050000, 1075000, 11000000, 1112500, 112500, 1150000
            }
    };

    public static int VOLTAGE = 1;
    public static int FREQ = 0;

    // need to get phone specific frequencies
    // /sys/devices/system/cpu/cpu0/cpufreq/available_frequencies

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_control_list);

        /* cpu */
        cpuLayout = (RelativeLayout) findViewById(R.id.cpuControl);
        cpuLayout.setVisibility(View.GONE);

        cpuPref = (ExpandingPreference) findViewById(R.id.cpu_control_pref);
        cpuPref.setTitle("CPU Control");
        cpuPref.setOnClickListener(this);

        cpuMaxSeek = (SeekBar) findViewById(R.id.cpu_max_seek);
        cpuMaxSeek.setOnSeekBarChangeListener(this);
        cpuMaxSeek.setMax(findMax(cputable, FREQ));
        cpuMaxSeek.setProgress(800);

        cpuMinSeek = (SeekBar) findViewById(R.id.cpu_min_seek);
        cpuMinSeek.setOnSeekBarChangeListener(this);
        cpuMinSeek.setMax(findMax(cputable, FREQ));
        cpuMinSeek.setProgress(200);

        currentCpuMaxClock = (TextView) findViewById(R.id.cpu_max_clock);
        currentCpuMinClock = (TextView) findViewById(R.id.cpu_min_clock);
        // cpuMaxSeek =

        /* gpu */
        gpuLayout = (LinearLayout) findViewById(R.id.gpuControl);
        gpuLayout.setVisibility(View.GONE);

        gpuPref = (ExpandingPreference) findViewById(R.id.gpu_control_pref);
        gpuPref.setTitle("GPU Control");
        gpuPref.setOnClickListener(this);

        // Log.e(TAG, formatMhz(972000 + ""));
    }

    public void onClick(View v) {
        boolean visible;

        switch (v.getId()) {
            case R.id.cpu_control_pref:
                visible = cpuLayout.getVisibility() == View.VISIBLE;

                if (visible) {
                    cpuPref.setExpanded(false);
                    cpuLayout.setVisibility(View.GONE);
                } else {

                    cpuPref.setExpanded(true);
                    cpuLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.gpu_control_pref:
                visible = gpuLayout.getVisibility() == View.VISIBLE;

                if (visible) {
                    gpuPref.setExpanded(false);
                    gpuLayout.setVisibility(View.GONE);
                } else {

                    gpuPref.setExpanded(true);
                    gpuLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public int findClosestIndex(int[][] cpu, int newValue) {
        int nearest = -1;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < cpu[0].length; i++) {
            int d = Math.abs(cpu[FREQ][i] - newValue);

            if (d < bestDistance) {
                // For the moment, this value is the nearest to the desired
                // number...
                bestDistance = d;
                nearest = i;
            }
        }
        return nearest;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (seekBar.getId()) {
            case R.id.cpu_max_seek:
                if (seekBar != null && currentCpuMaxClock != null) {
                    currentCpuMaxClock.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);
                    seekBar.setProgress(cputable[FREQ][closestIndex]);

                }
                break;
            case R.id.cpu_min_seek:
                if (seekBar != null && currentCpuMaxClock != null) {
                    currentCpuMinClock.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);

                    // check to make sure minimum value isn't higher than max

                    if (cpuMaxSeek.getProgress() < cputable[FREQ][closestIndex]) {
                        seekBar.setProgress(cpuMaxSeek.getProgress());
                    } else {

                        seekBar.setProgress(cputable[FREQ][closestIndex]);
                    }
                }
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // seekBar.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public static String formatMhz(String mhz) {
        int s;

        if (mhz.length() == 6) {
            s = Integer.parseInt(mhz) / 1000;
            return s + " mhz";
        } else if (mhz.length() == 7) {
            double ghz = Integer.parseInt(mhz) / 1000000.0;
            DecimalFormat dF = new DecimalFormat("###.00#");
            String formatted = dF.format(ghz);

            return formatted + " ghz";
        } else {
            return "#";
        }

    }

    public static int findMax(int[][] array, int index2) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < array[0].length; i++) {
            if (array[index2][i] > max) {
                max = array[index2][i];
            }
        }

        return max;
    }
}
