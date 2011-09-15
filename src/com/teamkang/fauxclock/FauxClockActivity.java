
package com.teamkang.fauxclock;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class FauxClockActivity extends Activity implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    public static String TAG = "faux";

    public boolean mAreCpuControlsVisible;
    public RelativeLayout cpuLayout;
    public RelativeLayout gpuLayout;
    public ExpandingPreference cpuPref;
    public ExpandingPreference gpuPref;
    public ExpandingPreference voltagePref;
    public SeekBar cpuMaxSeek;
    public SeekBar cpuMinSeek;
    public TextView currentCpuMaxClock;
    public TextView currentCpuMinClock;
    public Spinner cpuGovSpinner;
    public Spinner gpuGovSpinner;
    public SeekBar gpuIOFracSeek;
    public TextView gpuIOFracValue;

    public TextView currentCpu0Clock;
    public TextView currentCpu1Clock;

    public TextView voltageLabel;
    public TextView voltageDelta;
    public SeekBar voltageSeek;
    public RelativeLayout voltageLayout;

    public CheckBox enableOnBootCheckBox;

    CpuInterface cpu;
    GpuController gpu;

    public int[][] cputable;

    public static int VOLTAGE = 1;
    public static int FREQ = 0;

    // need to get phone specific frequencies
    // /sys/devices/system/cpu/cpu0/cpufreq/available_frequencies

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_control_list);

        cpu = new CpuVddController(getApplicationContext());
        // cpu = new CpuAriesController(getApplicationContext());
        cpu.loadValuesFromSettings();

        gpu = new GpuController(getApplicationContext());
        gpu.readGpuSettings();

        // CpuInterface cpu1 = new CpuAriesController(getApplicationContext());

        cputable = buildCpuTable();

        /* cpu */
        cpuLayout = (RelativeLayout) findViewById(R.id.cpuControl);
        cpuLayout.setVisibility(View.GONE);

        cpuPref = (ExpandingPreference) findViewById(R.id.cpu_control_pref);
        cpuPref.setTitle("CPU Control");
        cpuPref.setOnClickListener(this);

        cpuMaxSeek = (SeekBar) findViewById(R.id.cpu_max_seek);
        cpuMaxSeek.setOnSeekBarChangeListener(this);
        cpuMaxSeek.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
        cpuMaxSeek.setProgress(Integer.parseInt(cpu.getMaxFreqSet()));

        cpuMinSeek = (SeekBar) findViewById(R.id.cpu_min_seek);
        cpuMinSeek.setOnSeekBarChangeListener(this);
        cpuMinSeek.setMax(Integer.parseInt(cpu.getHighestFreqAvailable()));
        cpuMinSeek.setProgress(Integer.parseInt(cpu.getMinFreqSet()));

        currentCpuMaxClock = (TextView) findViewById(R.id.cpu_max_clock);
        currentCpuMaxClock.setText(formatMhz(cpu.getMaxFreqSet()));
        currentCpuMinClock = (TextView) findViewById(R.id.cpu_min_clock);
        currentCpuMinClock.setText(formatMhz(cpu.getMinFreqSet()));

        currentCpu0Clock = (TextView) findViewById(R.id.cpu0_freq);
        currentCpu1Clock = (TextView) findViewById(R.id.cpu1_freq);

        findViewById(R.id.refresh).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                refreshClocks();

            }
        });

        if (cpu.supportsVoltageControl()) {

            /* voltage */
            voltageSeek = (SeekBar) findViewById(R.id.global_voltage_seekbar);
            voltageSeek.setMax(200000);
            voltageSeek.setProgress(voltageSeek.getMax() / 2);
            voltageSeek.setOnSeekBarChangeListener(this);

            voltageDelta = (TextView) findViewById(R.id.voltage_delta);

            voltagePref = (ExpandingPreference) findViewById(R.id.voltage_control_pref);
            voltagePref.setTitle("Voltage Prefs");
            voltagePref.setOnClickListener(this);

            voltageLayout = (RelativeLayout) findViewById(R.id.voltage_control);
            voltageLayout.setVisibility(View.GONE);

        } else {
            voltageSeek = (SeekBar) findViewById(R.id.global_voltage_seekbar);
            voltageDelta = (TextView) findViewById(R.id.voltage_delta);
            voltagePref = (ExpandingPreference) findViewById(R.id.voltage_control_pref);
            voltageLayout = (RelativeLayout) findViewById(R.id.voltage_control);

            voltageSeek.setVisibility(View.GONE);
            voltageDelta.setVisibility(View.GONE);
            voltagePref.setVisibility(View.GONE);
            voltageLayout.setVisibility(View.GONE);
        }

        /* governer */
        cpuGovSpinner = (Spinner) findViewById(R.id.cpu_gov_spinner);
        ArrayAdapter<String> govSpinnerAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item,
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

        /* gpu */
        gpuLayout = (RelativeLayout) findViewById(R.id.gpuControl);
        gpuLayout.setVisibility(View.GONE);

        gpuPref = (ExpandingPreference) findViewById(R.id.gpu_control_pref);
        gpuPref.setTitle("GPU Control");
        gpuPref.setOnClickListener(this);
        gpuGovSpinner = (Spinner) findViewById(R.id.gpu_gov_spinner);
        ArrayAdapter<String> gpuGovSpinnerAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item,
                gpu.govs);
        gpuGovSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gpuGovSpinner.setAdapter(gpuGovSpinnerAdapter);
        gpuGovSpinner.setSelection(gpuGovSpinnerAdapter.getPosition(gpu
                .getCurrentActiveGov()));
        gpuGovSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                String selectedGov = (String) parent.getSelectedItem();

                gpu.setGpuGoverner(selectedGov);

            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        gpuIOFracSeek = (SeekBar) findViewById(R.id.seekbar_gpu_io_frac);
        gpuIOFracSeek.setMax(100);
        gpuIOFracSeek.setProgress(33);
        gpuIOFracSeek.setOnSeekBarChangeListener(this);

        gpuIOFracValue = (TextView) findViewById(R.id.gpu_io_frac_value);

        enableOnBootCheckBox = (CheckBox) findViewById(R.id.set_on_boot);
        boolean checked = cpu.getSettings().getBoolean(
                "load_on_startup", false);
        Log.e(TAG, "load on startup is: " + checked);
        enableOnBootCheckBox.setChecked(checked);
        enableOnBootCheckBox.setOnClickListener(this);

        refreshClocks();

        // hide extra labels for now

        // Log.e(TAG, formatMhz(972000 + ""));
    }

    public void refreshClocks() {

        // using dual core settings here, come up with something more clever
        currentCpu0Clock.setText(formatMhz(cpu.getCurrentFrequency()));
        ((CpuVddController) cpu).pingCpu1();
        currentCpu1Clock.setText(formatMhz(((CpuVddController) cpu)
                .getCurrentFrequency(1)));
    }

    public void onClick(View v) {

        boolean visible;

        switch (v.getId()) {
            case R.id.set_on_boot:
                boolean checked = ((CheckBox) v).isChecked();

                cpu.getEditor().putBoolean("load_on_startup", checked).apply();
                Log.e(TAG, "set load on startup to be: " + checked);
                break;
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
            case R.id.voltage_control_pref:
                visible = voltageLayout.getVisibility() == View.VISIBLE;

                if (visible) {
                    voltagePref.setExpanded(false);
                    voltageLayout.setVisibility(View.GONE);
                } else {

                    voltagePref.setExpanded(true);
                    voltageLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

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
            case R.id.global_voltage_seekbar:
                if (seekBar != null && voltageDelta != null) {

                    int zero = seekBar.getMax() / 2;

                    for (int i = seekBar.getMax() * -1; i <= seekBar.getMax(); i += cpu
                            .getVoltageInterval()) {
                        if (progress >= i
                                && progress < i + cpu.getVoltageInterval()) {

                            int diffleft = progress - i;
                            int diffright = (i + cpu.getVoltageInterval())
                                    - progress;

                            if (diffleft < diffright) {
                                int current = i - zero;
                                seekBar.setProgress(i);
                                voltageDelta.setText(formatVolts(current));

                                // cpu.setGlobalVoltageDelta((i - zero));

                                return;
                            } else {
                                int next = (i - zero) + cpu.getVoltageInterval();
                                seekBar.setProgress((i + cpu.getVoltageInterval()));
                                voltageDelta.setText(formatVolts(next));
                                // cpu.setGlobalVoltageDelta((i - zero));

                                return;
                            }

                        }
                    }

                }
                break;
            case R.id.seekbar_gpu_io_frac:
                if (seekBar != null && voltageDelta != null) {

                    int max = 80;
                    int min = 20;

                    if (progress >= min && progress <= max) {
                        gpuIOFracValue.setText(progress + "");
                        seekBar.setProgress(progress);
                    } else if (progress > 80) {
                        gpuIOFracValue.setText(80 + "");
                        seekBar.setProgress(80);
                    } else {
                        gpuIOFracValue.setText(20 + "");
                        seekBar.setProgress(20);
                    }
                }
                break;
        }

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // seekBar.
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.global_voltage_seekbar:
                cpu.setGlobalVoltageDelta(seekBar.getProgress()
                        - (seekBar.getMax() / 2));
                break;
            case R.id.seekbar_gpu_io_frac:
                gpu.setGpuIOFraction(seekBar.getProgress());
                break;
            case R.id.cpu_max_seek:
                cpu.setMaxFreq(seekBar.getProgress() + "");
                break;
            case R.id.cpu_min_seek:
                cpu.setMinFreq(seekBar.getProgress() + "");
                break;
        }
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

    public static String formatVolts(int mV) {
        int s;

        double n = mV / 1000.0;
        DecimalFormat dF = new DecimalFormat("###.###");
        String formatted = dF.format(n);

        if (n > 0)
            formatted = "+" + formatted;

        return formatted + " mV";

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
