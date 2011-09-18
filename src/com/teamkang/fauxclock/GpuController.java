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
package com.teamkang.fauxclock;

import ru.org.amip.MarketAccess.utils.ShellInterface;

import android.content.Context;
import android.content.SharedPreferences;

public class GpuController {

    // gpu stuff
    private String gpuGovernerPath = "/sys/devices/platform/kgsl/msm_kgsl/kgsl-3d0/scaling_governor";
    private String gpuIOFractionPath = "/sys/devices/platform/kgsl/msm_kgsl/kgsl-3d0/io_fraction";

    public String[] govs = {
            "ondemand", "performance"
    };

    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    Context mContext;

    public GpuController(Context c) {
        mContext = c;

        settings = mContext.getSharedPreferences("gpu", 0);
        editor = settings.edit();
    }

    public void loadValuesFromSettings() {
        // readGpuSettings();

        setGpuGoverner(settings.getString("gpu_gov", getCurrentActiveGov()));
        setGpuIOFraction(Integer.parseInt(settings.getString("gpu_io_fraction",
                "33")));
    }

    // public void readGpuSettings() {
    // // read gpu gov
    // if (ShellInterface.isSuAvailable()) {
    // gpuGoverner = ShellInterface.getProcessOutput("cat "
    // + gpuGovernerPath);
    // gpuIOFraction = ShellInterface.getProcessOutput("cat "
    // + gpuIOFractionPath);
    //
    // }
    // }

    public String getCurrentActiveGov() {
        String g = "";

        if (ShellInterface.isSuAvailable()) {
            g = ShellInterface.getProcessOutput("cat " + gpuGovernerPath);
        }

        return g;
    }

    public void setGpuGoverner(String newGov) {
        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newGov + "\" > "
                    + gpuGovernerPath);
            editor.putString("gpu_gov", newGov);
        }

    }

    public int getGpuIOFraction() {
        return Integer.parseInt(settings.getString("gpu_io_fraction", "33"));
    }

    public void setGpuIOFraction(int newFrac) {
        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFrac + "\" > "
                    + gpuIOFractionPath);
            editor.putString("gpu_io_fraction", newFrac + "").apply();
        }
    }

}
