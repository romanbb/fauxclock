package com.teamkang.fauxclock;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ru.org.amip.MarketAccess.utils.ShellInterface;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class CpuController {

	// cpu
	private static String cpuTablePath = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
	// private static HashMap<String, String> cpu_table;
	private ArrayList<String> freqs;

	protected Context mContext;

	SharedPreferences settings;
	SharedPreferences.Editor editor;

	public int globalVoltageDelta = 0;

	public CpuController(Context c) {
		mContext = c;

		settings = mContext.getSharedPreferences("cpu_table", 0);
	}

	public void readVddCpuTable() {
		String vdd_table = "";
		SharedPreferences.Editor editor = settings.edit();

		// read table into string
		if (ShellInterface.isSuAvailable()) {
			vdd_table = ShellInterface.getProcessOutput("cat " + cpuTablePath);
		}
		StringTokenizer st = new StringTokenizer(vdd_table, "\r\n");

		// break up string, read values, set keys, voltages
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			String freq = line.substring(0, line.indexOf(":")).trim();
			String voltage = line.substring(line.indexOf(":") + 1).trim();

			// cpu_table.put(freq, voltage);
			freqs.add(freq);
			editor.putString(freq, voltage);

			Log.e("FAUXISAKANGER", "Freq: " + freq + ", voltage: " + voltage);
		}

		editor.apply();
	}

	public void decreaseAllVoltagesBy25() {
		//loop through freqs, and decrease local references
		//for(int)
		
		if (ShellInterface.isSuAvailable()) {
			ShellInterface
					.runCommand("echo \"-25000\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
		}
	}

	public void increaseAllVoltagesBy25() {
		if (ShellInterface.isSuAvailable()) {
			ShellInterface
					.runCommand("echo \"-25000\" > /sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
		}
	}

	public boolean isValidFreq(String freq) {
		return freqs.contains((String) freq);
	}
}
