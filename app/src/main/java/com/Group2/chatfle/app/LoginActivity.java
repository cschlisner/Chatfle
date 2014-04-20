package com.Group2.chatfle.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
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

public class LoginActivity extends ActionBarActivity {
    EditText usrEmail, usrPswd;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String h = prefs.getString("CREDENTIALS", "");
        if (!h.isEmpty()) {
            Globals.hash = h;
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
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
        return false;
    }

    public void sendCreds(View v){
        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        if (!usrEmail.getText().toString().isEmpty() || !usrPswd.getText().toString().isEmpty()){
            String user = usrEmail.getText().toString();
            String pass = usrPswd.getText().toString();
            String request = "http://m.chatfle.com/";
            Networking.execute(new NetCallBack<Void, String>() {
                @Override
                public Void callPre() {
                    dialog.setMessage("Please wait");
                    dialog.show();
                    return null;
                }

                @Override
                public Void callPost(String result) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (result != null) {
                        Globals.hash = result;
                        SharedPreferences.Editor e = prefs.edit();
                        e.putString("CREDENTIALS", result);
                        e.commit();
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
                    return null;
                }
            }, request, "username", user, "password", pass);

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
