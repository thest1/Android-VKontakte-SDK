package com.perm.kate.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AudioAlbum {
    public long album_id;
    public long owner_id;
    public String title;

    public static AudioAlbum parse(JSONObject o) throws JSONException {
        AudioAlbum a = new AudioAlbum();
        a.title = Api.unescape(o.optString("title"));
        a.album_id = o.getLong("album_id");
        a.owner_id = o.optLong("owner_id");
        return a;
    }
    
    public static ArrayList<AudioAlbum> parseAlbums(JSONArray array) throws JSONException {
        ArrayList<AudioAlbum> albums = new ArrayList<AudioAlbum>();
        if (array == null) 
            return albums;
        int category_count = array.length();
        for(int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject)array.get(i);
            AudioAlbum m = AudioAlbum.parse(o);
            albums.add(m);
        }
        return albums;
    }
}