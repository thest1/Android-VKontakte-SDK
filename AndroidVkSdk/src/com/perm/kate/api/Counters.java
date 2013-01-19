package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Counters {
    public int friends;
    public int messages;
    public int notifications;//new replies notifications
    
    public static Counters parse(JSONObject o) throws JSONException {
        Counters a = new Counters();
        if(o==null)
            return a;
        a.friends = o.optInt("friends");
        a.messages = o.optInt("messages");
        a.notifications = o.optInt("notifications");
        return a;
    }

}
