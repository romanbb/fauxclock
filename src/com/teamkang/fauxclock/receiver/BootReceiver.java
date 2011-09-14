
package com.teamkang.fauxclock.receiver;

import com.teamkang.fauxclock.CpuController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", "Booted, starting cpu reading!");
        CpuController cpu = new CpuController(context);

        if (cpu.settings.getBoolean("load_on_startup", false)) {

            cpu.readVddCpuTable();
            cpu.readGovs();

            cpu.setGov(cpu.settings.getString("cpu_gov", cpu.getCurrentActiveGov()));
            cpu.setMinFreq(cpu.settings.getString("cpu0_min_freq",
                    cpu.getMinFreq(0)));
            cpu.setMaxFreq(cpu.settings.getString("cpu0_max_freq",
                    cpu.getMaxFreq(0)));

        }

    }
}
