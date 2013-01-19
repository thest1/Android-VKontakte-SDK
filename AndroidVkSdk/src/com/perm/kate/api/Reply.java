package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Reply implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public long id;
    public long date;
    public String text;
    
    public static Reply parse(JSONObject o) throws JSONException {
        Reply r = new Reply();
        r.id = o.getLong("id");
        r.date = o.optLong("date");
        r.text = o.getString("text");
        return r;
    }
    
}
