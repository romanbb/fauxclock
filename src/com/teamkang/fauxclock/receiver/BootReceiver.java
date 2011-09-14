package com.teamkang.fauxclock.receiver;

import com.teamkang.fauxclock.CpuController;
import com.teamkang.fauxclock.GpuController;

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
			cpu.loadValuesFromSettings();

			GpuController gpu = new GpuController(context);
			gpu.loadValuesFromSettings();
		}

	}
}
