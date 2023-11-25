package com.bhola.livevideochat4;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BotMessageService extends Service {

    private Handler handler;
    private Runnable runnable;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service logic code
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            int delayTime = 0;
            if (i == 0) {
                delayTime = 1500;
            } else if (i == 1 || i == 2) {
                delayTime = i * 8000;
            } else {
                delayTime = 20000;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(MyApplication.TAG, "run: "+finalI);
                    Fragment_Messenger.userListTemp.add(0,   Fragment_Messenger.userList.get(finalI));
                    Fragment_Messenger.adapter.notifyItemInserted(0);
//
//                    Intent intent = new Intent("timer-update");
//                    intent.putExtra("remainingTime", millisUntilFinished);
//                    sendBroadcast(intent);
                }
            }, delayTime);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // You can override this method if your service is bound
    }


    private void showNotification() {

        // Create a notification channel (required for Android 8.0 and above)


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MyApplication.TAG, "onDestroy: ");
    }
}
