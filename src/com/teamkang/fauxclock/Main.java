
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

        awesomeAdapter = new AwesomePagerAdapter();
        awesomePager = (ViewPager) findViewById(R.id.awesomepager);
        awesomePager.setAdapter(awesomeAdapter);
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
