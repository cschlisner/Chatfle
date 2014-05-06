package com.Group2.chatfle.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.GsmCellLocation;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ConversationActivity extends ActionBarActivity {
    static SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    Toast toast;
    static boolean sendingMsg, checkNew, stopChecking;
    static Conversation conversations[];
    EditText msgBox;
    ImageButton sendBtn;
    ProgressBar sndProgBar;
    static TimerTask newMsgTimer;
    static Timer timer = new Timer();
    static int currentConvo, gettingMsgs, checkingMsgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.context = this;
        setContentView(R.layout.activity_conversation);
        FlatUI.setDefaultTheme(FlatUI.DARK);
        FlatUI.setActionBarTheme(this, FlatUI.DARK, true, true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        sendBtn = (ImageButton) findViewById(R.id.sendButton);
        sndProgBar = (ProgressBar) findViewById(R.id.progBar);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(view);
                view.setVisibility(View.GONE);
            }
        });
        msgBox = (EditText) findViewById(R.id.messageBox);
        msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                int chars = editable.length();
                sendBtn.setVisibility((chars>0&&!sendingMsg)?View.VISIBLE:View.GONE);
            }
        });
        msgBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendBtn.performClick();
                    handled = true;
                }
                return handled;
            }
        });
        Intent intent = getIntent();
        conversations = new Conversation[intent.getStringArrayListExtra("CONVID").size()];
        for (int i=0; i<conversations.length; ++i)
            conversations[i] = new Conversation();
        ArrayList<String>[] convData = new ArrayList[3];
        for (int i=0; i<3; ++i)
            convData[i] = new ArrayList<String>();
        convData[0].addAll(intent.getStringArrayListExtra("CONVID"));
        convData[1].addAll(intent.getStringArrayListExtra("DISPNAME"));
        convData[2].addAll(intent.getStringArrayListExtra("MSGPREV"));
        for (int i=0; i<conversations.length; ++i) {
            conversations[i].convo_id = convData[0].get(i);
            conversations[i].display_name = convData[1].get(i);
            conversations[i].msg_preview = convData[2].get(i);
        }
        currentConvo = intent.getIntExtra("POSITION", 0);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new pageChange());
        mViewPager.setCurrentItem(currentConvo);
        setTitle(conversations[currentConvo].display_name);

        newMsgTimer = new TimerTask() {
            @Override
            public void run() {
            if (!sendingMsg&&!checkNew) {
                checkNew = true;
                for (int i=(currentConvo>0)?currentConvo-1:currentConvo; i<=((currentConvo+1<conversations.length)?currentConvo+1:currentConvo); ++i)
                    checkNewMessage(i);
            }
            }
        };
    }

    class pageChange implements ViewPager.OnPageChangeListener {
        public pageChange(){}
        @Override
        public void onPageSelected(int position) {
            setTitle(conversations[position].display_name);
            currentConvo = position;
            if (mSectionsPagerAdapter.getFragmentAt(position)!=null)
                getMessages(mSectionsPagerAdapter.getFragmentAt(position), null, null);
            if (conversations[currentConvo].hasNew)
                readMessages(currentConvo);
        }
        @Override
        public void onPageScrollStateChanged(int i){

        }
        @Override
        public void onPageScrolled(int a, float b, int c){

        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position==((currentConvo<conversations.length-1)?currentConvo+1:currentConvo)) {
                stopChecking = false;
                timer = new Timer();
                timer.schedule(newMsgTimer, 4000, 1000);
                System.out.println("timer scheduled");
            }
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return conversations.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return conversations[position].display_name;
        }

        public Fragment getFragmentAt(int position){
            return getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+Integer.toString(position));
        }
    }

    private static void getMessages(final Fragment frag, final ListView lv, final ProgressBar pb){
        if (gettingMsgs < 4) {
            final int position = frag.getArguments().getInt("section_number");
            final ListView msgList = (lv==null)?((ListView) frag.getView().findViewById(R.id.messageList)):lv;
            final String convoId = conversations[position].convo_id;
            Networking.execute(new NetCallBack<Void, String>() {
                @Override
                public Void callPre() {
                    if (pb != null)
                        pb.setVisibility(View.VISIBLE);
                    ++gettingMsgs;
                    return null;
                }

                @Override
                public Void callPost(String result) {
                    --gettingMsgs;
                    if (pb != null)
                        pb.setVisibility(View.GONE);
                    // handle JSON
                    if (result != null) {
                        try {
                            JSONObject jso = new JSONObject(result);
                            JSONArray messages = jso.getJSONArray("messages");
                            conversations[position].msgList.clear();
                            if (messages.length()>0) {
                                for (int i = 0; i < messages.length(); ++i) {
                                    JSONObject o = messages.getJSONObject(i);
                                    Message m = new Message(o.getString("display_name"),
                                                            o.getString("msg"),
                                                            o.getString("timestamp"),
                                                            o.getString("msg_sender"));
                                    conversations[position].msgList.add(m);
                                }
                            }
                            else Toast.makeText(Globals.context, "Send a message to get started", Toast.LENGTH_SHORT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[position]));
                    msgList.getAdapter();
                    return null;
                }
            }, "http://m.chatfle.com/get_messages.php", "hash", Globals.hash, "convo_id", convoId);
        }
    }

    public static void checkNewMessage(final int i){
        if (checkingMsgs<4) {
           final Fragment frag = mSectionsPagerAdapter.getFragmentAt(i);
           final ListView msgList = (ListView) frag.getView().findViewById(R.id.messageList);
           final int position = frag.getArguments().getInt("section_number");
           final String convoId = conversations[position].convo_id;
           Networking.execute(new NetCallBack<Void, String>() {
               @Override
               public Void callPre() {
                   ++checkingMsgs;
                   return null;
               }

               @Override
               public Void callPost(String result) {
                   --checkingMsgs;
                   if (result != null) {
                       conversations[currentConvo].hasNew = (i != currentConvo);
                       if (conversations[currentConvo].hasNew == false) readMessages(i);
                       try {
                           JSONObject jso = new JSONObject(result);
                           JSONArray messages = jso.getJSONArray("messages");
                           for (int i = 0; i < messages.length(); ++i) {
                               JSONObject o = messages.getJSONObject(i);
                               Message m = new Message(o.getString("display_name"),
                                       o.getString("msg"),
                                       o.getString("timestamp"),
                                       o.getString("msg_sender"));
                               conversations[position].msgList.add(m);
                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                       msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[position]));
                       msgList.getAdapter();
                   }
                   if (position == ((currentConvo+1<conversations.length)?currentConvo+1:currentConvo))
                       checkNew = false;
                   return null;
               }
           }, "http://m.chatfle.com/get_new.php", "hash", Globals.hash, "convo_id", convoId);
       }
    }

    public void sendMessage(View v){
        int convo = mViewPager.getCurrentItem();
        final Fragment frag = mSectionsPagerAdapter.getFragmentAt(convo);
        final int fragPos = frag.getArguments().getInt("section_number");
        final boolean isNewConvo = (conversations[convo].msgList.size()<1);
        try {
            final String msg = msgBox.getText().toString();
            Networking.execute(new NetCallBack<Void, String>() {
                @Override
                public Void callPre() {
                    frag.getView().findViewById(R.id.frag_convo_layout).requestFocus();
                    sndProgBar.setVisibility(View.VISIBLE);
                    msgBox.requestFocus();
                    sendingMsg = true;
                    conversations[fragPos].msgList.add(new Message(null, msg, "Just Now", "1"));
                    ListView msgList = (ListView) frag.getView().findViewById(R.id.messageList);
                    msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[fragPos]));
                    msgList.getAdapter();
                    msgList.smoothScrollToPosition(conversations[fragPos].msgList.size());
                    return null;
                }

                @Override
                public Void callPost(String result) {
                    sndProgBar.setVisibility(View.GONE);
                    sendingMsg = false;
                    if (!msgBox.getText().toString().isEmpty())
                        sendBtn.setVisibility(View.VISIBLE);
                    if (result == null) {
                        System.out.print("nope");
                        conversations[fragPos].msgList.remove(conversations[fragPos].msgList.size());
                        ListView msgList = (ListView) frag.getView().findViewById(R.id.messageList);
                        msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[fragPos]));
                        msgList.getAdapter();
                        msgList.smoothScrollToPosition(conversations[fragPos].msgList.size());
                        Toast.makeText(Globals.context, "Could not send", Toast.LENGTH_SHORT).show();
                    }
                    else if (result == "DISABLED") Toast.makeText(Globals.context, "User is disabled", Toast.LENGTH_SHORT).show();
                    else if (result == "ALREADYIN") Toast.makeText(Globals.context, "Already in conversation", Toast.LENGTH_SHORT).show();
                    else if (result == "NOTEXIST") Toast.makeText(Globals.context, "User does not exist", Toast.LENGTH_SHORT).show();
                    else if (result == "NOTYOU") Toast.makeText(Globals.context, "You can't talk to yourself", Toast.LENGTH_SHORT).show();
                    else if (result == "NOHASH") {
                        Toast.makeText(Globals.context, "Invalid hash, please login", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Globals.context);
                        SharedPreferences.Editor e = prefs.edit();
                        e.putString("CREDENTIALS", "");
                        e.commit();
                        startActivity(new Intent(Globals.context, LoginActivity.class));
                        finish();
                    }
                    else if (result == "NOPOST") Toast.makeText(Globals.context, "Could not send", Toast.LENGTH_SHORT).show();
                        return null;
                }
            }, "http://m.chatfle.com/"+((isNewConvo)?"start_convo.php":"send_message.php"), "hash", Globals.hash,
                    (isNewConvo)?"their_user":"convo_id", ((isNewConvo)?conversations[convo].display_name:conversations[convo].convo_id), "message", msg);
            msgBox.setText("");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private static void readMessages(final int i){
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {return null;}
            @Override
            public Void callPost(String result) {
                conversations[i].hasNew = false;
                if (result.equals("SUCCESS"))
                    Toast.makeText(Globals.context, "read success", Toast.LENGTH_SHORT).show();
                return null;
            }
        }, "http://m.chatfle.com/update_read.php", "hash", Globals.hash, "convo_id", conversations[i].convo_id);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String FRAG_POS = "section_number";
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(FRAG_POS, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        public PlaceholderFragment() {  /* keep me empty, thanks*/   }
        private ListView msgList;
        private ProgressBar progBar;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_convo, container, false);
            msgList = (ListView) rootView.findViewById(R.id.messageList);
            progBar = (ProgressBar) rootView.findViewById(R.id.loadingImg);
            RelativeLayout myLayout = (RelativeLayout) rootView.findViewById(R.id.frag_convo_layout);
            myLayout.requestFocus();
            getMessages(this, msgList, progBar);
            return rootView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation, menu);
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        newMsgTimer.cancel();
        timer.cancel();
        stopChecking = true;
        System.out.println("timer cancelled");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void finish() {
        newMsgTimer.cancel();
        timer.cancel();
        timer.purge();
        stopChecking = true;
        final ArrayList<String>[] convData = new ArrayList[4];
        for (int q=0; q<convData.length; ++q)
            convData[q] = new ArrayList<String>();
        for (Conversation c : conversations){
            convData[0].add(c.convo_id);
            convData[1].add(c.display_name);
            convData[2].add(c.msg_preview);
            convData[3].add(Boolean.toString(c.hasNew));
        }
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("SENDER", conversations[currentConvo].display_name);
        intent.putExtra("MESSAGE", conversations[currentConvo].msg_preview);
        intent.putExtra("CONVID", convData[0]);
        intent.putExtra("DISPNAME", convData[1]);
        intent.putExtra("MSGPREV", convData[2]);
        intent.putExtra("HASNEW", convData[3]);
        startService(intent);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getMessages(mSectionsPagerAdapter.getFragmentAt(mViewPager.getCurrentItem()), null, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
