package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class LastActivity {
    public boolean online;
    public Long last_seen=null;
    
    public static LastActivity parse(JSONObject o) throws JSONException {
        LastActivity u = new LastActivity();
        u.online = o.optInt("online")==1;
        u.last_seen = o.optLong("time");
        return u;
    }
}
