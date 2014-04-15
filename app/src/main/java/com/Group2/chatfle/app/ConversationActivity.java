package com.Group2.chatfle.app;

import java.util.Locale;

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
import android.widget.TextView;


public class ConversationActivity extends Activity{

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    static String[][] conversations;
    static String[][] messages;
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
        }
        @Override
        public void onPageScrollStateChanged(int i){

        }
        @Override
        public void onPageScrolled(int a, float b, int c){

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

    

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
            // Show 3 total pages.
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
            args.putString(ARG_ONE, conversations[1][sectionNumber]);
            fragment.setArguments(args);
            return fragment;
        }
        public PlaceholderFragment() {       }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_convo, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getArguments().getString(ARG_ONE));
            return rootView;
        }
    }

}
