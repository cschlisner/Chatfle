package com.Group2.chatfle.app;

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
}
