
package com.teamkang.fauxclock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.teamkang.fauxclock.OCApplication;
import com.teamkang.fauxclock.PhoneManager;
import com.teamkang.fauxclock.cpu.CpuInterface;

public class ScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;

    CpuInterface cpu;

    @Override
    public void onReceive(Context context, Intent intent) {

        cpu = ((OCApplication) context.getApplicationContext()).getCpu();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            cpu.setMaxFreq(cpu.getSettings().getString("cpu_screenoff_max", cpu.getMaxFreqSet()),
                    false);
            cpu.setMinFreq(cpu.getSettings().getString("cpu_screenoff_min", cpu.getMinFreqSet()),
                    false);

            screenOff = true;
            Log.e("ROMAN", "Screen off!");

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
            Log.e("ROMAN", "Screen on!");
            cpu.loadValuesFromSettings();
        }
        // Intent i = new Intent(context, ScreenStateService.class);
        // i.putExtra("screen_state", screenOff);
        // context.startService(i);
    }

}
