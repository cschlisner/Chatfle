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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
                HttpResponse response = getResponse(params);
                System.out.println("Networking::Response code: "+response.getStatusLine().getStatusCode());
                try {
                    return EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            private HttpResponse getResponse (String... params){
                System.out.print("Networking::Sending data: ");
                for (String i : params)
                    System.out.print(i+" ");
                System.out.println("");
                // Create a new HttpClient and Post Header
                final HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
                HttpClient httpclient = new DefaultHttpClient(httpParams);
                HttpPost httppost = new HttpPost(params[0]);
                try {
                    // Add your data
                    int index = params.length;
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.length);
                    for (int i=2; i<params.length; i+=2)
                        nameValuePairs.add(new BasicNameValuePair(params[i-1], params[i]));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    // Execute HTTP Post Request
                    return httpclient.execute(httppost);
                } catch (ClientProtocolException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
            }
            protected void onPostExecute(String result) {
                String print = (result.length()>40)?(result.substring(0, 40)+"..."):result;
                System.out.println("Networking::Result: "+print);
                netMethod.callPost((result.isEmpty())?null:result);
            }
        }
        new Net().execute(params);
    }
}