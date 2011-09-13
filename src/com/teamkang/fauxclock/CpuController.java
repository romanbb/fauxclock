
package com.teamkang.fauxclock;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ru.org.amip.MarketAccess.utils.ShellInterface;
import android.content.Context;
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

    // private static HashMap<String, String> cpu_table;
    private ArrayList<String> freqs;

    protected Context mContext;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public int globalVoltageDelta = 0;

    public CpuController(Context c) {
        mContext = c;

        freqs = new ArrayList<String>();

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
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > " + CPU0_MIN_FREQ_PATH);
                }
                return true;
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > " + CPU1_MIN_FREQ_PATH);
                }
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
        if (!isValidFreq(newFreq))
            return false;

        switch (whichCpu) {
            case 0:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > " + CPU0_MAX_FREQ_PATH);
                }
                return true;
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("echo \"" + newFreq + "\" > " + CPU1_MAX_FREQ_PATH);
                }
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
                    return ShellInterface.getProcessOutput("cat " + CPU0_MIN_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat " + CPU1_MIN_FREQ_PATH);
                }
            default:
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
                    return ShellInterface.getProcessOutput("cat " + CPU0_MAX_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat " + CPU1_MAX_FREQ_PATH);
                }
            default:
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
                    return ShellInterface.getProcessOutput("cat " + CPU0_CUR_FREQ_PATH);
                }
            case 1:
                if (ShellInterface.isSuAvailable()) {
                    return ShellInterface.getProcessOutput("cat " + CPU1_CUR_FREQ_PATH);
                }
            default:
                return null;

        }

    }

    public void decreaseAllVoltagesBy25() {
        // loop through freqs, and decrease local references

        // apply for later
        for (String freq : freqs) {
            int f = Integer.parseInt(freq);
            f -= 25000;

            editor.putString(freq, f + "");
        }

        // apply for now.
        if (ShellInterface.isSuAvailable()) {
            ShellInterface
                    .runCommand("echo \"-25000\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
        }
    }

    public void increaseAllVoltagesBy25() {
        // loop through freqs, and decrease local references

        // apply for later
        for (String freq : freqs) {
            int f = Integer.parseInt(freq);
            f += 25000;

            editor.putString(freq, f + "");
        }

        // apply for now.
        if (ShellInterface.isSuAvailable()) {
            ShellInterface
                    .runCommand("echo \"+25000\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
        }
    }

    public boolean isValidFreq(String freq) {
        return freqs.contains((String) freq);
    }
}
