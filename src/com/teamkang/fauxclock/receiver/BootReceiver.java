package com.teamkang.fauxclock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.teamkang.fauxclock.CpuInterface;
import com.teamkang.fauxclock.GpuController;
import com.teamkang.fauxclock.PhoneManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("BootReceiver", "Booted, starting cpu reading!");
		CpuInterface cpu = PhoneManager.getCpu(context);

		if (cpu.getSettings().getBoolean("load_on_startup", false)
				&& cpu.getSettings().getBoolean("safe", false)) {
			cpu.loadValuesFromSettings();

			if (PhoneManager.supportsGpu()) {
				GpuController gpu = new GpuController(context);
				gpu.loadValuesFromSettings();

			}
		} else if (!cpu.getSettings().getBoolean("safe", false)) {
			cpu.getEditor().clear().commit();
		}

	}
}
