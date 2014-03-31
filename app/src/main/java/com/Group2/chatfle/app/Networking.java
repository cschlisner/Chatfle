package com.Group2.chatfle.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cole on 3/18/14.
 */
public class Networking extends AsyncTask<String, Void, Boolean> {
    public boolean sucess;
    public HttpResponse response;
    @Override
    protected void onPreExecute() {
        //Bleh
    }
    @Override
    protected Boolean doInBackground(String... params){
        this.response = sendData(params);
        return true;
    }

    protected HttpResponse sendData(String... params){
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(params[0]);

        try {
            // Add your data
            int index = params.length;
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(index);
            nameValuePairs.add(new BasicNameValuePair("command", params[0]));
            for (int i=1; i<index; ++i) {
                nameValuePairs.add(new BasicNameValuePair("flag "+i, params[i]));
            }
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
        this.sucess = success;
    }
}