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

package com.teamkang.fauxclock.cpu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ru.org.amip.MarketAccess.utils.ShellInterface;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.teamkang.fauxclock.OCApplication;
import com.teamkang.fauxclock.ShellService;

public class CpuVddController implements CpuInterface {

    // cpu
    private static String cpuTablePath = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";

    private static String CPU0_MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static String CPU0_MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static String CPU0_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";

    private static String CPU1_MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq";
    private static String CPU1_MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq";
    private static String CPU1_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";

    private static String CPU_GOVS_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    private static String CPU0_CURRENT_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    private static String CPU1_CURRENT_GOV = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor";

    // private static HashMap<String, String> cpu_table;
    private ArrayList<String> freqs;
    private ArrayList<String> govs;

    protected Context mContext;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private int globalVoltageDelta = 0;
    private int voltageInterval = 12500;

    public static final String TAG = "CpuVddController";

    OCApplication root;

    public CpuVddController(Context c) {
        mContext = c;
        settings = mContext.getSharedPreferences("cpu_table", 0);
        editor = settings.edit();

        root = ((OCApplication) c.getApplicationContext());
        // fixPermissions();
        // readGovernersFromSystem();
        // readVddCpuTable();

    }

    public void fixPermissions() {
        runCommand("chmod 666 " + CPU_GOVS_LIST_PATH);

        runCommand("chmod 666 " + CPU0_CURRENT_GOV);
        runCommand("chmod 666 " + CPU0_MAX_FREQ_PATH);
        runCommand("chmod 666 " + CPU0_MIN_FREQ_PATH);
        runCommand("chmod 666 " + CPU0_CUR_FREQ_PATH);
        runCommand("chmod 666 " + CPU1_MAX_FREQ_PATH);
        runCommand("chmod 666 " + CPU1_MIN_FREQ_PATH);
        runCommand("chmod 666 " + CPU1_CUR_FREQ_PATH);
        root.setPermissionsChecked(true);
        readGovernersFromSystem();
        readVddCpuTable();
    }

    public static boolean isSupported() {
        Log.e("FauxClock", "is supported vdd was called");
        return new File(cpuTablePath).exists();
    }

    public void runCommand(String c) {
        // Log.e(TAG, "Running command: " + c);
        Intent si = new Intent(mContext, ShellService.class);
        si.putExtra("command", c);
        // Log.i(TAG, "Running: " + c);
        mContext.startService(si);

    }

    public void loadValuesFromSettings() {

        try {
            setGoverner(settings.getString("cpu_gov", getCurrentGoverner()));

            setMinFreq(0, settings.getString("cpu0_min_freq", getMinFreqSet(0)), true);
            setMaxFreq(0, settings.getString("cpu0_max_freq", getMaxFreqSet(0)), true);

            setMinFreq(1, settings.getString("cpu1_min_freq", getMinFreqSet(1)), true);
            setMaxFreq(1, settings.getString("cpu1_max_freq", getMaxFreqSet(1)), true);

            // globalVoltageDelta = Integer.parseInt(settings.getString(
            // "voltage_delta", "0"));
            // setGlobalVoltageDelta(globalVoltageDelta);
        } catch (ClassCastException e) {
        }

    }

