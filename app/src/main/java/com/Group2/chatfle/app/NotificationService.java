package com.Group2.chatfle.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends IntentService {
    private static ArrayList<NotificationService> serviceList;
    private static ArrayList<Message> notified;
    static {
        serviceList = new ArrayList<NotificationService>();
        notified = new ArrayList<Message>();
    }
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    int running;
    Timer timer = new Timer();
    TimerTask newMsgTimer;
    public NotificationService() {
        super("NotificationService");
        serviceList.add(this);
    }

    Conversation[] conversations;
    @Override
    protected void onHandleIntent(Intent intent) {
        ++running;
        if (running<2) {
            if (intent != null) {
                conversations = new Conversation[intent.getStringArrayListExtra("CONVID").size()];
                for (int i = 0; i < conversations.length; ++i)
                    conversations[i] = new Conversation();
                ArrayList<String>[] convData = new ArrayList[3];
                for (int i = 0; i < 3; ++i)
                    convData[i] = new ArrayList<String>();
                convData[0].addAll(intent.getStringArrayListExtra("CONVID"));
                convData[1].addAll(intent.getStringArrayListExtra("DISPNAME"));
                convData[2].addAll(intent.getStringArrayListExtra("HASNEW"));
                for (int i = 0; i < conversations.length; ++i) {
                    conversations[i].convo_id = convData[0].get(i);
                    conversations[i].display_name = convData[1].get(i);
                    conversations[i].hasNew = Boolean.valueOf(convData[2].get(i));
                }

                newMsgTimer = new TimerTask() {
                    @Override
                    public void run() {
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
                                            JSONObject o = messages.getJSONObject(0);
                                            boolean actualnew = true;
                                            for (Message m : notified)
                                                if (m.timestamp.equals(o.getString("timestamp"))) {
                                                    actualnew = false;
                                                    break;
                                                }
                                            if (actualnew) {
                                                if (messages.length() == 1)
                                                    notifyUser(o.getString("display_name"), o.getString("msg"));
                                                else if (messages.length() > 1)
                                                    notifyUser("Chatfle", messages.length() + " new messages");
                                                notified.add(new Message(o.getString("display_name"),
                                                                         o.getString("msg"),
                                                                         o.getString("timestamp"),
                                                                         o.getString("msg_sender")));
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
                };
                timer.schedule(newMsgTimer, 4000, 1000);
                System.out.println("notification timer scheduled");
            }
        }

    }

    private void stopProcesses(){
        if (running > 0) {
            if (mNotificationManager != null) mNotificationManager.cancelAll();
            if (newMsgTimer != null) newMsgTimer.cancel();
            timer.cancel();
            timer.purge();
            stopSelf();
            System.out.println("Stopped NS ");
        }
    }

    public static void stopAll(){
        if (serviceList != null && !serviceList.isEmpty()) {
            for (NotificationService n : serviceList) {
                System.out.println("notification timer cancelled");
                n.stopProcesses();
                serviceList.remove(n);
            }
        }
    }

    private void notifyUser(String title, String content){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true);
        Intent notifyIntent = new Intent(this, HomeActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pnotifyIntent = PendingIntent.getActivity(this,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pnotifyIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, builder.build());
    }
}
