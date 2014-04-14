package com.Group2.chatfle.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
    static private String[][] conversations;
    static private String[] convNames;
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
                // update the main content by replacing fragments
                ContentFragment fragment = new ContentFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                convFrag = fragment;
                // update selected item and title, then close the drawer
                drawerList.setItemChecked(position, true);
                setTitle(drawerItems[position]);
            }
            drawerLayout.closeDrawer(drawerList);

        }
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
//        if (inConvo){
//            drawerList.setSelection(0);
//            drawerList.getAdapter().getView(0, null, null).performClick();
//
//        }
    }

    private void refreshConvos(){
        retryButton.setVisibility(View.GONE);
        System.out.println("creds: "+ Globals.hash);
        Globals.context = this;
        new Net().execute("http://m.chatfle.com/get_convos.php", Globals.hash);
        Thread.yield();
    }

    private void setArrays(String response){
        System.out.println("response: "+response);
        try {
            JSONObject jso = new JSONObject(response);
            JSONArray convos = jso.getJSONArray("convos");
            conversations = new String[3][convos.length()];
            for (int i = 0; i<convos.length(); ++i) {
                System.out.print(".");
                JSONObject o = convos.getJSONObject(i);
                conversations[0][i] = o.getString("convo_id");
                conversations[1][i] = o.getString("display_name");
                conversations[2][i] = o.getString("msg_preview");
            }
            convNames = new String[conversations[1].length];
            System.arraycopy(conversations[1], 0, convNames, 0, conversations[1].length);
            System.out.println(conversations[0][0] + " " + conversations[1][0] + " " + conversations[2][0]);
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
            View rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
            loadSpinner = (ProgressBar) rootView.findViewById(R.id.loadingImg);
            retryButton = (Button) rootView.findViewById(R.id.retry_button);
            refreshConvos();
            getActivity().setTitle("Conversations");
            return rootView;
        }

        public void createList(){
            try {
                convList = (ListView) getView().findViewById(R.id.conversation_list);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            convList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, convNames));
            convList.getAdapter();
            convList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Fragment fragment = new ConversationFragment();
                    Bundle args = new Bundle();
                    args.putInt(ConversationFragment.ARG_CONVO_NUMBER, i);
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getFragmentManager();
                    try {
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // update selected item and title, then close the drawer
                    convList.setItemChecked(i, true);
                    setTitle(convNames[i]);
                }
            });
        }
    }

    public static class ConversationFragment extends Fragment {
        public static final String ARG_CONVO_NUMBER = "convo_number";

        public ConversationFragment() {
            // Empty constructor required for fragment subclasses
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            //String source = getArguments().getString(ARG_LIST_SAUCE);
            int i = getArguments().getInt(ARG_CONVO_NUMBER);
            String itemText = convNames[i];
            View rootView = inflater.inflate(R.layout.other_fragment, container, false);
            getActivity().setTitle(itemText);
            return rootView;
        }
    }

    private class Net extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog = new ProgressDialog(Globals.context);
        @Override
        protected void onPreExecute() {
            loadSpinner.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... params){
            try {
                return EntityUtils.toString(getResponse(params).getEntity());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private HttpResponse getResponse (String... params){
            System.out.print("Sending data: ");
            for (String i : params)
                System.out.print(i+" ");
            System.out.println("");
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);
            try {
                // Add your data
                int index = params.length;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("hash", params[1]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                return httpclient.execute(httppost);


            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return null;
        }

        protected void onPostExecute(String result) {
            loadSpinner.setVisibility(View.GONE);
            if (result != null) {
                setArrays(result);
            }
            else {
                Toast.makeText(Globals.context, "could not get response", Toast.LENGTH_SHORT).show();
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }
}