
package com.teamkang.fauxclock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import ru.org.amip.MarketAccess.utils.ShellInterface;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AquireRootTask extends AsyncTask<Void, Integer, Boolean> {

    View view;
    // TextView status;
    // Activity activity;
    Context mContext;
    OCApplication bc;

    private static final int PROGRESS_AQUIRING_ROOT = 1;
    private static final int PROGRESS_ROOT_AQUIRED = 2;
    private static final int PROGRESS_ROOT_NOT_AQUIRED = 3;
    private static final int PROGRESS_BUSYBOX_CHECKING = 6;
    private static final int PROGRESS_BUSYBOX_FOUND = 4;
    private static final int PROGRESS_BUSYBOX_NOT_FOUND = 7;
    private static final int PROGRESS_DONE = 5;

    public AquireRootTask(Context c) {
        bc = ((OCApplication) c.getApplicationContext());
    }

    public AquireRootTask(View v, Context c) {
        view = v;
        mContext = c;
        // activity = a;
        bc = ((OCApplication) c.getApplicationContext());
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // View layout = inflater.inflate(R.layout.aquireroot,
        // (ViewGroup) ((Activity) getCallingContext())
        // .findViewById(R.id.statuslayout), false);

        View layout = view;

        TextView status = (TextView) layout.findViewById(R.id.status);
        ProgressBar spinner = (ProgressBar) layout
                .findViewById(R.id.progressBar1);
        Log.d("EDT", (String) status.getText());
        switch (progress[0]) {
            case PROGRESS_AQUIRING_ROOT:
                status.setText("Aquiring root");
                break;

            case PROGRESS_ROOT_AQUIRED:
                status.setText("Root aquired!");
                break;

            case PROGRESS_ROOT_NOT_AQUIRED:
                spinner.setVisibility(View.GONE);
                status.setText("FauxClock needs root privaleges to run. Please make sure it has the proper permissions in SuperUser.");
                break;

            case PROGRESS_BUSYBOX_CHECKING:
                spinner.setVisibility(View.VISIBLE);
                status.setText("Checking for busybox");
                break;

            case PROGRESS_BUSYBOX_NOT_FOUND:
                spinner.setVisibility(View.GONE);
                status.setText("Busybox was not found. Please try and install it from the market.");
                break;

            case PROGRESS_BUSYBOX_FOUND:
                spinner.setVisibility(View.GONE);
                status.setText("Busybox was found!");
                break;
            case PROGRESS_DONE:
                status.setText("Checks passed!");
                break;
        }

    }

    protected void onPostExecute(Boolean result) {
        // localDialog.dismiss();
        if (result) {
            Intent i = new Intent(mContext, Main.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            bc.startActivity(i);
            ((Activity) mContext).finish();
        }
        // status.setText("Test");

    }

    public static boolean canRunRootCommands() {
        boolean retval = false;
        Process suProcess;

        try
        {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os =
                    new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes =
                    new DataInputStream(suProcess.getInputStream());

            if (null != os && null != osRes)
            {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                boolean exitSu = false;
                if (null == currUid)
                {
                    retval = false;
                    exitSu = false;
                    Log.d("ROOT", "Can't get root access or denied by user");
                }
                else if (true == currUid.contains("uid=0"))
                {
                    retval = true;
                    exitSu = true;
                    Log.d("ROOT", "Root access granted");
                }
                else
                {
                    retval = false;
                    exitSu = true;
                    Log.d("ROOT", "Root access rejected: " + currUid);
                }

                if (exitSu)
                {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        } catch (Exception e)
        {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output
            // stream after su failed, meaning that the device is not rooted

            retval = false;
            Log.d("ROOT", "Root access rejected [" +
                    e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        ((OCApplication) mContext.getApplicationContext()).setPhoneManagerStuff();
        
        publishProgress(PROGRESS_AQUIRING_ROOT);
        
        if (canRunRootCommands()) {
            publishProgress(PROGRESS_ROOT_AQUIRED);
        } else {
            publishProgress(PROGRESS_ROOT_NOT_AQUIRED);
            return false;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        
        publishProgress(PROGRESS_DONE);
        
        if(true)
            return true;

        publishProgress(PROGRESS_BUSYBOX_CHECKING);

        String busyboxOutput = "";
        if (ShellInterface.isSuAvailable()) {
            busyboxOutput = ShellInterface.getProcessOutput("busybox");
        }
        if (busyboxOutput.contains("not found") || busyboxOutput.equals("")) {
            Log.d("EDT", busyboxOutput);
            publishProgress(PROGRESS_BUSYBOX_NOT_FOUND);
            return false;
        } else {
            publishProgress(PROGRESS_BUSYBOX_FOUND);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        publishProgress(PROGRESS_DONE);

        return true;
    }
}
