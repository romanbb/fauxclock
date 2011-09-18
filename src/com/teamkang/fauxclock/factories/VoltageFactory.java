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

package com.teamkang.fauxclock.factories;

import com.teamkang.fauxclock.R;
import com.teamkang.fauxclock.cpu.CpuInterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class VoltageFactory implements SeekBar.OnSeekBarChangeListener {

    View me;

    public TextView voltageLabel;
    public TextView voltageDelta;
    public SeekBar voltageSeek;
    public RelativeLayout voltageLayout;

    TextView tabTitle;
    TextView tabLeft;
    TextView tabRight;

    CpuInterface cpu;

    Context mContext;

    public VoltageFactory(Context c, CpuInterface ci) {
        mContext = c;
        cpu = ci;
    }

    private void createvoltageView() {

        LayoutInflater inf = LayoutInflater.from(mContext);

        me = inf.inflate(R.layout.voltage_control,
                null);

        voltageSeek = (SeekBar) me.findViewById(R.id.global_voltage_seekbar);
        voltageDelta = (TextView) me.findViewById(R.id.voltage_delta);

        voltageLayout = (RelativeLayout) me.findViewById(R.id.voltage_control);

        if (cpu.supportsVoltageControl()) {

            /* voltage */

            voltageSeek.setMax(200000);
            voltageSeek.setOnSeekBarChangeListener(this);

            int cV = cpu.getGlobalVoltageDelta();
            int zero = voltageSeek.getMax() / 2;
            voltageSeek.setProgress(cV == 0 ? zero : zero + cV);

            voltageDelta.setText(formatVolts(cpu.getGlobalVoltageDelta()));

            voltageLayout = (RelativeLayout) me.findViewById(R.id.voltage_control);

        } else {

            voltageSeek.setVisibility(View.GONE);
            voltageDelta.setVisibility(View.GONE);
            voltageLayout.setVisibility(View.GONE);

        }

        tabTitle = (TextView) me.findViewById(R.id.tab_title);
        tabTitle.setText("Voltage Control");

    }

    public View getView() {
        if (me == null) {
            createvoltageView();
        }

        return me;
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {

        switch (seekBar.getId()) {

            case R.id.global_voltage_seekbar:
                if (seekBar != null && voltageDelta != null) {

                    int zero = seekBar.getMax() / 2;

                    for (int i = seekBar.getMax() * -1; i <= seekBar.getMax(); i += cpu
                            .getVoltageInterval()) {
                        if (progress >= i
                                && progress < i + cpu.getVoltageInterval()) {

                            int diffleft = progress - i;
                            int diffright = (i + cpu.getVoltageInterval())
                                    - progress;

                            if (diffleft < diffright) {
                                int current = i - zero;
                                seekBar.setProgress(i);
                                voltageDelta.setText(formatVolts(current));

                                // cpu.setGlobalVoltageDelta((i - zero));

                                return;
                            } else {
                                int next = (i - zero) + cpu.getVoltageInterval();
                                seekBar.setProgress((i + cpu.getVoltageInterval()));
                                voltageDelta.setText(formatVolts(next));
                                // cpu.setGlobalVoltageDelta((i - zero));

                                return;
                            }

                        }
                    }

                }
                break;

        }

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // seekBar.
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.global_voltage_seekbar:
                cpu.setGlobalVoltageDelta(seekBar.getProgress()
                        - (seekBar.getMax() / 2));
                break;
        }
    }

    public static String formatVolts(int mV) {
        int s;

        double n = mV / 1000.0;
        DecimalFormat dF = new DecimalFormat("###.###");
        String formatted = dF.format(n);

        if (n > 0)
            formatted = "+" + formatted;

        return formatted + " mV";

    }

}
