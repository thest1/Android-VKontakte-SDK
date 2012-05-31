package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Video implements Serializable{
    private static final long serialVersionUID = 1L;
    public long vid;
    public long owner_id;
    public String title;
    public String description;
    public long duration;
    public String link;
    public String image;
    public long date;
    public String player;

    public static Video parse(JSONObject o) throws NumberFormatException, JSONException{
        Video v = new Video();
        if(o.has("vid"))
            v.vid = o.getLong("vid");
        if(o.has("id"))//video.getUserVideos
            v.vid = Long.parseLong(o.getString("id"));
        v.owner_id = o.getLong("owner_id");
        v.title = Api.unescape(o.getString("title"));
        v.duration = o.getLong("duration");
        v.description = Api.unescape(o.optString("description"));
        if(o.has("image"))
            v.image = o.optString("image");
        if(o.has("thumb"))//video.getUserVideos
            v.image = o.optString("thumb");
        v.link = o.optString("link");
        v.date = o.optLong("date");
        v.player = o.optString("player");
        return v;
    }
}
