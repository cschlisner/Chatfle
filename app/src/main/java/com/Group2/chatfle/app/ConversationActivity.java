package com.Group2.chatfle.app;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ConversationActivity extends Activity{

    static SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    static String[][] conversations;
    static String[][][] messages;
    int currentConvo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);


        Intent intent = getIntent();
        conversations = new String[3][intent.getStringArrayExtra("CONVID").length];
        conversations[0] = intent.getStringArrayExtra("CONVID");
        conversations[1] = intent.getStringArrayExtra("DISPNAME");
        conversations[2] = intent.getStringArrayExtra("MSGPREV");
        currentConvo = intent.getIntExtra("POSITION", 0);
        messages = new String[(conversations[0].length>5)?5:conversations[0].length][][];
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new pageChange());
        mViewPager.setCurrentItem(currentConvo);
        setTitle(conversations[1][currentConvo]);
    }

    class pageChange implements ViewPager.OnPageChangeListener {
        public pageChange(){}
        @Override
        public void onPageSelected(int position) {
            setTitle(conversations[1][position]);
            System.out.println("in convo: " + position);
        }
        @Override
        public void onPageScrollStateChanged(int i){

        }
        @Override
        public void onPageScrolled(int a, float b, int c){

        }
    }
    private static void getMessages(final int position, final ListView msgList, final ProgressBar loadSpinner){
        final String convoId = conversations[0][position];
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {
                loadSpinner.setVisibility(View.VISIBLE);
                return null;
            }

            @Override
            public Void callPost(String result) {
                loadSpinner.setVisibility(View.GONE);
                // handle JSON
                try {
                    JSONObject jso = new JSONObject(result);
                    JSONArray convos = jso.getJSONArray("messages");
                    messages[0] = new String[3][convos.length()];
                    for (int i = 0; i<convos.length(); ++i) {
                        JSONObject o = convos.getJSONObject(i);
                        messages[0][0][i] = o.getString("display_name");
                        messages[0][1][i] = o.getString("msg");
                        messages[0][2][i] = o.getString("timestamp");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msgList.setAdapter(new ArrayAdapter<String>(Globals.context, android.R.layout.simple_list_item_1, messages[0][1]));
                msgList.getAdapter();
                return null;
            }
        }, "http://m.chatfle.com/get_messages.php", "hash", Globals.hash, "convo_id", convoId);
    }

    private static void setArrays(String response, final Fragment frag){

    }

    

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PlaceholderFragment frag = PlaceholderFragment.newInstance(position);
            return frag;
        }

        @Override
        public int getCount() {
            return conversations[0].length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return conversations[1][position];
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_ONE = "section_number";
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_ONE, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        public PlaceholderFragment() {       }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            System.out.println("Created fragment "+getArguments().getInt(ARG_ONE));
            View rootView = inflater.inflate(R.layout.fragment_convo, container, false);
            ProgressBar loadSpinner = (ProgressBar) rootView.findViewById(R.id.loadingImg);
            ListView msgList = (ListView) rootView.findViewById(R.id.messageList);
            RelativeLayout myLayout = (RelativeLayout) rootView.findViewById(R.id.frag_convo_layout);
            myLayout.requestFocus();
            getMessages(getArguments().getInt(ARG_ONE), msgList, loadSpinner);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
