/**
 * Copyright 2011 Roman Birg, Paul Reioux, RootzWiki

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.teamkang.fauxclock.receiver;

import com.teamkang.fauxclock.OCApplication;
import com.teamkang.fauxclock.PhoneManager;
import com.teamkang.fauxclock.cpu.CpuInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", "Booted, starting cpu reading!");
        OCApplication app = ((OCApplication) context.getApplicationContext());

        CpuInterface cpu = app.getCpu();
        if (cpu == null)
            return;

        // do some fancy kernel checking
        String newKernel = System.getProperty("os.version");
        String knownKernel = cpu.getSettings().getString("kernel", "");
        Log.d("FauxClock", "previous kernel: " + knownKernel);
        Log.e("FauxClock", "new kernel" + newKernel);

        // check for 'safe' flag, check whether we want to actually use
        // settings, and then check for the same kernel string
        if (cpu.getSettings().getBoolean("load_on_startup", false)
                && cpu.getSettings().getBoolean("safe", false) && knownKernel.equals(newKernel)) {

            // load cpu settings
            cpu.loadValuesFromSettings();

            // logic for screen reciever stuff!
            if (cpu.getSettings().getBoolean("use_screen_off_profile", false)) {
                app.registerScreenReceiver();
            }

            // load gpu settings
            if (PhoneManager.supportsGpu()) {
                app.getGpu().loadValuesFromSettings();
            }
        } else if (!cpu.getSettings().getBoolean("safe", false)) {
            cpu.getEditor().clear().commit();
            // now change the app-known kernel since they're different
            cpu.getEditor().putString("kernel", newKernel).apply();

        }

    }
}