    public String readOneLineFile(String fileName) {
        //runCommand("chmod 666 " + fileName);
        String out = null;
        FileInputStream fis;
        try {

            fis = new FileInputStream(fileName);

            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            out = dis.readLine();
            dis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return out;
    }

    public String readFile(String fileName) {
        //runCommand("chmod 666 " + fileName);
        String out = "";
        FileInputStream fis;
        try {

            fis = new FileInputStream(fileName);

            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            do {
                String line = dis.readLine();
                if (line == null)
                    break;
                out += line;
                Log.i(TAG, "adding: " + line);
            } while (true);
            dis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return out;
    }

    public int getNumberOfCores() {
        return 2;
    }

    public void readVddCpuTable() {
        freqs = new ArrayList<String>();

        // read table into string
        // if (ShellInterface.isSuAvailable())
        // vdd_table = ShellInterface.getProcessOutput("cat " + cpuTablePath);
        // Log.e(TAG, "readVddTable: " + vdd_table);

        StringTokenizer st = new StringTokenizer(readFile(cpuTablePath));

        // break up string, read values, set keys, voltages
        while (st.hasMoreElements()) {

            String freq = st.nextToken().trim();
            freq = freq.substring(0, freq.indexOf(":"));

            String voltage = st.nextToken().trim();

            if (freq == null || voltage == null)
                break;

            Log.e(TAG, "Freq: " + freq + ", voltage: " + voltage);
            freqs.add(freq.trim());
            editor.putString(freq, voltage);

        }
        ;

        editor.apply();
    }

    public boolean supportsVoltageControl() {
        return true;
    }

    public String getCurrentGoverner() {
        return readOneLineFile(CPU0_CURRENT_GOV);

    }

    public void pingCpu1() {
        if (settings.getBoolean("ping_cpu_1", true)) {
            runCommand("echo \"1\" > /sys/devices/system/cpu/cpu1/online");
            // Log.e(TAG, "pinged cpu1");
        }
    }

    public String getMaxFreqFromSettings() {
        return settings.getString("cpu0_max_freq", getMaxFreqSet());
    }

    public String getMaxFreqFromSettings(int whichCpu) {

        return settings.getString("cpu" + whichCpu + "_max_freq",
                getMaxFreqSet(whichCpu));

    }

    public String getMinFreqFromSettings() {
        return settings.getString("cpu0_min_freq", getMinFreqSet());
    }

    public String getMinFreqFromSettings(int whichCpu) {

        return settings.getString("cpu" + whichCpu + "_min_frew",
                getMinFreqSet(whichCpu));

    }

    public ArrayList<String> getFreqs() {

        return freqs;
    }

    public ArrayList<String> getGovs() {
        return govs;
    }

    public String[] getAvailableGoverners() {
        String[] arr = new String[govs.size()];

        for (int i = 0; i < govs.size(); i++) {
            arr[i] = govs.get(i);
        }
        return arr;
    }

    public void readGovernersFromSystem() {
        govs = new ArrayList<String>();
        String output = "";

        // read table into string

        // output = ShellInterface.getProcessOutput("cat " +
        // CPU_GOVS_LIST_PATH);
        //
        // StringTokenizer st = new StringTokenizer(output);

        StringTokenizer gov = new StringTokenizer(readOneLineFile(CPU_GOVS_LIST_PATH));

        // break up string, read values, set keys, voltages
        while (gov.hasMoreElements()) {
            String line = gov.nextToken();

            Log.e(TAG, "Gov: " + line);
            govs.add(line);
        }

    }

    public boolean setGoverner(String newGov) {
        if (!isValidGov(newGov))
            return false;

        runCommand("echo \"" + newGov + "\" > "
                + CPU0_CURRENT_GOV);

        if (new File(CPU1_CURRENT_GOV).exists()) {
            // Log.e(TAG, "writing cpu1 gov");
            runCommand("chmod 666 CPU1_CURRENT_GOV");
            runCommand("echo \"" + newGov + "\" > " + CPU1_CURRENT_GOV);
        }
        editor.putString("cpu_gov", newGov).apply();
        return true;

    }

    public boolean setMinFreq(String newFreq) {
        return setMinFreq(newFreq, true);
    }

    /**
     * sets minimum frequency for both cpus
     * 
     * @return returns false if the frequency isn't valid
     */
    public boolean setMinFreq(String newFreq, boolean permanent) {
        boolean a = false;

        a = setMinFreq(0, newFreq, permanent);
        a &= setMinFreq(1, newFreq, permanent);

        return a;
    }

    public boolean setMinFreq(int whichCpu, String newFreq, boolean permanent) {
        if (!isValidFreq(newFreq))
            return false;

        switch (whichCpu) {
            case 0:
                Log.e("FauxClock", "setMinFreq(0): " + newFreq);

                runCommand("echo \"" + newFreq + "\" > "
                        + CPU0_MIN_FREQ_PATH);

                if (permanent)
                    editor.putString("cpu0_min_freq", newFreq).apply();
                return true;
            case 1:
                Log.e("FauxClock", "setMinFreq(1): " + newFreq);
                pingCpu1();

                runCommand("echo \"" + newFreq + "\" > "
                        + CPU1_MIN_FREQ_PATH);

                if (permanent)
                    editor.putString("cpu1_min_freq", newFreq).apply();
                return true;
            default:
                return false;
        }

    }

    public boolean setMaxFreq(String newFreq) {
        return setMaxFreq(newFreq, true);
    }

    /**
     * sets maximum frequency for both cpus
     * 
     * @return returns false if the frequency isn't valid
     */
    public boolean setMaxFreq(String newFreq, boolean permanent) {
        boolean a = false;

        a = setMaxFreq(0, newFreq, permanent);
        a &= setMaxFreq(1, newFreq, permanent);

        return a;
    }

    public boolean setMaxFreq(int whichCpu, String newFreq, boolean permanent) {
        if (!isValidFreq(newFreq)) {
            Log.e(TAG, "setMaxFreq failed, tried to set : " + newFreq
                    + " on cpu: " + whichCpu);
            return false;
        }

        int f = Integer.parseInt(newFreq);
        // if (f < Integer.parseInt(getHighestFreqAvailable())) {
        //
        // runCommand("stop thermald");
        //
        // } else {
        //
        // runCommand("start thermald");
        //
        // }

        switch (whichCpu) {
            case 0:

                runCommand("echo \"" + newFreq + "\" > "
                        + CPU0_MAX_FREQ_PATH);

                if (permanent)
                    editor.putString("cpu0_max_freq", newFreq).apply();
                return true;
            case 1:
                pingCpu1();

                runCommand("echo \"" + newFreq + "\" > "
                        + CPU1_MAX_FREQ_PATH);

                if (permanent)
                    editor.putString("cpu1_max_freq", newFreq).apply();
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
    public String getLowestFreqAvailable() {
        int min = Integer.MAX_VALUE;

        for (String freq : freqs) {
            int f = Integer.parseInt(freq);

            if (f < min)
                min = f;
        }
        return min + "";
    }

    /**
     * returns min cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getMinFreqSet(int whichCpu) {
        String output = "";
        String which;
        int min = Integer.MAX_VALUE;
        switch (whichCpu) {
            case 0:
                which = CPU0_MIN_FREQ_PATH;
                // output = ShellInterface.getProcessOutput("cat "
                // + CPU0_MIN_FREQ_PATH);
                //
                // Log.e("FauxClock", "getMinFreqSet(0): " + output);
                // return output;
                break;
            case 1:
                which = CPU1_MIN_FREQ_PATH;
                // output = ShellInterface.getProcessOutput("cat "
                // + CPU1_MIN_FREQ_PATH);
                // Log.e("FauxClock", "getMinFreqSet(1): " + output);
                // return output;
                break;
            default:
                Log.e(TAG, "getMinFreq() failed with cpu:" + whichCpu);
                return null;
        }
        return readOneLineFile(which);

        // for (int i = 0; i < 4; i++) {
        // int got = 0;
        // try {
        // got = Integer.parseInt(readOneLineFile(which));
        // } catch (Exception e) {
        // continue;
        // }
        //
        // //Log.e("FauxClock", "getMinFreqSet() on try " + i + " got " + got);
        // if (min >= got)
        // min = got;
        // }
        // return min + "";

    }

    /**
     * returns the max frequency of cpu0
     * 
     * @return
     */
    public String getHighestFreqAvailable() {
        int max = 0;

        for (String freq : freqs) {
            int f = Integer.parseInt(freq);

            if (f > max)
                max = f;
        }
        return max + "";
    }

    public String getMaxFreqSet() {
        return getMaxFreqSet(0);
    }

    public String getMinFreqSet() {
        return getMinFreqSet(0);
    }

    /**
     * returns max cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getMaxFreqSet(int whichCpu) {
        String cpu;
        String out = null;
        switch (whichCpu) {
            case 0:
                cpu = CPU0_MAX_FREQ_PATH;
                break;

            // return ShellInterface.getProcessOutput("cat "
            // + CPU0_MAX_FREQ_PATH);

            case 1:
                cpu = CPU1_MAX_FREQ_PATH;
                break;
            // return ShellInterface.getProcessOutput("cat "
            // + CPU1_MAX_FREQ_PATH);

            default:
                Log.e(TAG, "getMaxFreq() failed with cpu:" + whichCpu);
                return null;

        }

        return readOneLineFile(cpu);
    }

    /**
     * returns the current frequency of cpu0
     * 
     * @return
     */
    public String getCurrentFrequency() {
        return getCurrentFrequency(0);
    }

    /**
     * returns the current cpu freq of specified cpu
     * 
     * @param whichCpu should be 0 or 1
     * @return null if invalid param is sent in
     */
    public String getCurrentFrequency(int whichCpu) {
        String cpu;

        switch (whichCpu) {
            case 0:
                cpu = CPU0_CUR_FREQ_PATH;
                // // if (ShellInterface.isSuAvailable()) {
                // runCommand("chmod 644 " + CPU0_CUR_FREQ_PATH);
                // // }
                // BufferedReader bf1 = new BufferedReader(new
                // FileReader(CPU0_CUR_FREQ_PATH));
                // String cpu0 = bf1.readLine();
                // // Log.e(TAG, "getCurFreq for cpu: " + whichCpu + ": " +
                // // cpu0);
                // return cpu0.trim();
                break;
            case 1:
                pingCpu1();
                cpu = CPU1_CUR_FREQ_PATH;
                // // if (ShellInterface.isSuAvailable()) {
                // runCommand("chmod 644 " + CPU1_CUR_FREQ_PATH);
                // // }
                // BufferedReader bf2 = new BufferedReader(new
                // FileReader(CPU1_CUR_FREQ_PATH));
                // String cpu1 = bf2.readLine();
                // // Log.e(TAG, "getCurFreq for cpu: " + whichCpu + ": " +
                // // cpu1);
                // return cpu1.trim();
                break;
            default:
                return null;

        }
        String output = readOneLineFile(cpu);
        // Log.e(TAG, "geCurrentFrequency(" + whichCpu + "): " + output);
        return output;
    }

    /**
     * @param delta in MILLIVOLTS! -25000 125000, etc
     */
    public boolean setGlobalVoltageDelta(int newDeltaFromZero) {
        int diff = Math.abs(newDeltaFromZero - globalVoltageDelta);

        if (newDeltaFromZero - globalVoltageDelta < 0)
            diff *= -1;

        if (diff == 0) {
            return false;
        } else {
            applyVoltageDelta(diff);
            return true;
        }

    }

    /**
     * @param newDelta pass millivolts, 12500, 25000, 50000
     */
    private void applyVoltageDelta(int newDelta) {

        globalVoltageDelta += newDelta;
        // loop through freqs, and decrease local references

        // apply for later
        for (String freq : freqs) {
            int f = Integer.parseInt(freq);
            f += newDelta;

            editor.putString(freq, f + "");
        }
        editor.apply();

        // apply for now.
        String s = Math.abs(newDelta) + "";
        if (newDelta > 0)
            s = "+" + s;
        else
            s = "-" + s;

        // Log.e(TAG, "applying voltage: " + s);
        runCommand("echo \""
                + s
                + "\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
        editor.putString("voltage_delta", globalVoltageDelta + "");
        editor.apply();

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

    public boolean setVoltageDeltaForFrequency(int newDelta, String frequency) {
        // TODO Auto-generated method stub
        return false;
    }

    public SharedPreferences getSettings() {
        return settings;
    }

    public Editor getEditor() {
        return editor;
    }

    public String[] getAvailableFrequencies() {
        ArrayList<String> temp = freqs;
        temp.remove("310500");
        String[] arr = new String[temp.size()];

        for (int i = 0; i < temp.size(); i++) {
            arr[i] = temp.get(i);
        }

        return arr;
    }

    public int getVoltageInterval() {
        return voltageInterval;
    }

    public int getGlobalVoltageDelta() {
        return Integer.parseInt(settings.getString("voltage_delta", "0"));
    }

    public String[] getCurrentFrequencies() {
        String[] f = new String[2];

        f[0] = getCurrentFrequency(0);
        f[1] = getCurrentFrequency(1);

        return f;
    }

}
