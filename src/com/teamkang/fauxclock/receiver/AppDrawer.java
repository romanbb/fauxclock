
package com.teamkang.fauxclock.receiver;

import com.teamkang.fauxclock.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class AppDrawer extends Activity {

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);

        startActivity(new Intent(getApplicationContext(), Main.class));
        this.finish();
    }
}
