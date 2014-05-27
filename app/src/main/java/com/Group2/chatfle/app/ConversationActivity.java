package com.Group2.chatfle.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
    NotificationService n = null;
    private static Object syncObj = new Object();
    ViewPager mViewPager;
    static boolean sendingMsg, checkNew, stopChecking, convosLoaded, timerRunning;
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

        ArrayList<String>[] convData = new ArrayList[3];
        for (int i=0; i<3; ++i)
            convData[i] = new ArrayList<String>();
        convData[0].addAll(intent.getStringArrayListExtra("CONVID"));
        convData[1].addAll(intent.getStringArrayListExtra("DISPNAME"));
        convData[2].addAll(intent.getStringArrayListExtra("CANREV"));
        for (int i=0; i<conversations.length; ++i)
            conversations[i] = new Conversation(convData[0].get(i), convData[1].get(i), "", "", "false", convData[2].get(i));
        currentConvo = intent.getIntExtra("POSITION", 0);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new pageChange());
        mViewPager.setCurrentItem(currentConvo);
        setTitle(conversations[currentConvo].display_name);
        readMessages(currentConvo);
        //Log.d("OnCreate", "end");
    }

    class pageChange implements ViewPager.OnPageChangeListener {
        public pageChange(){}
        @Override
        public void onPageSelected(int position) {
            currentConvo = position;
            setTitle(conversations[currentConvo].display_name);
            invalidateOptionsMenu();
            if (mSectionsPagerAdapter.getFragmentAt(currentConvo)!=null)
                getMessages(mSectionsPagerAdapter.getFragmentAt(currentConvo), null, null);
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
            if (position==((currentConvo<conversations.length-1)?currentConvo+1:currentConvo) && !timerRunning) {
                stopChecking = false;
                timer.cancel();
                System.out.println("timer cancelled");
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!sendingMsg&&!checkNew) {
                            checkNew = true;
                            for (int i=(currentConvo>0)?currentConvo-1:currentConvo; i<=((currentConvo+1<conversations.length)?currentConvo+1:currentConvo); ++i)
                                checkNewMessage(i);
                        }
                    }
                }, 4000, 1000);
                System.out.println("timer scheduled");
                convosLoaded = true;
                timerRunning = true;
            }
            //Log.d("getItem", "end");
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
        //Log.d("getMessages", "start");
        if (gettingMsgs < 4) {
            final int position = frag.getArguments().getInt("section_number");
            final ListView msgList = (lv==null)?((ListView) frag.getView().findViewById(R.id.messageList)):lv;
            final String convoId = conversations[position].convo_id;
            Networking.execute(new NetCallBack<Void, String>() {
                @Override
                public Void callPre() {
                    //Log.d("getMessages", "callPre");
                    stopChecking = true;
                    if (pb != null)
                        pb.setVisibility(View.VISIBLE);
                    ++gettingMsgs;
                    return null;
                }

                @Override
                public Void callPost(String result) {
                    ////Log.d("getMessages", "callPost");
                    stopChecking = false;
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
                                for (int i = 0; i < messages.length(); ++i)
                                    conversations[position].msgList.add(new Message(messages.getJSONObject(i)));
                            }
                            else Toast.makeText(Globals.context, "Send a message to get started", Toast.LENGTH_SHORT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //System.out.println(result);
                    msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[position]));
                    msgList.getAdapter();
                    return null;
                }
            }, "http://m.chatfle.com/get_messages.php", "hash", Globals.hash, "convo_id", convoId);
        }
    }

    public static void checkNewMessage(final int i){
        ////Log.d("checkNewMessage", "start");
        if (checkingMsgs<4 && !stopChecking) {
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
                       try {
                           JSONObject jso = new JSONObject(result);
                           JSONArray messages = jso.getJSONArray("messages");
                           for (int i = 0; i < messages.length(); ++i) {
                               Message m = new Message(messages.getJSONObject(i));
                               if (!m.timestamp.equals(conversations[position].msgList.get(conversations[position].msgList.size()-1)))
                                    conversations[position].msgList.add(m);
                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                       msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[position]));
                       msgList.getAdapter();
                       conversations[currentConvo].hasNew = (i != currentConvo);
                       if (!conversations[currentConvo].hasNew) readMessages(i);
                   }
                   if (position == ((currentConvo+1<conversations.length)?currentConvo+1:currentConvo))
                       checkNew = false;
                   return null;
               }
           }, "http://m.chatfle.com/get_new.php", "hash", Globals.hash, "convo_id", convoId);
       }
    }

    public void sendMessage(View v){
        //Log.d("sendMessage", "start");
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
                    //Globals.hash = "thisisahashiswear";
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
                    else if (result.equals("NOHASH")) {
                        Toast.makeText(Globals.context, "Invalid hash, please login", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Globals.context);
                        SharedPreferences.Editor e = prefs.edit();
                        e.putString("CREDENTIALS", "");
                        e.commit();
                        startActivity(new Intent(Globals.context, LoginActivity.class));
                        finish();
                    }
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
        //Log.d("ReadMessage", "start");
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {return null;}
            @Override
            public Void callPost(String result) {
                if (result.equals("SUCCESS")) conversations[i].hasNew = false;
                return null;
            }
        }, "http://m.chatfle.com/update_read.php", "hash", Globals.hash, "convo_id", conversations[i].convo_id);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String FRAG_POS = "section_number";
        public static PlaceholderFragment newInstance(int sectionNumber) {
            //Log.d("newInstance", "start");
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
            //Log.d("OnCreateView", "start");
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
        menu.findItem(R.id.action_reveal).setVisible(conversations[currentConvo].can_reveal);
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
        timer.cancel();
        timer.purge();
        stopChecking = true;
        System.out.println("timer cancelled");
        if (HomeActivity.isPaused) {
            n = new NotificationService();
            Globals.notificationService = n;
            n.setAlarm(getApplicationContext(), 1000, conversations);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        //Log.d("OnResume", "start");
        if (n != null) n.cancelAlarm(getApplicationContext());
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!sendingMsg&&!checkNew) {
                    checkNew = true;
                    for (int i=(currentConvo>0)?currentConvo-1:currentConvo; i<=((currentConvo+1<conversations.length)?currentConvo+1:currentConvo); ++i)
                        checkNewMessage(i);
                }
            }
        }, 4000, 1000);
        System.out.println("timer scheduled");
        super.onResume();
    }


    @Override
    public void finish() {
        timer.cancel();
        timer.purge();
        stopChecking = true;
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                getMessages(mSectionsPagerAdapter.getFragmentAt(mViewPager.getCurrentItem()), null, null);
                return true;
            case R.id.action_reveal:
                reveal();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reveal(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Reveal Identity");
        alert.setMessage("Are you sure you want to reveal yourself?");
        alert.setInverseBackgroundForced(true);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Networking.execute(new NetCallBack<Void, String>() {
                    @Override
                    public Void callPre() {
                        return null;
                    }

                    @Override
                    public Void callPost(String result) {
                        String msg;
                        if (result.equals("NOT STARTER")) msg = "You didn't start this conversation!";
                        else if (result.equals("ALREADY REVEALED")) msg = "You have already revealed yourself!";
                        else msg = "Identity Revelaed";
                        Toast.makeText(Globals.context, msg, Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }, "http://m.chatfle.com/user_reveal.php", "hash", Globals.hash, "convo_id", conversations[currentConvo].convo_id);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }
}
