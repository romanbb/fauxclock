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

package com.teamkang.fauxclock;

import com.teamkang.fauxclock.cpu.CpuInterface;
import com.teamkang.fauxclock.factories.CpuFactory;
import com.teamkang.fauxclock.factories.GpuFactory;
import com.teamkang.fauxclock.factories.VoltageFactory;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Main extends Activity {

    public static final String TAG = "Pager";

    private ViewPager awesomePager;
    private Context mContext;
    private AwesomePagerAdapter awesomeAdapter;

    private CpuInterface cpu;
    private GpuController gpu;

    private CpuFactory cpuFactory;
    private VoltageFactory voltageFactory;
    private GpuFactory gpuFactory;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awesome);
        mContext = this;

        cpu = PhoneManager.getCpu(getApplicationContext());
        gpu = PhoneManager.getGpu(getApplicationContext());

        if (cpu == null) {
            this.setContentView(R.layout.unspported_kernel);
        } else {

            awesomeAdapter = new AwesomePagerAdapter();
            awesomePager = (ViewPager) findViewById(R.id.awesomepager);
            awesomePager.setAdapter(awesomeAdapter);
        }
    }

    public void onPause() {
        super.onPause();

        if (cpu != null) {
            cpu.getEditor().putBoolean("safe", true).apply();
        }

        if (cpuFactory != null) {
            cpuFactory.stopClockRefresh();
        }
    }

    public void onResume() {
        super.onResume();

        if (cpu != null) {
            cpu.getEditor().putBoolean("safe", false).apply();
        }

        if (cpuFactory != null) {
            cpuFactory.refreshClocks();
        }

    }

    private class AwesomePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {

            int views = 0;

            if (cpu != null)
                views++;

            if (cpu.supportsVoltageControl())
                views++;

            if (gpu != null)
                views++;

            return views;
        }

        /**
         * Create the page for the given position. The adapter is responsible
         * for adding the view to the container given here, although it only
         * must ensure this is done by the time it returns from
         * {@link #finishUpdate()}.
         * 
         * @param container The containing View in which the page will be shown.
         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page. This does not
         *         need to be a View, but can be some other container of the
         *         page.
         */
        @Override
        public Object instantiateItem(View collection, int position) {
            View v = null;

            // TODO fix this hacky bs, use a linked list maybe

            switch (position) {
                case 0:

                    if (cpuFactory == null)
                        cpuFactory = new CpuFactory(mContext, cpu);
                    else
                        cpuFactory.refreshClocks();

                    v = cpuFactory.getView();

                    break;
                case 1:
                    if (voltageFactory == null)
                        voltageFactory = new VoltageFactory(mContext, cpu);

                    v = voltageFactory.getView();

                    break;
                case 2:
                    if (gpuFactory == null)
                        gpuFactory = new GpuFactory(mContext, gpu);

                    v = gpuFactory.getView();

                    break;
            }

            // hide right/left tab indicators
            TextView tabLeft = (TextView) v.findViewById(R.id.tab_left);
            TextView tabRight = (TextView) v.findViewById(R.id.tab_right);

            if (position == 0) {
                tabLeft.setText("");
            } else if (position == getCount() - 1) {
                tabRight.setText("");
            }

            ((ViewPager) collection).addView(v, position);
            return v;
        }

        /**
         * Remove a page for the given position. The adapter is responsible for
         * removing the view from its container, although it only must ensure
         * this is done by the time it returns from {@link #finishUpdate()}.
         * 
         * @param container The containing View from which the page will be
         *            removed.
         * @param position The page position to be removed.
         * @param object The same object that was returned by
         *            {@link #instantiateItem(View, int)}.
         */
        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);

            if (position == 0) {
                cpuFactory.stopClockRefresh();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        /**
         * Called when the a change in the shown pages has been completed. At
         * this point you must ensure that all of the pages have actually been
         * added or removed from the container as appropriate.
         * 
         * @param container The containing View which is displaying this
         *            adapter's page views.
         */
        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

}
