package com.Group2.chatfle.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
                // Call the first callback method
                netMethod.callPre();
            }
            @Override
            protected String doInBackground(String... params){
                HttpResponse response = getResponse(params);
                //if (response!=null)
//                    System.out.println("Networking::Response code: "+response.getStatusLine().getStatusCode());
                try {
                     return EntityUtils.toString(response.getEntity());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
            private HttpResponse getResponse (String... params){
                ConnectivityManager connManager = (ConnectivityManager) Globals.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mData = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mWifi.isConnected() || mData.isConnected()) {
//                    System.out.print("Networking::Sending data: ");
//                    for (String i : params)
//                        System.out.print(i + " ");
//                    System.out.println("");
                    final HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
                    HttpClient httpclient = new DefaultHttpClient(httpParams);
                    HttpPost httppost = new HttpPost(params[0]);
                    try {
                        int index = params.length;
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.length);
                        for (int i = 2; i < params.length; i += 2)
                            nameValuePairs.add(new BasicNameValuePair(params[i - 1], params[i]));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        return httpclient.execute(httppost);
                    } catch (ClientProtocolException e) {
                        return null;
                    } catch (IOException e) {
                        return null;
                    }
                }
                return null;
            }
            protected void onPostExecute(String result) {
                if (result!=null&&!result.isEmpty()&&!result.equals("ERROR")) {
//                    String print = (result.length()>100)?(result.substring(0, 100)+"..."):result;
//                    System.out.println("Networking::Result: "+print);
                    netMethod.callPost(result);
                    return;
                }
                //call the second callback method
                netMethod.callPost(null);           // returns null response if response is empty, "ERROR", or null
            }
        }
        new Net().execute(params);
    }
}