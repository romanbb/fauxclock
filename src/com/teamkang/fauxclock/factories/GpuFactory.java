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

import com.teamkang.fauxclock.ExpandingPreference;
import com.teamkang.fauxclock.GpuController;
import com.teamkang.fauxclock.PhoneManager;
import com.teamkang.fauxclock.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class GpuFactory implements SeekBar.OnSeekBarChangeListener {

    Context mContext;

    View me;

    public RelativeLayout gpuLayout;
    public Spinner cpuGovSpinner;
    public Spinner gpuGovSpinner;
    public SeekBar gpuIOFracSeek;
    public TextView gpuIOFracValue;

    TextView tabTitle;
    TextView tabLeft;
    TextView tabRight;

    GpuController gpu;

    public GpuFactory(Context c, GpuController g) {
        mContext = c;
        gpu = g;
    }

    public void createGpuView() {

        LayoutInflater inf = LayoutInflater.from(mContext);

        me = inf.inflate(R.layout.gpu_control,
                null);

        /* gpu */
        gpuLayout = (RelativeLayout) me.findViewById(R.id.gpuControl);
        gpuGovSpinner = (Spinner) me.findViewById(R.id.gpu_gov_spinner);
        gpuIOFracSeek = (SeekBar) me.findViewById(R.id.seekbar_gpu_io_frac);
        gpuIOFracValue = (TextView) me.findViewById(R.id.gpu_io_frac_value);

        if (PhoneManager.supportsGpu()) {
            gpu = new GpuController(mContext);

            ArrayAdapter<String> gpuGovSpinnerAdapter = new ArrayAdapter<String>(
                    mContext,
                    android.R.layout.simple_spinner_item, gpu.govs);
            gpuGovSpinnerAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            gpuGovSpinner.setAdapter(gpuGovSpinnerAdapter);
            gpuGovSpinner.setSelection(gpuGovSpinnerAdapter.getPosition(gpu
                    .getCurrentActiveGov()));
            gpuGovSpinner
                    .setOnItemSelectedListener(new OnItemSelectedListener() {

                        public void onItemSelected(AdapterView<?> parent,
                                View view, int position, long id) {
                            String selectedGov = (String) parent
                                    .getSelectedItem();

                            gpu.setGpuGoverner(selectedGov);

                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                            // TODO Auto-generated method stub

                        }
                    });

            gpuIOFracSeek.setMax(100);
            gpuIOFracSeek.setProgress(gpu.getGpuIOFraction());
            gpuIOFracSeek.setOnSeekBarChangeListener(this);

            gpuIOFracValue.setText(gpu.getGpuIOFraction() + "");

        } else {
            gpuLayout.setVisibility(View.GONE);
            gpuGovSpinner.setVisibility(View.GONE);
            gpuIOFracSeek.setVisibility(View.GONE);
            gpuIOFracValue.setVisibility(View.GONE);
        }

        tabTitle = (TextView) me.findViewById(R.id.tab_title);
        tabTitle.setText("GPU Control");

    }

    public View getView() {
        if (me == null)
            createGpuView();

        return me;
    }


    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {

        switch (seekBar.getId()) {
            case R.id.seekbar_gpu_io_frac:
                if (seekBar != null) {

                    int max = 80;
                    int min = 20;

                    if (progress >= min && progress <= max) {
                        gpuIOFracValue.setText(progress + "");
                        seekBar.setProgress(progress);
                    } else if (progress > 80) {
                        gpuIOFracValue.setText(80 + "");
                        seekBar.setProgress(80);
                    } else {
                        gpuIOFracValue.setText(20 + "");
                        seekBar.setProgress(20);
                    }
                }
                break;
        }

    }

    public void onStartTrackingTouch1(SeekBar seekBar) {
        // seekBar.
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekbar_gpu_io_frac:
                gpu.setGpuIOFraction(seekBar.getProgress());
                break;
        }
    }
}
