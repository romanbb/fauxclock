
package com.teamkang.fauxclock;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ru.org.amip.MarketAccess.utils.ShellInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class CpuController {

    // cpu
    private static String cpuTablePath = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";

    private static String CPU0_MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static String CPU0_MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static String CPU0_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";

    private static String CPU1_MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq";
    private static String CPU1_MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq";
    private static String CPU1_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";

    private static String CPU_GOVS_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    private static String CPU_CURRENT_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";

    // private static HashMap<String, String> cpu_table;
    private ArrayList<String> freqs;
    private ArrayList<String> govs;

    protected Context mContext;

    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    public int globalVoltageDelta = 0;
    public int currentVoltageDelta = 0;
    public int voltageInterval = 12500;

    public static final String TAG = "CpuController";

    public CpuController(Context c) {
        mContext = c;

        freqs = new ArrayList<String>();
        govs = new ArrayList<String>();

        settings = mContext.getSharedPreferences("cpu_table", 0);
        editor = settings.edit();
    }

    public void readVddCpuTable() {
        String vdd_table = "";

        // read table into string
        if (ShellInterface.isSuAvailable()) {
            vdd_table = ShellInterface.getProcessOutput("cat " + cpuTablePath);
        }
        StringTokenizer st = new StringTokenizer(vdd_table);

        // break up string, read values, set keys, voltages
        while (st.hasMoreTokens()) {
            // String line = st.nextToken();
            String freq = st.nextToken().trim();
            freq = freq.substring(0, freq.indexOf(":"));

            String voltage = st.nextToken().trim();

            if (freq == null || voltage == null)
                break;

            freqs.add(freq);
            editor.putString(freq, voltage);

            Log.e("FAUXISAKANGER", "Freq: " + freq + ", voltage: " + voltage);
        }

        editor.apply();
    }

    public ArrayList<String> getFreqs() {
        return freqs;
    }

    public ArrayList<String> getGovs() {
        return govs;
    }

    public String getCurrentActiveGov() {
        String g = "";

        if (ShellInterface.isSuAvailable()) {
            g = ShellInterface.getProcessOutput("cat " + CPU_CURRENT_GOV);
        }

        return g;
    }

    public void readGovs() {
        String output = "";

        // read table into string
        if (ShellInterface.isSuAvailable()) {
            output = ShellInterface.getProcessOutput("cat "
                    + CPU_GOVS_LIST_PATH);
        }
        StringTokenizer st = new StringTokenizer(output);

        // break up string, read values, set keys, voltages
        while (st.hasMoreTokens()) {
            // String line = st.nextToken();
            String gov = st.nextToken().trim();

            Log.e(TAG, "Gov: " + gov);
            govs.add(gov);

        }
    }

    public boolean setGov(String newGov) {
        if (!isValidGov(newGov))
            return false;

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.getProcessOutput("echo \"" + newGov + "\" > "
                    + CPU_CURRENT_GOV);
            editor.putString("cpu_gov", newGov).apply();
            return true;
        }

        return false;

    }

    /**
     * sets minimum frequency for both cpus
     * 
     * @return returns false if the frequency isn't valid
     */
    public boolean setMinFreq(String newFreq) {
        boolean a = false;

        a = setMinFreq(0, newFreq);
        a &= setMinFreq(1, newFreq);

        return a;
    }

    public boolean setMinFreq(int whichCpu, String newFreq) {
        if (!isValidFreq(newFreq))
            return false;

        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                            + CPU0_MIN_FREQ_PATH);
                }
                editor.putString("cpu0_min", newFreq).apply();
                return true;
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                            + CPU1_MIN_FREQ_PATH);
                }
                editor.putString("cpu1_min", newFreq).apply();
                return true;
            default:
                return false;
        }

    }

    /**
     * sets maximum frequency for both cpus
     * 
     * @return returns false if the frequency isn't valid
     */
    public boolean setMaxFreq(String newFreq) {
        boolean a = false;

        a = setMaxFreq(0, newFreq);
        a &= setMaxFreq(1, newFreq);

        return a;
    }

    public boolean setMaxFreq(int whichCpu, String newFreq) {
        if (!isValidFreq(newFreq)) {
            Log.e(TAG, "setMaxFreq failed, tried to set : " + newFreq + " on cpu: " + whichCpu);
            return false;
        }

        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                            + CPU0_MAX_FREQ_PATH);
                }
                editor.putString("cpu0_max", newFreq).apply();
                return true;
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                            + CPU1_MAX_FREQ_PATH);
                }
                editor.putString("cpu1_max", newFreq).apply();
                return true;
            default:
                return false;
        }

    }

    /**
     * returns the min frequency of cpu0
     * 
     * @return
     */
    public String getMinFreq() {
        return getMinFreq(0);
    }

    /**
     * returns min cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getMinFreq(int whichCpu) {
        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU0_MIN_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU1_MIN_FREQ_PATH);
                }
            default:
                Log.e(TAG, "getMinFreq() failed with cpu:" + whichCpu);
                return null;

        }
    }

    /**
     * returns the max frequency of cpu0
     * 
     * @return
     */
    public String getMaxFreq() {
        return getMaxFreq(0);
    }

    /**
     * returns max cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getMaxFreq(int whichCpu) {
        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU0_MAX_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU1_MAX_FREQ_PATH);
                }
            default:
                Log.e(TAG, "getMaxFreq() failed with cpu:" + whichCpu);
                return null;

        }
    }

    /**
     * returns the current frequency of cpu0
     * 
     * @return
     */
    public String getCurrentFreq() {
        return getCurrentFreq(0);
    }

    /**
     * returns the current cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getCurrentFreq(int whichCpu) {
        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU0_CUR_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat "
                            + CPU1_CUR_FREQ_PATH);
                }
            default:
                Log.e(TAG, "getCurrentFreq() failed with cpu:" + whichCpu);
                return null;

        }

    }

    /**
     * @param delta in MILLIVOLTS! 25 mV, 50 mV, 75 mV
     */
    public void setGlobalVoltageDelta(int newDeltaFromZero) {
        int diff = Math.abs(newDeltaFromZero - currentVoltageDelta);

        if (newDeltaFromZero - currentVoltageDelta < 0)
            diff *= -1;

        if (diff == 0) {
            return;
        } else {
            applyVoltageDelta(diff);
        }

    }

    /**
     * @param newDelta pass millivolts, 12500, 25000, 50000
     */
    private void applyVoltageDelta(int newDelta) {

        currentVoltageDelta += newDelta;
        // loop through freqs, and decrease local references

        // apply for later
        for (String freq : freqs) {
            int f = Integer.parseInt(freq);
            f += newDelta;

            editor.putString(freq, f + "");
        }
        editor.apply();

        // apply for now.
        if (ShellInterface.isSuAvailable()) {
            String s = Math.abs(newDelta) + "";
            if (newDelta > 0)
                s = "+" + s;
            else
                s = "-" + s;

            // Log.e(TAG, "applying voltage: " + s);
            ShellInterface
                    .runCommand("echo \""
                            + s
                            + "\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
            editor.putString("voltage_delta", currentVoltageDelta + "");
            editor.apply();
        }
    }

    public boolean isValidGov(String gov) {
        return govs.contains((String) gov);
    }

    public boolean isValidFreq(String freq) {
        return freqs.contains((String) freq);

    }

}
