
package com.teamkang.fauxclock;

import android.content.SharedPreferences;

public interface CpuInterface {

    // Settings
    void loadValuesFromSettings();

    SharedPreferences getSettings();

    SharedPreferences.Editor getEditor();

    // governer stuff
    void readGovernersFromSystem();

    boolean setGoverner(String newGov);

    String getCurrentGoverner();

    String[] getAvailableGoverners();

    // voltage stuff
    boolean setGlobalVoltageDelta(int newDelta);

    boolean setVoltageDeltaForFrequency(int newDelta, String frequency);

    // frequency stuffs
    boolean supportsVoltageControl();

    String getHighestFreqAvailable();

    String getMaxFreqSet();

    String getLowestFreqAvailable();

    String getMinFreqSet();

    String getCurrentFrequency();

    String[] getAvailableFrequencies();

    int getVoltageInterval();

    boolean setMaxFreq(String newFreq);

    boolean setMinFreq(String newFreq);

}
