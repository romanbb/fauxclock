
package com.teamkang.fauxclock;

import ru.org.amip.MarketAccess.utils.ShellInterfaceO;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.receiver.ScreenReceiver;

public class OCApplication extends Application {

    private CpuInterface cpu;
    private GpuController gpu;
    private BroadcastReceiver mReceiver;
    private ShellInterfaceO sh;

    private boolean PermissionsChecked = false;

    private String TAG = "OCApplication";

    public void onCreate() {
        super.onCreate();
        sh = new ShellInterfaceO();
        mReceiver = new ScreenReceiver();
    }

    public String getProcessOutput(String c) {
        return sh.getProcessOutput(c);
    }

    public void runCommand(String c) {
        // Log.e(TAG, "Running command: " + c);
        Intent si = new Intent(getApplicationContext(), ShellService.class);
        si.putExtra("command", c);
        // Log.i(TAG, "Running: " + c);
        getApplicationContext().startService(si);

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

    public boolean arePermissionsChecked() {
        return PermissionsChecked;
    }

    public void setPermissionsChecked(boolean yesno) {
        PermissionsChecked = yesno;
    }

    public BroadcastReceiver getScreenReceiver() {
        return mReceiver;
    }

    public void setPhoneManagerStuff() {
        cpu = PhoneManager.getCpu(getApplicationContext());
        gpu = PhoneManager.getGpu(getApplicationContext());
    }

    public CpuInterface getCpu() {
        return cpu;
    }

    public GpuController getGpu() {
        return gpu;
    }

}
