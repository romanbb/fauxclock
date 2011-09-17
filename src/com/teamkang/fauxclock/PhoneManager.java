
package com.teamkang.fauxclock;

import com.teamkang.fauxclock.cpu.CpuAriesController;
import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.cpu.CpuVddController;

import android.content.Context;
import android.os.Build;

import java.io.File;

public class PhoneManager {

    private Context mContext;

    public PhoneManager(Context c) {
        mContext = c;

    }

    public static CpuInterface getCpu(Context c) {
        String board = Build.BOARD;

        if (board.equals("aries"))
            return new CpuAriesController(c);
        else
            return new CpuVddController(c);
    }

    public static GpuController getGpu(Context c) {
        return supportsGpu() ? new GpuController(c) : null;
    }

    public CpuInterface getCpu() {
        String board = Build.BOARD;

        if (board.equals("aries"))
            return new CpuAriesController(mContext);
        else
            return new CpuVddController(mContext);

        // return null;
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
