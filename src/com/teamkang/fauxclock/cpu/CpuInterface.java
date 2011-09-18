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

import android.content.SharedPreferences;

public interface CpuInterface {

    int getNumberOfCores();

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
