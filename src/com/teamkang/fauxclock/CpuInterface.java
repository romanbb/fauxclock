
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

    boolean supportsVoltageControl();

    boolean setGlobalVoltageDelta(int newDelta);

    boolean setVoltageDeltaForFrequency(int newDelta, String frequency);

    int getGlobalVoltageDelta();

    int getVoltageInterval();

    // frequency stuffs
    String getHighestFreqAvailable();

    String getMaxFreqSet();

    String getLowestFreqAvailable();

    String getMinFreqSet();

    String getCurrentFrequency();

    String[] getCurrentFrequencies();

    String[] getAvailableFrequencies();

    boolean setMaxFreq(String newFreq);

    boolean setMinFreq(String newFreq);

}
