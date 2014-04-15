package com.Group2.chatfle.app;

/**
 * Created by Cole on 4/14/14.
 */
public abstract class NetCallBack<Void, String> {
    public abstract Void callPre();
    public abstract Void callPost(String result);
}
