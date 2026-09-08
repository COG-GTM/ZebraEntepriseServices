package com.zebra.enterpriseservices;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static com.zebra.enterpriseservices.RESTHostServiceConstants.PRINT_SERVER_PORT;

public class RESTHostService extends Service {
    private static final int SERVICE_ID = 2545;
    private static final int PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private RESTServiceWebServer mRESTServer;

    public RESTHostService() {
    }

    public IBinder onBind(Intent paramIntent)
    {
        return null;
    }

    public void onCreate()
    {
        logD("onCreate");
        this.mNotificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
        startService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logD("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    public void onDestroy()
    {
        logD("onDestroy");
        stopService();
    }

    @SuppressLint({"Wakelock"})
    private void startService()
    {
        logD("startService");
        try
        {
            Intent mainActivityIntent = new Intent(this, RESTHostServiceActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    mainActivityIntent,
                    PENDING_INTENT_FLAGS);

            // Create the Foreground Service
            String channelId = createNotificationChannel(mNotificationManager);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            mNotification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.zebra_enterprise_services_notification_title))
                    .setContentText(getString(R.string.zebra_enterprise_services_notification_text))
                    .setTicker(getString(R.string.zebra_enterprise_services_notification_tickle))
                    .setPriority(PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();

            TaskStackBuilder localTaskStackBuilder = TaskStackBuilder.create(this);
            localTaskStackBuilder.addParentStack(RESTHostServiceActivity.class);
            localTaskStackBuilder.addNextIntent(mainActivityIntent);
            notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, PENDING_INTENT_FLAGS));

            // Start foreground service
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                startForeground(SERVICE_ID, mNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            }
            else
            {
                startForeground(SERVICE_ID, mNotification);
            }

            // Launch web server here
            if(mRESTServer != null)
            {
                mRESTServer.closeAllConnections();
                mRESTServer.stop();
                mRESTServer = null;
            }

            mRESTServer = new RESTServiceWebServer(PRINT_SERVER_PORT, getBaseContext());
            mRESTServer.start();
            
            logD("startService:Service started without error.");
        }
        catch(Exception e)
        {
            logD("startService:Error while starting service.");
            e.printStackTrace();
        }


    }

    private void stopService()
    {
        try
        {
            logD("stopService.");

            // Release web server here
            if(mRESTServer != null)
            {
                mRESTServer.closeAllConnections();
                mRESTServer.stop();
                mRESTServer = null;
            }
            
            stopForeground(STOP_FOREGROUND_REMOVE);
            logD("stopService:Service stopped without error.");
        }
        catch(Exception e)
        {
            logD("Error while stopping service.");
            e.printStackTrace();

        }

    }

    private String createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel channel = new NotificationChannel(getString(R.string.zebra_enterprise_services_channel_id), getString(R.string.zebra_enterprise_services_channel_name), NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return getString(R.string.zebra_enterprise_services_channel_id);
    }

    private void logD(String message)
    {
        Log.d(RESTHostServiceConstants.TAG, message);
    }

    public static void startService(Context context)
    {
        Intent myIntent = new Intent(context, RESTHostService.class);
        // Use start foreground service to prevent the runtime error:
        // "not allowed to start service intent app is in background"
        context.startForegroundService(myIntent);
    }

    public static void stopService(Context context)
    {
        Intent myIntent = new Intent(context, RESTHostService.class);
        context.stopService(myIntent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RESTHostService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
