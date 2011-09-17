
package com.teamkang.fauxclock.factories;

import com.teamkang.fauxclock.ExpandingPreference;
import com.teamkang.fauxclock.R;
import com.teamkang.fauxclock.cpu.CpuInterface;

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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;

public class CpuFactory implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    Context mContext;

    public static final String TAG = "CpuFactory";

    public static int VOLTAGE = 1;
    public static int FREQ = 0;

    public int[][] cputable;

    Handler mHandler = new Handler();

    LinearLayout cpuLayout;
    ExpandingPreference cpuPref;
    SeekBar cpuMaxSeek;
    SeekBar cpuMinSeek;
    TextView currentCpuMaxClock;
    TextView currentCpuMinClock;
    Spinner cpuGovSpinner;
    TextView currentCpu0Clock;
    TextView currentCpu1Clock;

    CheckBox enableOnBootCheckBox;

    View me;

    TextView tabTitle;
    TextView tabLeft;
    TextView tabRight;

    private CpuInterface cpu;

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
        cpuMaxSeek.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
        cpuMaxSeek.setProgress(Integer.parseInt(cpu.getMaxFreqSet()));

        cpuMinSeek = (SeekBar) me.findViewById(R.id.cpu_min_seek);
        cpuMinSeek.setOnSeekBarChangeListener(this);
        cpuMinSeek.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
        cpuMinSeek.setProgress(Integer.parseInt(cpu.getMinFreqSet()));

        currentCpuMaxClock = (TextView) me.findViewById(R.id.cpu_max_clock);
        currentCpuMaxClock.setText(formatMhz(cpu.getMaxFreqSet()));
        currentCpuMinClock = (TextView) me.findViewById(R.id.cpu_min_clock);
        currentCpuMinClock.setText(formatMhz(cpu.getMinFreqSet()));

        currentCpu0Clock = (TextView) me.findViewById(R.id.cpu0_freq);
        currentCpu1Clock = (TextView) me.findViewById(R.id.cpu1_freq);

        /* governer */
        cpuGovSpinner = (Spinner) me.findViewById(R.id.cpu_gov_spinner);
        ArrayAdapter<String> govSpinnerAdapter = new ArrayAdapter<String>(
                mContext, android.R.layout.simple_spinner_item,
                cpu.getAvailableGoverners());
        govSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cpuGovSpinner.setAdapter(govSpinnerAdapter);
        cpuGovSpinner.setSelection(govSpinnerAdapter.getPosition(cpu
                .getCurrentGoverner()));
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
        }

        enableOnBootCheckBox = (CheckBox) me.findViewById(R.id.set_on_boot);
        boolean checked = cpu.getSettings()
                .getBoolean("load_on_startup", false);
        Log.e(TAG, "load on startup is: " + checked);
        enableOnBootCheckBox.setChecked(checked);
        enableOnBootCheckBox.setOnClickListener(this);

        tabTitle = (TextView) me.findViewById(R.id.tab_title);
        tabTitle.setText("CPU Control");

        refreshClocks();

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

                if (cpus[i] == null) {
                    mHandler.removeCallbacks(mUpdateClockTimeTask);
                    Log.e(TAG, "cpus[" + i + "] was null, stopping update task");
                }

                // Log.e(TAG, "for: " + i);
                if (i == 0)
                    currentCpu0Clock.setText(formatMhz(cpus[0]));
                else if (i == 1)
                    currentCpu1Clock.setText(formatMhz(cpus[1]));

            }

            // Log.e(TAG, "for done, posting again!");
            mHandler.postAtTime(this, SystemClock.uptimeMillis() + 1000);
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
            return "Unknown";
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

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {

        switch (seekBar.getId()) {
            case R.id.cpu_max_seek:
                if (seekBar != null && currentCpuMaxClock != null) {
                    currentCpuMaxClock.setText(formatMhz(progress + ""));

                    int closestIndex = 0;
                    closestIndex = findClosestIndex(cputable, progress);
                    seekBar.setProgress(cputable[FREQ][closestIndex]);
                    // cpu.setMaxFreq(cputable[FREQ][closestIndex] + "");

                }
                break;
            case R.id.cpu_min_seek:
                if (seekBar != null && currentCpuMaxClock != null) {
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

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.cpu_max_seek:
                cpu.setMaxFreq(seekBar.getProgress() + "");
                break;
            case R.id.cpu_min_seek:
                cpu.setMinFreq(seekBar.getProgress() + "");
                break;
        }
    }

    @Override
    public void onClick(View v) {

        boolean visible;

        switch (v.getId()) {
            case R.id.set_on_boot:
                boolean checked = ((CheckBox) v).isChecked();

                cpu.getEditor().putBoolean("load_on_startup", checked).apply();
                Log.e(TAG, "set load on startup to be: " + checked);
                break;
        }
    }

}
