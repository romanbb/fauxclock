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

import com.teamkang.fauxclock.GpuController;
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

        if (cpu.getSettings().getBoolean("load_on_startup", false)
                && cpu.getSettings().getBoolean("safe", false)) {

            // load cpu settings
            cpu.loadValuesFromSettings();

            // logic for screen reciever stuff!
            // Intent i = new Intent(context, ScreenStateService.class);
            // context.startService(i);
            if (cpu.getSettings().getBoolean("use_screen_off_profile", false)) {
                app.registerScreenReceiver();
            }

            // load gpu settings
            if (PhoneManager.supportsGpu()) {
                app.getGpu().loadValuesFromSettings();
            }
        } else if (!cpu.getSettings().getBoolean("safe", false)) {
            cpu.getEditor().clear().commit();
        }

    }
}
