package com.Group2.chatfle.app;

import java.util.ArrayList;

/**
 * Created by Cole on 4/20/14.
 */
public class Conversation {
    public String convo_id, display_name, their_user,  msg_preview;
    public boolean hasNew, can_reveal;
    public ArrayList<Message> msgList;
    public Conversation(String convo_id, String display_name, String their_user, String msg_preview, String hasNew, String can_reveal){
        this.convo_id = convo_id;
        this.display_name = display_name;
        this.their_user = their_user;
        this.msg_preview = msg_preview;
        this.hasNew = Boolean.valueOf(hasNew);
        this.can_reveal = Boolean.valueOf(can_reveal);
        msgList = new ArrayList<Message>();
    }
    public Conversation(){
        this.convo_id = "";
        this.display_name = "";
        this.their_user = "";
        this.msg_preview = "";
        this.can_reveal = false;
        this.hasNew = false;
        msgList = new ArrayList<Message>();
    }
}
