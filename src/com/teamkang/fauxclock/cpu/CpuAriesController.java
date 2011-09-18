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

import ru.org.amip.MarketAccess.utils.ShellInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    // private int globalVoltageDelta = 0;
    private int voltageInterval = 25000;

    public CpuAriesController(Context c) {
        mContext = c;

        readGovernersFromSystem();
        readFrequenciesFromSystem();

        settings = mContext.getSharedPreferences("cpu", 0);
        editor = settings.edit();
    }

    private void readFrequenciesFromSystem() {
        freqs = new ArrayList<String>();
        String gString;

        if (ShellInterface.isSuAvailable()) {
            gString = ShellInterface.getProcessOutput("cat "
                    + FREQ_AVAILABLE_PATH);

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

    public int getNumberOfCores() {
        return 1;
    }

    public void loadValuesFromSettings() {
        try {
            setGoverner(settings.getString("cpu_gov", getCurrentGoverner()));

            setMinFreq(settings.getString("cpu_min_freq", getMinFreqSet()));
            setMaxFreq(settings.getString("cpu_max_freq", getMaxFreqSet()));

            setGlobalVoltageDelta(Integer.parseInt(settings.getString(
                    "voltage_delta", "0")));

            Log.e(TAG, "Got " + settings.getString("voltage_delta", "0")
                    + " from settings for voltage");

        } catch (ClassCastException e) {
        }

    }

    public SharedPreferences getSettings() {
        return settings;
    }

    public Editor getEditor() {
        return editor;
    }

    public void readGovernersFromSystem() {
        String gString;
        govs = new ArrayList<String>();

        if (ShellInterface.isSuAvailable()) {
            gString = ShellInterface.getProcessOutput("cat "
                    + GOV_AVAIALBLE_PATH);

            StringTokenizer st = new StringTokenizer(gString);

            while (st.hasMoreTokens()) {
                govs.add(st.nextToken());
            }
        }

    }

    public boolean setGoverner(String newGov) {
        if (!isValidGov(newGov))
            return false;

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newGov + "\" > "
                    + GOV_CURRENT_PATH);
        }

        return false;
    }

    public String getCurrentGoverner() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat " + GOV_CURRENT_PATH);
        }

        return null;
    }

    public String[] getAvailableGoverners() {
        String[] arr = new String[govs.size()];

        for (int i = 0; i < govs.size(); i++) {
            arr[i] = govs.get(i);
        }
        return arr;
    }

    /**
     * millivolts: -50,000 50,000 -25,000 25,000
     */
    public boolean setGlobalVoltageDelta(int newDelta) {
        if (DBG) {
            Log.e(TAG, "new global voltage delta: " + newDelta);
            // return true;
        }

        int realMilliVolts = newDelta;

        // supports only undervolting, can't overvolt
        // must be positive numbers
        if (newDelta < 0) {
            newDelta *= -1;
        }

        newDelta /= 1000;

        String write = "";

        for (String freq : freqs) {
            editor.putString(freq, newDelta + "").apply();
            write += newDelta + " ";
        }

        write = write.trim();

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + write + "\" > "
                    + UV_TABLE_PATH);
            editor.putString("voltage_delta", realMilliVolts + "").apply();
            Log.e(TAG, "put 'voltage_delta' key in settings as: "
                    + realMilliVolts);
            // globalVoltageDelta = realMilliVolts;
            return true;
        }

        Toast.makeText(mContext,
                "Couldn't set global voltage for some reason!",
                Toast.LENGTH_SHORT);
        return false;
    }

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
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat " + FREQ_MAX_PATH);
        }
        return null;
    }

    public String getLowestFreqAvailable() {
        int min = Integer.MAX_VALUE;

        for (String freq : freqs) {
            int f = Integer.parseInt(freq);

            if (f < min)
                min = f;
        }
        return min + "";
    }

    public String getMinFreqSet() {
        if (ShellInterface.isSuAvailable()) {
            return ShellInterface.getProcessOutput("cat " + FREQ_MIN_PATH);

        }

        return null;
    }

    public String getCurrentFrequency() {
        String speed = "";

        try {
            BufferedReader bf = new BufferedReader(new FileReader(FREQ_CURRENT_PATH));
            speed = bf.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        speed.trim();

        // Log.e(TAG, "called get cur freq, got: " + speed);
        return speed;
    }

    public String[] getAvailableFrequencies() {
        String[] arr = new String[freqs.size()];

        for (int i = 0; i < freqs.size(); i++) {
            arr[i] = freqs.get(i);
        }

        return arr;
    }

    public int getVoltageInterval() {
        return voltageInterval;
    }

    public boolean setMaxFreq(String newFreq) {
        if (DBG) {
            Log.e(TAG, "setMaxFreq(" + newFreq + ")");
        }

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                    + FREQ_MAX_PATH);
            editor.putString("cpu_max_freq", newFreq).apply();
            return true;
        }
        return false;

    }

    public boolean setMinFreq(String newFreq) {
        Log.e(TAG, "setMinFreq(" + newFreq + ")");

        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFreq + "\" > "
                    + FREQ_MIN_PATH);
            editor.putString("cpu_min_freq", newFreq).apply();
            return true;
        }
        return false;
    }

    public boolean supportsVoltageControl() {
        // if (true)
        // return false;

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

    public int getGlobalVoltageDelta() {
        return Integer.parseInt(settings.getString("voltage_delta", "0"));
    }

    public String[] getCurrentFrequencies() {
        String[] f = new String[1];

        f[0] = getCurrentFrequency();

        return f;
    }

}
