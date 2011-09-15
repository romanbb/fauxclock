
package com.teamkang.fauxclock.receiver;

import com.teamkang.fauxclock.CpuAriesController;
import com.teamkang.fauxclock.CpuInterface;
import com.teamkang.fauxclock.CpuVddController;
import com.teamkang.fauxclock.GpuController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", "Booted, starting cpu reading!");
        CpuVddController cpu = new CpuVddController(context);
        // CpuInterface cpu = new CpuAriesController(context);

        if (cpu.getSettings().getBoolean("load_on_startup", false)) {
            cpu.loadValuesFromSettings();

            GpuController gpu = new GpuController(context);
            gpu.loadValuesFromSettings();
        }

    }
}
