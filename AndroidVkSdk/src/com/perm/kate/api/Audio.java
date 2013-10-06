package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Audio implements Serializable {
    private static final long serialVersionUID = 1L;
    public long aid;
    public long owner_id;
    public String artist;
    public String title;
    public long duration;
    public String url;
    public Long lyrics_id;

    public static Audio parse(JSONObject o) throws NumberFormatException, JSONException{
        Audio audio = new Audio();
        audio.aid = Long.parseLong(o.getString("id"));
        audio.owner_id = Long.parseLong(o.getString("owner_id"));
        audio.artist = Api.unescape(o.optString("artist"));
        audio.title = Api.unescape(o.optString("title"));
        audio.duration = Long.parseLong(o.getString("duration"));
        audio.url = o.optString("url", null);
        
        String tmp=o.optString("lyrics_id");
        if(tmp!=null && !tmp.equals(""))//otherwise lyrics_id=null 
            audio.lyrics_id = Long.parseLong(tmp);
        return audio;
    }
}