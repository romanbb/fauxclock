package com.teamkang.fauxclock;

import java.io.File;

import android.content.Context;
import android.os.Build;

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

	public CpuInterface getCpu() {
		String board = Build.BOARD;

		if (board.equals("aries"))
			return new CpuAriesController(mContext);
		else
			return new CpuVddController(mContext);

		// return null;
	}

	public static boolean isDualCore() {
		return new File("/sys/devices/system/cpu/cpu1/").isDirectory();
	}

	public static boolean supportsGpu() {
		return new File(
				"/sys/devices/platform/kgsl/msm_kgsl/kgsl-3d0/scaling_governor")
				.exists();
	}

}
