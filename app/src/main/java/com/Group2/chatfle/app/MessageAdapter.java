package com.Group2.chatfle.app;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> messages;
    public MessageAdapter(Context context, int textViewResourceId, ArrayList<Message> messages) {
        super(context, textViewResourceId, messages);
        this.messages = messages;
    }
    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        View v = convertView;
        Message i = messages.get(position);
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate((i.msg_sender.equals("0"))?R.layout.recmsg_list_row:R.layout.sndmsg_list_row, null);
        }
        if (i != null) {
            TextView msgContent = (TextView) v.findViewById(R.id.message_content);
            TextView timeStamp = (TextView) v.findViewById(R.id.timestamp);

            if (msgContent != null){
                msgContent.setText(i.msg);
            }
            if (timeStamp != null){
                timeStamp.setText(i.timestamp);
            }
        }
        // the view must be returned to our activity
        return v;

    }
}