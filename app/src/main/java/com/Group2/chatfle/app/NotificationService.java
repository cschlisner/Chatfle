package com.Group2.chatfle.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends BroadcastReceiver {
    public boolean ready;
    static Conversation[] conversations;
    private static Message notified = new Message();
    @Override
    public void onReceive(final Context context, Intent intent){
        if (conversations != null && conversations.length>0) {
            for (Conversation c : conversations) {
                Networking.execute(new NetCallBack<Void, String>() {
                    @Override
                    public Void callPre() {
                        return null;
                    }

                    @Override
                    public Void callPost(String result) {
                        if (result != null) {
                            try {
                                JSONObject jso = new JSONObject(result);
                                JSONArray messages = jso.getJSONArray("messages");
                                JSONObject o = messages.getJSONObject(messages.length()-1);
                                if (!notified.timestamp.equals(o.getString("timestamp"))) {
                                    if (messages.length() == 1)
                                        notifyUser(context, o.getString("display_name"), o.getString("msg"));
                                    else if (messages.length() > 1)
                                        notifyUser(context, "Chatfle", messages.length() + " new messages");
                                    notified = new Message(o);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                }, "http://m.chatfle.com/get_new.php", "hash", Globals.hash, "convo_id", c.convo_id);
            }
        }
    }

    public void setAlarm(Context context, int repeat, Conversation[] conversations){
        ready = true;
        NotificationService.conversations = conversations;
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeat, pi);
        Log.d(getClass().toString(), "setAlarm");
    }

    public void cancelAlarm(Context context){
        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(sender);
        Log.d(getClass().toString(), "cancelAlarm");
    }

    public void notifyUser(Context context, String title, String content){
        NotificationManager mNotificationManager;
        long vibr[] = {0,500};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setLights(Color.argb(100, 12, 140, 0), 500, 2500)
                .setVibrate(vibr)
                .setSound(Uri.fromFile(new File("file:///android_asset/sound/Button.mp3")));
        Intent notifyIntent = new Intent(context, NotifcationClickActivity.class);
        PendingIntent pnotifyIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pnotifyIntent);
        Notification n = builder.build();
        n.defaults |= Notification.DEFAULT_SOUND;
        n.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, n);
    }
}
