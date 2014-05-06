package com.Group2.chatfle.app;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> messages;
    public MessageAdapter(Context context, int textViewResourceId, Conversation conversation) {
        super(context, textViewResourceId, conversation.msgList);
        this.messages = conversation.msgList;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        Message i = messages.get(position);
        String vtag;
        try {
            vtag = v.getTag().toString();
        } catch (NullPointerException e) {
            vtag = "nope";
        }
        if (v == null || i.msg_sender.equals("1")&&vtag.equals(Integer.toString(R.layout.recmsg_list_row)) ||
                         i.msg_sender.equals("0")&&vtag.equals(Integer.toString(R.layout.sndmsg_list_row))) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate((i.msg_sender.equals("1"))?R.layout.sndmsg_list_row:R.layout.recmsg_list_row, null);
            v.setTag((i.msg_sender.equals("1"))?R.layout.sndmsg_list_row:R.layout.recmsg_list_row);
        }
        if (i != null) {
            TextView msgContent = (TextView) v.findViewById(R.id.message_content);
            TextView timeStamp = (TextView) v.findViewById(R.id.timestamp);
            if (msgContent != null){
                msgContent.setText(i.msg);
            }
            if (timeStamp != null && i.timestamp != null){
                if (i.timestamp.equals("Just Now"))
                    timeStamp.setText(i.timestamp);
                else {
                    long unixSeconds = Long.valueOf(i.timestamp);
                    Date date = new Date(unixSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    sdf.setTimeZone(TimeZone.getDefault());
                    String formattedDate = sdf.format(date);
                    timeStamp.setText(formattedDate);
                }
            }
        }
        return v;

    }
}