package com.Group2.chatfle.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends ActionBarActivity {

    DrawerLayout drawerLayout;
    ListView drawerList, convList;
    ProgressBar loadSpinner;
    Context context;
    Button retryButton;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    private ContentFragment convFrag;
    static private String[] drawerItems = new String[]{"Conversations", "Settings", "Logout"}; //placeholder
    static private Conversation[] conversations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        FlatUI.setDefaultTheme(FlatUI.DARK);
        FlatUI.setActionBarTheme(this, FlatUI.DARK, true, true);
        //Preload Conversations
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
        createListFrag();
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
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshConvos();
                return true;
            default:
                return drawerToggle.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (drawerItems[position].equals("Settings")) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
            else if (drawerItems[position].equals("Logout")){
                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                e.putString("CREDENTIALS", "");
                e.commit();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
            else {
                createListFrag();
            }
            drawerLayout.closeDrawer(drawerList);

        }
    }

    private void createListFrag(){
        // update the main content by replacing fragments
        ContentFragment fragment = new ContentFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        convFrag = fragment;
        // update selected item and title, then close the drawer
        drawerList.setItemChecked(0, true);
        setTitle(drawerItems[0]);
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void refreshConvos(){
        retryButton.setVisibility(View.GONE);
        Globals.context = this;
        Networking.execute(new NetCallBack<Void, String>() {
            @Override
            public Void callPre() {
                loadSpinner.setVisibility(View.VISIBLE);
                return null;
            }

            @Override
            public Void callPost(String result) {
                loadSpinner.setVisibility(View.GONE);
                if (result != null) {
                    setArrays(result);
                }
                else {
                    Toast.makeText(Globals.context, "No response from server", Toast.LENGTH_SHORT).show();
                    retryButton.setVisibility(View.VISIBLE);
                }
                return null;
            }
        }, "http://m.chatfle.com/get_convos.php", "hash", Globals.hash);
    }

    private void setArrays(String response){
        try {
            JSONObject jso = new JSONObject(response);
            JSONArray convos = jso.getJSONArray("convos");
            conversations = new Conversation[convos.length()];
            for (int i=0; i<conversations.length; ++i)
                conversations[i] = new Conversation();
            for (int i = 0; i<convos.length(); ++i) {
                JSONObject o = convos.getJSONObject(i);
                conversations[i].convo_id = o.getString("convo_id");
                conversations[i].display_name = o.getString("display_name");
                conversations[i].msg_preview = o.getString("msg_preview");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        convFrag.createList();
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public class ContentFragment extends Fragment {
        public ContentFragment() {
            // Empty constructor required for fragment subclasses
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_conversationlist, container, false);
            loadSpinner = (ProgressBar) rootView.findViewById(R.id.loadingImg);
            retryButton = (Button) rootView.findViewById(R.id.retry_button);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refreshConvos();
                }
            });
            getActivity().setTitle("Conversations");
            refreshConvos();
            return rootView;
        }

        public void createList(){
            try {
                convList = (ListView) getView().findViewById(R.id.conversation_list);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //TODO: make conversations list adapter
            final ArrayList<String>[] convData = new ArrayList[3];
            for (int i=0; i<convData.length; ++i)
                convData[i] = new ArrayList<String>();
            for (Conversation c : conversations){
                convData[0].add(c.convo_id);
                convData[1].add(c.display_name);
                convData[2].add(c.msg_preview);
            }

            convList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, convData[1]));
            convList.getAdapter();
            convList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(context, ConversationActivity.class);
                    intent.putExtra("CONVID", convData[0]);
                    intent.putExtra("DISPNAME", convData[1]);
                    intent.putExtra("MSGPREV", convData[2]);
                    intent.putExtra("POSITION", i);
                    startActivity(intent);
                    convList.setItemChecked(i, true);
                }
            });
        }
    }
}