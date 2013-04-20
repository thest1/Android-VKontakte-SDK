package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Country {
    public long cid;
    public String name;
    public String title;

    public static Country parse(JSONObject o) throws NumberFormatException, JSONException{
        Country c = new Country();
        c.cid = Long.parseLong(o.getString("cid"));
        c.name = o.optString("name");
        c.title = o.optString("title");
        return c;
    }
}
