/**
 * Copyright 2011 Roman Birg, Paul Reioux, RootzWiki

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.teamkang.fauxclock.factories;

import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.teamkang.fauxclock.ExpandingPreference;
import com.teamkang.fauxclock.OCApplication;
import com.teamkang.fauxclock.R;
import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.receiver.ScreenReceiver;

public class CpuFactory implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    Context mContext;

    public static final String TAG = "CpuFactory";

    public static int VOLTAGE = 1;
    public static int FREQ = 0;

    public int[][] cputable;

    Handler mHandler = new Handler();
    BroadcastReceiver mReceiver = new ScreenReceiver();

    LinearLayout cpuLayout;
    ExpandingPreference cpuPref;
    SeekBar cpuMaxSeek;
    SeekBar cpuMinSeek;
    TextView currentCpuMaxClock;
    TextView currentCpuMinClock;
    Spinner cpuGovSpinner;
    TextView currentCpu0Clock;
    TextView currentCpu1Clock;

    RelativeLayout cpuLayoutScreenOff;
    SeekBar cpuMaxSeekScreenOff;
    SeekBar cpuMinSeekScreenOff;
    TextView currentCpuMaxClockScreenOff;
    TextView currentCpuMinClockScreenOff;
    TextView currentCpu0ClockScreenOff;
    TextView currentCpu1ClockScreenOff;

    CheckBox useScreenOffProfile;
    CheckBox enableOnBootCheckBox;
    CheckBox pingCpu1BootCheckBox;

    View me;

    TextView tabTitle;
    TextView tabLeft;
    TextView tabRight;

    private CpuInterface cpu;

    boolean touchingScreen = false;

    public CpuFactory(Context c, CpuInterface ci) {
        mContext = c;
        cpu = ci;
    }

    /**
     * create the CPU view, initialize all the settings, etc
     * 
     * @param inf
     * @return
     */
    private void createCpuView() {

        LayoutInflater inf = LayoutInflater.from(mContext);

        me = inf.inflate(R.layout.cpu_control,
                null);

        if (cpu.getSettings().getBoolean("safe", false)) {
            cpu.loadValuesFromSettings();
        }

        cpu.getEditor().putBoolean("safe", false).apply();

        cputable = buildCpuTable();

        /* cpu */
        cpuLayout = (LinearLayout) me.findViewById(R.id.cpuControl);

        // cpuPref = (ExpandingPreference)
        // me.findViewById(R.id.cpu_control_pref);
        // cpuPref.setTitle("CPU Control");
        // cpuPref.setOnClickListener(this);

        cpuMaxSeek = (SeekBar) me.findViewById(R.id.cpu_max_seek);
        cpuMaxSeek.setOnSeekBarChangeListener(this);

        cpuMinSeek = (SeekBar) me.findViewById(R.id.cpu_min_seek);
        cpuMinSeek.setOnSeekBarChangeListener(this);

        currentCpuMaxClock = (TextView) me.findViewById(R.id.cpu_max_clock);
        currentCpuMinClock = (TextView) me.findViewById(R.id.cpu_min_clock);

        currentCpu0Clock = (TextView) me.findViewById(R.id.cpu0_freq);
        currentCpu1Clock = (TextView) me.findViewById(R.id.cpu1_freq);

        pingCpu1BootCheckBox = (CheckBox) me.findViewById(R.id.ping_cpu_1);
        pingCpu1BootCheckBox.setChecked(pingCpu1());
        pingCpu1BootCheckBox.setVisibility(View.GONE);

        /* governer */
        cpuGovSpinner = (Spinner) me.findViewById(R.id.cpu_gov_spinner);
        ArrayAdapter<String> govSpinnerAdapter = new ArrayAdapter<String>(
                mContext, android.R.layout.simple_spinner_item,
                cpu.getAvailableGoverners());
        govSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cpuGovSpinner.setAdapter(govSpinnerAdapter);
        cpuGovSpinner.setSelection(govSpinnerAdapter.getPosition(cpu.getCurrentGoverner()));
        cpuGovSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                String selectedGov = (String) parent.getSelectedItem();

                cpu.setGoverner(selectedGov);

            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        if (cpu.getNumberOfCores() == 1) {
            currentCpu1Clock.setVisibility(View.GONE);

            me.findViewById(R.id.cpu_divider).setVisibility(View.GONE);
            pingCpu1BootCheckBox.setVisibility(View.GONE);
        }

        enableOnBootCheckBox = (CheckBox) me.findViewById(R.id.set_on_boot);
        boolean checked = cpu.getSettings()
                .getBoolean("load_on_startup", false);
        Log.e(TAG, "load on startup is: " + checked);
        enableOnBootCheckBox.setChecked(checked);
        enableOnBootCheckBox.setOnClickListener(this);

        tabTitle = (TextView) me.findViewById(R.id.tab_title);
        tabTitle.setText("CPU Control");

        /* screen off stuff */
        cpuLayoutScreenOff = (RelativeLayout) me.findViewById(R.id.cpuControl_screen_off);
        currentCpuMaxClockScreenOff = (TextView) me.findViewById(R.id.cpu_max_clock_screen_off);
        cpuMaxSeekScreenOff = (SeekBar) me.findViewById(R.id.cpu_max_seek_screen_off);
        cpuMinSeekScreenOff = (SeekBar) me.findViewById(R.id.cpu_min_seek_screen_off);
        useScreenOffProfile = (CheckBox) me.findViewById(R.id.screen_off_profile);
        currentCpuMinClockScreenOff = (TextView) me.findViewById(R.id.cpu_min_clock_screen_off);

        cpuMaxSeekScreenOff.setOnSeekBarChangeListener(this);
        cpuMinSeekScreenOff.setOnSeekBarChangeListener(this);

//        cpuMaxSeekScreenOff.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
//
//        String progress = cpu.getSettings().getString("cpu_screenoff_max", cpu.getMaxFreqSet());
//        cpuMaxSeekScreenOff.setProgress(Integer.parseInt(progress));
//        currentCpuMaxClockScreenOff.setText(formatMhz(progress));
//
//        cpuMinSeekScreenOff.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
//        progress = cpu.getSettings().getString("cpu_screenoff_min", cpu.getMinFreqSet());
//        Log.e(TAG, "min progress: " + progress);
//        cpuMinSeekScreenOff.setProgress(Integer.parseInt(progress));
//        currentCpuMinClockScreenOff.setText(formatMhz(progress));

        boolean usingScreenOffProfile = cpu.getSettings().getBoolean("use_screen_off_profile", false);
        useScreenOffProfile.setOnClickListener(this);
        useScreenOffProfile.setChecked(usingScreenOffProfile);
        if (usingScreenOffProfile) {
            cpuLayoutScreenOff.setVisibility(View.VISIBLE);
        } else {
            cpuLayoutScreenOff.setVisibility(View.GONE);
        }

        /* finally! */
        refreshClocks();
        refreshSliders();
        refreshScreenOffSliders();
    }

    public void refreshScreenOffSliders() {
        if (touchingScreen)
            return;

        String temp;

        temp = cpu.getHighestFreqAvailable();
        if (temp != null && !temp.equals("")) {
            cpuMaxSeekScreenOff.setMax(Integer.parseInt(temp));
            cpuMinSeekScreenOff.setMax(Integer.parseInt(temp));
        }

        temp = cpu.getSettings().getString("cpu_screenoff_max", cpu.getMaxFreqSet());
        if (temp != null && !temp.equals("")) {
            cpuMaxSeekScreenOff.setProgress(Integer.parseInt(temp));
            currentCpuMaxClockScreenOff.setText(formatMhz(temp));
        }

        temp = cpu.getSettings().getString("cpu_screenoff_min", cpu.getMinFreqSet());
        if (temp != null && !temp.equals("")) {
            cpuMinSeekScreenOff.setProgress(Integer.parseInt(temp));
            currentCpuMinClockScreenOff.setText(formatMhz(temp));
        }
    }

    public void refreshSliders() {
        if (touchingScreen)
            return;

        String temp;

        temp = cpu.getHighestFreqAvailable();
        if (temp != null && !temp.equals("")) {
            cpuMaxSeek.setMax(Integer.parseInt(temp));
            cpuMinSeek.setMax(Integer.parseInt(temp));
        }

        temp = cpu.getMaxFreqSet();
        if (temp != null && !temp.equals("")) {
            cpuMaxSeek.setProgress(Integer.parseInt(temp));
            currentCpuMaxClock.setText(formatMhz(temp));
        }

        temp = cpu.getMinFreqSet();
        if (temp != null && !temp.equals("")) {
            cpuMinSeek.setProgress(Integer.parseInt(temp));
            currentCpuMinClock.setText(formatMhz(temp));
        }

    }

    public View getView() {
        if (me == null) {
            createCpuView();
        }

        return me;
    }

    public void stopClockRefresh() {
        mHandler.removeCallbacks(mUpdateClockTimeTask);
    }

    public void refreshClocks() {

        // using dual core settings here, come up with something more clever
        // currentCpu0Clock.setText(formatMhz(cpu.getCurrentFrequency()));

        mHandler.removeCallbacks(mUpdateClockTimeTask);
        mHandler.postDelayed(mUpdateClockTimeTask, 100);
    }

    private Runnable mUpdateClockTimeTask = new Runnable() {
        public void run() {
            // Log.e(TAG, "Running!");
            String[] cpus = cpu.getCurrentFrequencies();

            // Log.e(TAG, "for!");
            for (int i = 0; i < cpus.length; i++) {

                if (cpus[0] == null) {
                    mHandler.removeCallbacks(mUpdateClockTimeTask);
                    Log.e(TAG, "cpus[" + i + "] was null, stopping update task");
                    return;
                }

                // Log.e(TAG, "for: " + i);
                if (i == 0)
                    currentCpu0Clock.setText(formatMhz(cpus[0]));
                else if (i == 1) {
                    currentCpu1Clock.setText(formatMhz(cpus[1]));
                } 

            }
            mHandler.postDelayed(mUpdateClockTimeTask, 1000);
            refreshSliders();
            refreshScreenOffSliders();
        }
    };

    public int[][] buildCpuTable() {
        int[][] table;

        String[] freqs = cpu.getAvailableFrequencies();

        table = new int[2][freqs.length];

        for (int i = 0; i < freqs.length; i++) {
            String key = freqs[i];
            table[FREQ][i] = Integer.parseInt(key);

            String voltage = cpu.getSettings().getString(key, "0");
            table[VOLTAGE][i] = Integer.parseInt(voltage);
        }

        return table;
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

    public static String formatMhz(String mhz) {
        if (mhz == null) {
            return "offline";
        }

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

    public static String formatVolts(int mV) {
        int s;

        double n = mV / 1000.0;
        DecimalFormat dF = new DecimalFormat("###.###");
        String formatted = dF.format(n);

        if (n > 0)
            formatted = "+" + formatted;

        return formatted + " mV";

    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (seekBar.getId()) {
            case R.id.cpu_max_seek_screen_off:
                if (seekBar != null && currentCpuMaxClockScreenOff != null) {
                    currentCpuMaxClockScreenOff.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);
                    seekBar.setProgress(cputable[FREQ][closestIndex]);
                    // cpu.setMaxFreq(cputable[FREQ][closestIndex] + "");

                }
                break;
            case R.id.cpu_max_seek:
                if (seekBar != null && currentCpuMaxClock != null) {
                    currentCpuMaxClock.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);
                    seekBar.setProgress(cputable[FREQ][closestIndex]);
                    // cpu.setMaxFreq(cputable[FREQ][closestIndex] + "");

                }
                break;
            case R.id.cpu_min_seek_screen_off:
                if (seekBar != null && currentCpuMinClockScreenOff != null) {
                    currentCpuMinClockScreenOff.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);

                    // check to make sure minimum value isn't higher than max

                    if (cpuMaxSeek.getProgress() < cputable[FREQ][closestIndex]) {
                        // set previous
                        seekBar.setProgress(cpuMaxSeek.getProgress());
                    } else {
                        seekBar.setProgress(cputable[FREQ][closestIndex]);
                        // cpu.setMinFreq(cputable[FREQ][closestIndex] + "");
                    }
                }
                break;
            case R.id.cpu_min_seek:
                if (seekBar != null && currentCpuMinClock != null) {
                    currentCpuMinClock.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);

                    // check to make sure minimum value isn't higher than max

                    if (cpuMaxSeek.getProgress() < cputable[FREQ][closestIndex]) {
                        // set previous
                        seekBar.setProgress(cpuMaxSeek.getProgress());
                    } else {
                        seekBar.setProgress(cputable[FREQ][closestIndex]);
                        // cpu.setMinFreq(cputable[FREQ][closestIndex] + "");
                    }
                }
                break;

        }

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        touchingScreen = true;

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.cpu_max_seek:
                cpu.setMaxFreq(seekBar.getProgress() + "");
                break;
            case R.id.cpu_min_seek:
                cpu.setMinFreq(seekBar.getProgress() + "");
                break;
            case R.id.cpu_max_seek_screen_off:
                cpu.getEditor().putString("cpu_screenoff_max", seekBar.getProgress() + "").apply();
                break;
            case R.id.cpu_min_seek_screen_off:
                cpu.getEditor().putString("cpu_screenoff_min", seekBar.getProgress() + "").apply();
                break;
        }
        touchingScreen = false;
    }

    public void onClick(View v) {

        // boolean visible;

        switch (v.getId()) {
            case R.id.set_on_boot:
                boolean checked = ((CheckBox) v).isChecked();

                cpu.getEditor().putBoolean("load_on_startup", checked).apply();
                Log.e(TAG, "set load on startup to be: " + checked);
                break;
            case R.id.screen_off_profile:
                boolean checked1 = ((CheckBox) v).isChecked();

                cpu.getEditor().putBoolean("use_screen_off_profile", checked1).apply();
                if (checked1) {
                    ((OCApplication) mContext.getApplicationContext()).registerScreenReceiver();
                    cpuLayoutScreenOff.setVisibility(View.VISIBLE);

                    cpu.setMaxFreq(cpu.getMaxFreqSet());
                    cpu.setMinFreq(cpu.getMinFreqSet());

                } else {
                    cpuLayoutScreenOff.setVisibility(View.GONE);
                    ((OCApplication) mContext.getApplicationContext()).unregisterScreenRecever();
                }
                break;
            case R.id.ping_cpu_1:
                boolean checked2 = ((CheckBox) v).isChecked();

                cpu.getEditor().putBoolean("ping_cpu_1", checked2).apply();

                break;
        }
    }
    
    public boolean pingCpu1() {
        return cpu.getSettings().getBoolean("ping_cpu_1", true);
    }
}
