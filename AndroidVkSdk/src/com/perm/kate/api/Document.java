package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Document {
    public String title;
    public String url;
    public long size;
    
    public static Document parse(JSONObject o) throws JSONException {
        Document d = new Document();
        d.title = o.getString("title");
        d.url = o.getString("url");
        d.size = o.getLong("size");
        return d;
    }

}
