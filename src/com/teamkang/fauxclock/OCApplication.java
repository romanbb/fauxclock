
package com.teamkang.fauxclock;

import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.receiver.ScreenReceiver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public class OCApplication extends Application {

    private CpuInterface cpu;
    private GpuController gpu;
    private BroadcastReceiver mReceiver;

    public void onCreate() {
        super.onCreate();

        mReceiver = new ScreenReceiver();

        cpu = PhoneManager.getCpu(getApplicationContext());
        gpu = PhoneManager.getGpu(getApplicationContext());

    }

    public void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    public void unregisterScreenRecever() {
        try {
            getApplicationContext().unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    public BroadcastReceiver getScreenReceiver() {
        return mReceiver;
    }

    public CpuInterface getCpu() {
        return cpu;
    }

    public GpuController getGpu() {
        return gpu;
    }

}
