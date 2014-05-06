package com.Group2.chatfle.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public NotificationService() {
        super("NotificationService");

        Intent resultIntent = new Intent(this, HomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    Conversation[] conversations;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            conversations = new Conversation[intent.getStringArrayListExtra("CONVID").size()];
            for (int i=0; i<conversations.length; ++i)
                conversations[i] = new Conversation();
            ArrayList<String>[] convData = new ArrayList[4];
            for (int i=0; i<4; ++i)
                convData[i] = new ArrayList<String>();
            convData[0].addAll(intent.getStringArrayListExtra("CONVID"));
            convData[1].addAll(intent.getStringArrayListExtra("DISPNAME"));
            convData[2].addAll(intent.getStringArrayListExtra("MSGPREV"));
            convData[3].addAll(intent.getStringArrayListExtra("HASNEW"));
            for (int i=0; i<conversations.length; ++i) {
                conversations[i].convo_id = convData[0].get(i);
                conversations[i].display_name = convData[1].get(i);
                conversations[i].msg_preview = convData[2].get(i);
                conversations[i].hasNew = Boolean.valueOf(convData[3].get(i));
            }
            TimerTask newMsgTimer = new TimerTask() {
                @Override
                public void run() {
                    for (int i = 0; i < conversations.length; ++i) {
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
                                        notifyUser(o.getString("display_name"), o.getString("msg"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return null;
                            }
                        }, "http://m.chatfle.com/get_new.php", "hash", Globals.hash, "convo_id", conversations[i].convo_id);
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(newMsgTimer, 4000, 1000);
            System.out.println("timer scheduled");
        }
    }

    private void notifyUser(String title, String content){
        mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
