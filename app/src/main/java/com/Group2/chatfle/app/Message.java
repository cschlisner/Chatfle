package com.Group2.chatfle.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Cole on 4/20/14.
 */
public class Message {
    public String display_name="", msg="", timestamp="", msg_sender="";
    public Message(String display_name, String msg, String timestamp, String msg_sender){
        this.display_name = display_name;
        this.msg = msg;
        this.timestamp = timestamp;
        this.msg_sender = msg_sender;
    }
    public Message(){
        this.display_name = "";
        this.msg = "";
        this.timestamp = "";
        this.msg_sender = "";
    }
    public Message(JSONObject o){
        try {
            this.display_name = o.getString("display_name");
            this.msg = o.getString("msg");
            this.timestamp = o.getString("timestamp");
            this.msg_sender = o.getString("msg_sender");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
