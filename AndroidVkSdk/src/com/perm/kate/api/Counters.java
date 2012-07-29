package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Counters {
    public int friends;
    public int messages;
    
    public static Counters parse(JSONObject o) throws JSONException {
        Counters a = new Counters();
        if(o==null)
            return a;
        a.friends = o.optInt("friends", 0);
        a.messages = o.optInt("messages", 0);
        return a;
    }

}
