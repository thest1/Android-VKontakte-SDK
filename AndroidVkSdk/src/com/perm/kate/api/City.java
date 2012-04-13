package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class City {
    public long cid;
    public String name;

    public static City parse(JSONObject o) throws NumberFormatException, JSONException{
        City c = new City();
        c.cid = Long.parseLong(o.getString("cid"));
        c.name = o.getString("name");
        return c;
    }
}
