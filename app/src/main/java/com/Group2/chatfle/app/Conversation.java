package com.Group2.chatfle.app;

import java.util.ArrayList;

/**
 * Created by Cole on 4/20/14.
 */
public class Conversation {
    public String convo_id, display_name, msg_preview;
    public ArrayList<Message> msgList;
    public Conversation(){
        msgList = new ArrayList<Message>();
    }
}
