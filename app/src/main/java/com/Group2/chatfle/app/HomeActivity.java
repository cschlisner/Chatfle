package com.Group2.chatfle.app;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cengalabs.flatui.FlatUI;

public class HomeActivity extends ActionBarActivity {

    DrawerLayout drawerLayout;
    ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    private String[] s = new String[]{"Item 1", "Item 2"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FlatUI.setDefaultTheme(FlatUI.DEEP);
        FlatUI.setActionBarTheme(this, FlatUI.DEEP, true, false);
        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.id.list_item, s));
        //drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

//        drawerToggle = new ActionBarDrawerToggle(
//                this,                  /* host Activity */
//                drawerLayout,         /* DrawerLayout object */
//                //R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
//                //R.string.drawer_open,  /* "open drawer" description for accessibility */
//                //R.string.drawer_close  /* "close drawer" description for accessibility */
//        ) {
//            public void onDrawerClosed(View view) {
//                getActionBar().setTitle(title);
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(drawerTitle);
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//        };
        drawerLayout.setDrawerListener(drawerToggle);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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
