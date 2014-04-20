package com.Group2.chatfle.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.GsmCellLocation;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    static boolean sendingMsg, checkNew;
    static String[][] conversations;
    static ArrayList<String>[][] messages;
    static ArrayList<String>[] newMsg = new ArrayList[4];
    EditText msgBox;
    ImageButton sendBtn;
    ProgressBar sndProgBar;
    static TimerTask newMsgTimer;
    static Timer timer = new Timer();
    static int currentConvo;
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
        Intent intent = getIntent();
        conversations = new String[3][intent.getStringArrayExtra("CONVID").length];
        conversations[0] = intent.getStringArrayExtra("CONVID");
        conversations[1] = intent.getStringArrayExtra("DISPNAME");
        conversations[2] = intent.getStringArrayExtra("MSGPREV");
        currentConvo = intent.getIntExtra("POSITION", 0);
        messages  = new ArrayList[conversations[0].length][4];
        for (int i=0; i<messages.length; ++i)
            for (int j=0; j<messages[i].length; ++j)
                messages[i][j] = new ArrayList<String>();
        for (int i = 0; i < newMsg.length; ++i)
            newMsg[i] = new ArrayList<String>();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new pageChange());
        mViewPager.setCurrentItem(currentConvo);
        setTitle(conversations[1][currentConvo]);

        newMsgTimer = new TimerTask() {
            @Override
            public void run() {
            if (!sendingMsg&&!checkNew) {
                checkNew = true;
                checkNewMessage(mSectionsPagerAdapter.getFragmentAt(0));
            }
            }
        };
    }

    class pageChange implements ViewPager.OnPageChangeListener {
        public pageChange(){}
        @Override
        public void onPageSelected(int position) {
            setTitle(conversations[1][position]);
            if (((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).isAcceptingText())
                Toast.makeText(Globals.context, mSectionsPagerAdapter.getPageTitle(position), Toast.LENGTH_SHORT).show();
            currentConvo = position;
            if (mSectionsPagerAdapter.getFragmentAt(position)!=null)
                getMessages(mSectionsPagerAdapter.getFragmentAt(position), null, null);
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
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return conversations[0].length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return conversations[1][position];
        }

        public Fragment getFragmentAt(int position){
            return getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+Integer.toString(position));
        }
    }

    private static void getMessages(final Fragment frag, final ListView lv, final ProgressBar pb){
        final int position = frag.getArguments().getInt("section_number");
        System.out.println("Getting Messages for convo "+position);
        final ListView msgList = (lv==null)?((ListView) frag.getView().findViewById(R.id.messageList)):lv;
        final String convoId = conversations[0][position];
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {
                if (pb!=null)
                    pb.setVisibility(View.VISIBLE);
                return null;
            }

            @Override
            public Void callPost(String result) {
                if (pb!=null)
                    pb.setVisibility(View.GONE);
                // handle JSON
                try {
                    JSONObject jso = new JSONObject(result);
                    JSONArray convos = jso.getJSONArray("messages");
                    for (ArrayList<String> l : messages[position])
                        l.clear();
                    for (int i = 0; i<convos.length(); ++i) {
                        JSONObject o = convos.getJSONObject(i);
                        messages[position][0].add(o.getString("display_name"));
                        messages[position][1].add(o.getString("msg"));
                        messages[position][2].add(o.getString("timestamp"));
                        messages[position][3].add(o.getString("msg_sender"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msgList.setAdapter(new ArrayAdapter<String>(Globals.context, android.R.layout.simple_list_item_1, messages[position][1]));
                msgList.getAdapter();
                return null;
            }
        }, "http://m.chatfle.com/get_messages.php", "hash", Globals.hash, "convo_id", convoId);
    }

    public static void checkNewMessage(final Fragment frag){
        final int position = frag.getArguments().getInt("section_number");
        final String convoId = conversations[0][position];
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {
                System.out.println("callPre "+position);
                return null;
            }

            @Override
            public Void callPost(String result) {
                System.out.println("callPost "+position);
                if (messages[position][0].size() > 0) {
                    try {
                        JSONObject jso = new JSONObject(result);
                        JSONArray convos = jso.getJSONArray("messages");
                        JSONObject o = convos.getJSONObject(0);
                        newMsg[0].add(o.getString("display_name"));
                        newMsg[1].add(o.getString("msg"));
                        newMsg[2].add(o.getString("timestamp"));
                        newMsg[3].add(o.getString("msg_sender"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int yep = 0;
                    for (int i = 0; i < 4; ++i) {
                        if (newMsg[i].get(0).equals(messages[position][i].get(messages[position][i].size() - 1)))
                            ++yep;
                        newMsg[i].clear();
                    }
                    if (yep < 4)
                        getMessages(mSectionsPagerAdapter.getFragmentAt(position), null, null);
                    if (position<conversations[0].length-1)
                        checkNewMessage(mSectionsPagerAdapter.getFragmentAt(position+1));
                    else checkNew = false;
                }
                return null;
            }
        }, "http://m.chatfle.com/new_messages.php", "hash", Globals.hash, "convo_id", convoId, "msg_count", "1");
    }

    public void sendMessage(View v){
        int convo = mViewPager.getCurrentItem();
        final Fragment frag = mSectionsPagerAdapter.getFragmentAt(convo);
        final int fragPos = frag.getArguments().getInt("section_number");
        try {
            final String msg = msgBox.getText().toString();
            Networking.execute(new NetCallBack<Void, String>() {
                @Override
                public Void callPre() {
                    frag.getView().findViewById(R.id.frag_convo_layout).requestFocus();
                    sndProgBar.setVisibility(View.VISIBLE);
                    msgBox.requestFocus();
                    sendingMsg = true;
                    messages[fragPos][1].add(msg);
                    messages[fragPos][3].add("1");
                    ListView msgList = (ListView)frag.getView().findViewById(R.id.messageList);
                    msgList.setAdapter(new ArrayAdapter<String>(Globals.context, android.R.layout.simple_list_item_1, messages[fragPos][1]));
                    msgList.getAdapter();
                    msgList.smoothScrollToPosition(messages[fragPos][0].size());
                    return null;
                }

                @Override
                public Void callPost(String result) {
                    sndProgBar.setVisibility(View.GONE);
                    sendingMsg = false;
                    if (!msgBox.getText().toString().isEmpty())
                        sendBtn.setVisibility(View.VISIBLE);
                    if (result==null){
                        messages[fragPos][1].remove(messages[fragPos][0].size());
                        messages[fragPos][3].remove(messages[fragPos][0].size());
                        ListView msgList = (ListView)frag.getView().findViewById(R.id.messageList);
                        msgList.setAdapter(new ArrayAdapter<String>(Globals.context, android.R.layout.simple_list_item_1, messages[fragPos][1]));
                        msgList.getAdapter();
                        msgList.smoothScrollToPosition(messages[fragPos][1].size());
                        toast = Toast.makeText(Globals.context, "Could not send", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return null;
                }
            }, "http://m.chatfle.com/send_message.php", "hash", Globals.hash, "convo_id", conversations[0][convo], "message", msg);
            msgBox.setText("");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_ONE = "section_number";
        public static PlaceholderFragment newInstance(int sectionNumber) {
            System.out.println("newInstance "+sectionNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_ONE, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        public PlaceholderFragment() {  /* keep me empty, thanks*/   }
        private ListView msgList;
        private ProgressBar progBar;
        private int convPos;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            convPos = getArguments().getInt(ARG_ONE);
            System.out.println("oncreateview "+getArguments().getInt(ARG_ONE));
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
        return super.onNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        System.out.println("timer cancelled");
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(newMsgTimer, 500, 4000);
        System.out.println("timer scheduled");
    }

    @Override
    public void finish() {
        newMsgTimer.cancel();
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
