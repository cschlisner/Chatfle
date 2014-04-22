package com.Group2.chatfle.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    static Message tmpMsg = new Message(); // for checking & retrieving new messages
    EditText msgBox;
    ImageButton sendBtn;
    ProgressBar sndProgBar;
    static TimerTask newMsgTimer;
    static Timer timer = new Timer();
    static int currentConvo, gettingMsgs;
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
                checkNewMessage(mSectionsPagerAdapter.getFragmentAt((currentConvo>0)?currentConvo-1:currentConvo));
            }
            }
        };
    }

    class pageChange implements ViewPager.OnPageChangeListener {
        public pageChange(){}
        @Override
        public void onPageSelected(int position) {
            setTitle(conversations[position].display_name);
            //TODO: figure out how to detect keyboard state
//            if (((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).isAcceptingText())
//                Toast.makeText(Globals.context, mSectionsPagerAdapter.getPageTitle(position), Toast.LENGTH_SHORT).show();
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
            if (position==conversations.length-1) {
                stopChecking = false;
                timer = new Timer();
                timer.schedule(newMsgTimer, 500, 4000);
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
        final int position = frag.getArguments().getInt("section_number");
        System.out.println("Getting Messages for convo "+position);
        final ListView msgList = (lv==null)?((ListView) frag.getView().findViewById(R.id.messageList)):lv;
        final String convoId = conversations[position].convo_id;
        if (gettingMsgs < 4) {
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
                    try {
                        JSONObject jso = new JSONObject(result);
                        JSONArray messages = jso.getJSONArray("messages");
                        conversations[position].msgList.clear();
                        for (int i = 0; i < messages.length(); ++i) {
                            JSONObject o = messages.getJSONObject(i);
                            tmpMsg = new Message();
                            tmpMsg.display_name = o.getString("display_name");
                            tmpMsg.msg = o.getString("msg");
                            tmpMsg.timestamp = o.getString("timestamp");
                            tmpMsg.msg_sender = o.getString("msg_sender");
                            conversations[position].msgList.add(tmpMsg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[position].msgList));
                    msgList.getAdapter();
                    return null;
                }
            }, "http://m.chatfle.com/get_messages.php", "hash", Globals.hash, "convo_id", convoId);
        }
    }

    public static void checkNewMessage(final Fragment frag){
        final int position = frag.getArguments().getInt("section_number");
        final String convoId = conversations[position].convo_id;
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {
                System.out.println("checking new messages "+position);
                return null;
            }
            @Override
            public Void callPost(String result) {
                if (conversations[position].msgList.size() > 0) {
                    try {
                        JSONObject jso = new JSONObject(result);
                        JSONArray convos = jso.getJSONArray("messages");
                        JSONObject o = convos.getJSONObject(0);
                        //tmpMsg = new Message();
                        tmpMsg.timestamp = o.getString("timestamp");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!tmpMsg.timestamp.equals(conversations[position].msgList.get(conversations[position].msgList.size()-1).timestamp))
                        getMessages(mSectionsPagerAdapter.getFragmentAt(position), null, null);
                    if (position<=currentConvo+1 && !stopChecking && !sendingMsg && mSectionsPagerAdapter.getFragmentAt(position+1)!=null)
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
                    tmpMsg = new Message();
                    tmpMsg.msg = msg;
                    tmpMsg.msg_sender = "1";
                    tmpMsg.timestamp = "Just Now";
                    conversations[fragPos].msgList.add(tmpMsg);
                    ListView msgList = (ListView)frag.getView().findViewById(R.id.messageList);
                    msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[fragPos].msgList));
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
                    if (result==null){
                        System.out.print("nope");
                        conversations[fragPos].msgList.remove(conversations[fragPos].msgList.size());
                        ListView msgList = (ListView)frag.getView().findViewById(R.id.messageList);
                        msgList.setAdapter(new MessageAdapter(Globals.context, android.R.layout.simple_list_item_1, conversations[fragPos].msgList));
                        msgList.getAdapter();
                        msgList.smoothScrollToPosition(conversations[fragPos].msgList.size());
                        toast = Toast.makeText(Globals.context, "Could not send", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return null;
                }
            }, "http://m.chatfle.com/send_message.php", "hash", Globals.hash, "convo_id", conversations[convo].convo_id, "message", msg);
            msgBox.setText("");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_ONE = "section_number";
        public static PlaceholderFragment newInstance(int sectionNumber) {
            System.out.println("initializing fragment "+sectionNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_ONE, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        public PlaceholderFragment() {  /* keep me empty, thanks*/   }
        private ListView msgList;
        private ProgressBar progBar;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            System.out.println("onCreateVIew  "+getArguments().getInt(ARG_ONE));
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
        timer.purge();
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
