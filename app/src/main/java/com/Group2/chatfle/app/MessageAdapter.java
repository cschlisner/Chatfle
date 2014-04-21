package com.Group2.chatfle.app;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

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
        View v = convertView;
        Message i = messages.get(position);
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
            if (timeStamp != null && i.timestamp != null){
                long unixSeconds = Long.valueOf(i.timestamp);
                Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // the format of your date
                sdf.setTimeZone(TimeZone.getDefault());
                String formattedDate = sdf.format(date);
                timeStamp.setText(formattedDate);
            }
        }
        return v;

    }
}