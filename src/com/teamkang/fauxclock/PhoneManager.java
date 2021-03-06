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

import com.teamkang.fauxclock.cpu.CpuAriesController;
import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.cpu.CpuVddController;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;

public class PhoneManager {

    private Context mContext;

    public PhoneManager(Context c) {
        mContext = c;

    }

    public static CpuInterface getCpu(Context c) {
        String board = Build.BOARD;
        // Log.e("FauxClock", "Board: " + board);

        if (board.equals("aries")) {
            return new CpuAriesController(c);
        }
        if (CpuVddController.isSupported()) {
            // Log.e("FauxClock", "Vdd is supported!");
            return new CpuVddController(c);

        } else
            return null;
    }

    public static GpuController getGpu(Context c) {
        return GpuController.isSupported() ? new GpuController(c) : null;
    }

    public CpuInterface getCpu() {
        String board = Build.BOARD;
        // Log.e("FauxClock", "Board: " + board);

        if (board.equals("aries")) {
            return new CpuAriesController(mContext);
        } else if (board.equals("pyramid")) {

            if (CpuVddController.isSupported()) {
                // Log.e("FauxClock", "Vdd is supported!");
                return new CpuVddController(mContext);
            } else {
                // Log.e("FauxClock", "Vdd is NOT supported!");
                return null;
            }

        } else
            return null;

    }

    public GpuController getGpu() {
        return supportsGpu() ? new GpuController(mContext) : null;
    }

    public static boolean isDualCore() {
        return new File("/sys/devices/system/cpu/cpu1/").isDirectory();
    }

    public static boolean supportsGpu() {
        // temporary nasty nasty
        return isDualCore();
    }

}
