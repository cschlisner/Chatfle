package com.Group2.chatfle.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;

import java.util.Locale;

public class HomeActivity extends ActionBarActivity {

    DrawerLayout drawerLayout;
    ListView drawerList;
    ImageView img;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    static private String[] drawerItems = new String[]{"Conversations", "Something", "Settings"}; //placeholder
    static private String[] conversations = new String[]{"ONE", "TWO", "THREE"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FlatUI.setDefaultTheme(FlatUI.DARK);
        FlatUI.setActionBarTheme(this, FlatUI.DARK, true, true);
        img = (ImageView) findViewById(R.id.imageView);
//        img.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        //Preload Conversations

        ListView convList = (ListView) findViewById(R.id.conversation_list);
        convList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conversations));
        convList.getAdapter();
        convList.setOnItemClickListener(new DrawerItemClickListener());
        //Drawer
        title = drawerTitle = "Chatfle";
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems));
        drawerList.getAdapter();
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return (drawerToggle.onOptionsItemSelected(item));
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        if (drawerItems[position].equals("Settings")) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else {
            // update the main content by replacing fragments
            Fragment fragment = new ContentFragment();
            Bundle args = new Bundle();
            args.putInt(ContentFragment.ARG_CONVO_NUMBER, position);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            // update selected item and title, then close the drawer
            drawerList.setItemChecked(position, true);
            setTitle(drawerItems[position]);
        }
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getActionBar().setTitle(this.title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class ContentFragment extends Fragment {
        public static final String ARG_CONVO_NUMBER = "convo_number";
        public static final String ARG_LIST_SAUCE = "list_sauce";

        public ContentFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            int layout, i=0;
            //String source = getArguments().getString(ARG_LIST_SAUCE);
            try {
                i = (getArguments().isEmpty()) ? 0 : getArguments().getInt(ARG_CONVO_NUMBER);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            String itemText = drawerItems[i];
            if (i==0) {
                layout = R.layout.fragment_conversation;
            }
            else {
                layout = R.layout.other_fragment;
            }
            View rootView = inflater.inflate(layout, container, false);

            //Data collection --PUT THIS IN THE CONVERSATIONS OPTION FRAGMENT--
//            Networking getConvos = new Networking();
//            getConvos.execute("Get Conversation List");
//            if (getConvos.getStatus()== AsyncTask.Status.FINISHED && getConvos.sucess){
//                //Handle Http Response and JSON interpretation
//            }

            getActivity().setTitle(itemText);
            if (itemText.equals("Home")){
                //Figure out how to switch xml layouts dynamically
            }
            return rootView;
        }
    }
}