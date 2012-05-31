package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;
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
