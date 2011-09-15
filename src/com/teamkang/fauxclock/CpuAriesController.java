
package com.teamkang.fauxclock;

import ru.org.amip.MarketAccess.utils.ShellInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CpuAriesController implements CpuInterface {

    public String TAG = "Aries CPU Controller";

    public static boolean DBG = true;

    private static final String UV_TABLE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    private static final String GOV_AVAIALBLE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    private static final String GOV_CURRENT_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    private static final String FREQ_AVAILABLE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    private static final String FREQ_CURRENT_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private static final String FREQ_MAX_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String FREQ_MIN_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private ArrayList<String> freqs;
    private ArrayList<String> govs;

    protected Context mContext;

    private int globalVoltageDelta = 0;
    private int currentVoltageDelta = 0;
    private int voltageInterval = 25;

    public CpuAriesController(Context c) {
        mContext = c;

        freqs = new ArrayList<String>();
        govs = new ArrayList<String>();
        readGovernersFromSystem();
        readFrequenciesFromSystem();

        settings = mContext.getSharedPreferences("cpu", 0);
        editor = settings.edit();
    }

    private void readFrequenciesFromSystem() {
        String gString;

        if (ShellInterface.isSuAvailable()) {
            gString = ShellInterface.getProcessOutput("cat " + FREQ_AVAILABLE_PATH);

            StringTokenizer st = new StringTokenizer(gString);

            while (st.hasMoreTokens()) {
                String toAdd = st.nextToken();
                freqs.add(toAdd);
                // Log.e(TAG, "Adding freq to table: " + toAdd);
            }

            /*
             * no need to sort, already reading in sorted way // construct array
             * to be sorted Integer[] sortMe = new Integer[freqs.size()]; for
             * (int i = 0; i < freqs.size(); i++) { sortMe[i] =
             * Integer.parseInt(freqs.get(i)); } Arrays.sort(sortMe); // sorts
             * in ascending freqs = new ArrayList<String>(); for (int i =
             * sortMe.length - 1; i <= 0; i--) { freqs.add(sortMe[i] + "");
             * Log.e(TAG, "adding freq: " + sortMe[i]); }
             */
        }
    }

    @Override
    public void loadValuesFromSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    public SharedPreferences getSettings() {
        return settings;
    }

    @Override
    public Editor getEditor() {
        return editor;
    }

    @Override
    public void readGovernersFromSystem() {
        String gString;

        if (ShellInterface.isSuAvailable()) {
            gString = ShellInterface.getProcessOutput("cat " + GOV_AVAIALBLE_PATH);

            StringTokenizer st = new StringTokenizer(gString);

            while (st.hasMoreTokens()) {
                govs.add(st.nextToken());
            }
        }

    }

    @Override
    public boolean setGoverner(String newGov) {
        if (!isValidGov(newGov))
            return false;

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newGov + "\" > " + GOV_CURRENT_PATH);
        }

        return false;
    }

    @Override
    public String getCurrentGoverner() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat " + GOV_CURRENT_PATH);
        }

        return null;
    }

    @Override
    public String[] getAvailableGoverners() {
        String[] arr = new String[govs.size()];

        for (int i = 0; i < govs.size(); i++) {
            arr[i] = govs.get(i);
        }
        return arr;
    }

    @Override
    public boolean setGlobalVoltageDelta(int newDelta) {
        String write = "";

        for (String freq : freqs) {
            editor.putString(freq, newDelta + "").apply();
            write += newDelta + " ";
        }

        write = write.trim();

        if (ShellInterface.isSuAvailable()) {
            // ShellInterface.runCommand("echo \"" + write + "\" > " +
            // UV_TABLE_PATH);
            return true;
        }

        return false;
    }

    @Override
    public boolean setVoltageDeltaForFrequency(int newDelta, String frequency) {
        String write = "";

        for (String freq : freqs) {
            if (freq.equals(frequency)) {

                editor.putString(freq, newDelta + "").apply();
                write += newDelta + " ";
            } else {
                write += settings.getString(frequency, "0") + " ";
            }
        }

        write = write.trim();

        if (ShellInterface.isSuAvailable()) {
            // ShellInterface.runCommand("echo \"" + write + "\" > " +
            // UV_TABLE_PATH);
            return true;
        }

        return false;
    }

    @Override
    public String getHighestFreqAvailable() {
        int max = 0;

        for (String freq : freqs) {
            int f = Integer.parseInt(freq);

            if (f > max)
                max = f;
        }
        return max + "";
    }

    @Override
    public String getMaxFreqSet() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat "
                    + FREQ_MAX_PATH);
        }
        return null;
    }

    @Override
    public String getLowestFreqAvailable() {
        int min = Integer.MAX_VALUE;

        for (String freq : freqs) {
            int f = Integer.parseInt(freq);

            if (f < min)
                min = f;
        }
        return min + "";
    }

    @Override
    public String getMinFreqSet() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat " + FREQ_MIN_PATH);

        }

        return null;
    }

    @Override
    public String getCurrentFrequency() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat "
                    + FREQ_CURRENT_PATH);
        }
        return null;
    }

    @Override
    public String[] getAvailableFrequencies() {
        String[] arr = new String[freqs.size()];

        for (int i = 0; i < freqs.size(); i++) {
            arr[i] = freqs.get(i);
        }

        return arr;
    }

    @Override
    public int getVoltageInterval() {
        return voltageInterval;
    }

    @Override
    public boolean setMaxFreq(String newFreq) {
        if (DBG) {
            Log.e(TAG, "setMaxFreq(" + newFreq + ")");
        }

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                    + FREQ_MAX_PATH);
            editor.putString("cpu_max", newFreq).apply();
            return true;
        }
        return false;

    }

    @Override
    public boolean setMinFreq(String newFreq) {
        Log.e(TAG, "setMinFreq(" + newFreq + ")");

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                    + FREQ_MIN_PATH);
            editor.putString("cpu_min", newFreq).apply();
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsVoltageControl() {
        if (true)
            return false;

        return new File(UV_TABLE_PATH).exists();

    }

    public boolean isValidGov(String gov) {

        if (govs.isEmpty()) {
            Log.e(TAG,
                    "can't execute isValidGov because there are no govs to compare to!");
        }

        for (String g : govs) {
            if (g.equals(gov)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidFreq(String freq) {
        if (freqs.isEmpty()) {
            Log.e(TAG,
                    "can't execute isValidFreq because there are no freqs to compare to!");
        }

        for (String f : freqs) {
            if (f.equals(freq)) {
                return true;
            }
        }
        return false;

    }

}
