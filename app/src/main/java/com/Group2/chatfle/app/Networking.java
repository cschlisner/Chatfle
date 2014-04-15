package com.Group2.chatfle.app;

import android.os.AsyncTask;

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
    public Networking(){
    }

    public static void execute(final NetCallBack<Void, String> netMethod, String... params) {
        class Net extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                netMethod.callPre();
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
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.length);
                    for (int i=2; i<params.length-1; i+=2)
                        nameValuePairs.add(new BasicNameValuePair(params[i-1], params[i]));
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
                netMethod.callPost(result);
            }
        }
        new Net().execute(params);
    }
}