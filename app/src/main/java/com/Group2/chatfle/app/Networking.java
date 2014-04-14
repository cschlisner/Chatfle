package com.Group2.chatfle.app;

import android.app.ProgressDialog;
import android.content.Entity;
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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cole on 3/18/14.
 */
public class Networking {
    private HttpResponse response;
    private String[] params;

    public Networking(String... params) {
        this.params = new String[params.length];
        int j =0;
        for (String i : params)
            this.params[j++] = i;
    }

    public HttpResponse exec(){
        return response;
    }

    public void onSuccess(HttpResponse result){
        try {
            System.out.println("response is "+EntityUtils.toString(result.getEntity())+" @ Networking.onSuccess()");
        } catch (IOException e) {
            e.printStackTrace();
        }
        response = result;
    }


}