package com.teamkang.fauxclock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class CheckRoot extends Activity {

    public void onCreate(Bundle ofJoy) {
        super.onCreate(ofJoy);
        this.setContentView(R.layout.aquireroot);

        View v = findViewById(R.id.entry_layout);
        AquireRootTask task = new AquireRootTask(v, CheckRoot.this);
        task.execute();
    }
}
