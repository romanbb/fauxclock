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

import com.teamkang.fauxclock.CheckRoot;
import com.teamkang.fauxclock.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class AppDrawer extends Activity {

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);

        startActivity(new Intent(getApplicationContext(), CheckRoot.class));
        this.finish();
    }
}
