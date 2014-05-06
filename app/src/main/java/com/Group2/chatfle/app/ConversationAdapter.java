package com.Group2.chatfle.app;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConversationAdapter extends ArrayAdapter<Conversation> {
    private ArrayList<Conversation> Conversations;
    public ConversationAdapter(Context context, int textViewResourceId, ArrayList<Conversation> Conversations) {
        super(context, textViewResourceId, Conversations);
        this.Conversations = Conversations;
    }
    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        View v = convertView;
        Conversation i = Conversations.get(position);
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.conv_list_row, null);
        }
        if (position%2==0)
            v.findViewById(R.id.relative_layout).setBackground(Globals.context.getResources().getDrawable(R.drawable.altconvselector));
        if (i != null) {
            TextView dispName = (TextView) v.findViewById(R.id.display_name);
            TextView msgPrev = (TextView) v.findViewById(R.id.msg_preview);

            if (dispName != null){
                dispName.setText(i.display_name);
            }
            if (msgPrev != null){
                msgPrev.setText(i.msg_preview);
            }
        }
        // the view must be returned to our activity
        return v;

    }
}