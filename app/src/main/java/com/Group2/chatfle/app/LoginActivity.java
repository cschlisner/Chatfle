package com.Group2.chatfle.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
        actionBar.hide();
        FlatUI.setDefaultTheme(FlatUI.DEEP);
        FlatUI.setActionBarTheme(this, FlatUI.DEEP, true, false);
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(FlatUI.DEEP, false));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void sendCreds(View v){
        if (!usrEmail.getText().toString().isEmpty() || !usrPswd.getText().toString().isEmpty()){
            String dirtyInput = usrEmail.getText().toString()+md5(usrPswd.getText().toString());
            String creds = md5(dirtyInput);
            String user = usrEmail.getText().toString();
            String pass = usrPswd.getText().toString();
            String request = "http://m.chatfle.com/";
            Networking n = new Networking();
            n.execute(request, pass, user);

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

    private class Networking extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }
        @Override
        protected Boolean doInBackground(String... params){
//            System.out.println(sendData(params).getStatusLine().getStatusCode());
//            try {
//                HttpEntity entity = sendData(params).getEntity();
//                System.out.println(EntityUtils.toString(entity));
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
            //return (sendData(params).getStatusLine().getStatusCode()==200);
            return true;
        }

        protected HttpResponse sendData(String... params){
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("username", params[2]));
                nameValuePairs.add(new BasicNameValuePair("password", params[1]));
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
        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (success) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
                usrPswd.setText("");
                usrEmail.setText("");
            }
            else {
                try {
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_SHORT);
                    toast.show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
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
