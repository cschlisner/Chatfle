package com.Group2.chatfle.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cengalabs.flatui.FlatUI;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.sql.Time;
import java.util.Date;

public class LoginActivity extends ActionBarActivity {
    EditText usrEmail, usrPswd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView title = (TextView) findViewById(R.id.title_view);
        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf"));
        usrEmail = (EditText) findViewById(R.id.email_text);
        usrPswd = (EditText) findViewById(R.id.password_text);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();
        FlatUI.setDefaultTheme(FlatUI.DEEP);
        FlatUI.setActionBarTheme(this, FlatUI.DEEP, true, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void sendCreds(View v){
        if (!usrEmail.getText().toString().isEmpty() || !usrPswd.getText().toString().isEmpty()){
            String dirtyInput = usrEmail.getText().toString()+usrPswd.getText().toString()+"salt";
            String creds = md5(dirtyInput);
            String urlParameters = "hash="+creds;
            String request = "http://example.com/index.php";
            Networking n = new Networking();
            n.execute(request, urlParameters);
        }
        else {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    public static String md5(String input) {
        String md5 = null;
        if(null == input) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(input.getBytes(), 0, input.length());
            md5 = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    private class Networking extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params){
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(params[1].getBytes().length));
                connection.setUseCaches (false);

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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
