package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Link implements Serializable {
    private static final long serialVersionUID = 1L;
    public String url; 
    public String title; 
    public String description; 
    public String image_src;

    public static Link parse(JSONObject o) throws NumberFormatException, JSONException{
        Link link = new Link();
        link.url = o.optString("url");
        link.title = Api.unescape(o.optString("title"));
        link.description = Api.unescape(o.optString("description"));
        link.image_src = o.optString("image_src");
        return link;
    }
    
    public static Link parseFromGroup(JSONObject o) throws NumberFormatException, JSONException {
        Link link = new Link();
        link.url = o.optString("url");
        link.title = Api.unescape(o.optString("name"));
        link.description = Api.unescape(o.optString("desc"));
        link.image_src = o.optString("photo_100");
        return link;
    }
}
