
package com.teamkang.fauxclock;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ShellService extends Service {

    Process process;
    OutputStream os;
    private final IBinder mBinder = new MyBinder();

    public static String TAG = "ShellService";

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        try {
            process = Runtime.getRuntime().exec("su");
            os = process.getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void runCommand(String command) {
        synchronized (this) {

            //Log.e(TAG, "runCommand: " + command);

            try {
                command += "\n";
                os.write(command.getBytes());
                os.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

        }

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.e(TAG, "onStartCommand");
        runCommand(intent.getStringExtra("command"));
        return START_STICKY;
    }

    public void onDestroy() {
        Log.e(TAG, "Destroying shell");
        try {
            os = new DataOutputStream(process.getOutputStream());
            String command = "exit + \n";
            os.write(command.getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;

    }

    public class MyBinder extends Binder {
        ShellService getService() {
            return ShellService.this;
        }
    }

}
