
package com.teamkang.fauxclock;

import ru.org.amip.MarketAccess.utils.ShellInterface;

import android.util.Log;

import java.util.HashMap;
import java.util.StringTokenizer;

public class GpuController {

    // gpu stuff
    private static String gpuGoverner;
    private static String gpuGovernerPath = "/sys/devices/platform/kgsl/msm_kgsl/kgsl-3d0/scaling_governor";
    private static int gpuIOFraction;
    private static String gpuIOFractionPath = "/sys/devices/platform/kgsl/msm_kgsl/kgsl-3d0/io_fraction";

    public static void readGpuSettings() {
        // read gpu gov
        if (ShellInterface.isSuAvailable()) {
            gpuGoverner = ShellInterface.getProcessOutput("cat " + gpuGovernerPath);
            gpuIOFraction = Integer.parseInt(ShellInterface.getProcessOutput("cat "
                    + gpuIOFractionPath));
        }
    }

    public static void setGpuGoverner(String newGov) {
        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newGov + "\" > " + gpuGovernerPath);
        }
    }

    public static void setGpuIOFraction(int newFrac) {
        if (ShellInterface.isSuAvailable()) {
            ShellInterface.runCommand("echo \"" + newFrac + "\" > " + gpuIOFractionPath);
        }
    }

}
