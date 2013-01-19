package com.perm.kate.api;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    public long pid;
    public long aid;
    public String owner_id;
    public String src;
    public String src_small;//not used for now because in newsfeed it's empty
    public String src_big;
    public String src_xbig;
    public String src_xxbig;
    public String src_xxxbig;
    public String phototext;
    public long created;
    public Integer like_count;
    public Boolean user_likes;
    public Integer comments_count;

    public static Photo parse(JSONObject o) throws NumberFormatException, JSONException{
        Photo p = new Photo();
        p.pid = o.getLong("pid");
        p.aid = o.optLong("aid");
        p.owner_id = o.getString("owner_id");
        p.src = o.optString("src");
        p.src_small = o.optString("src_small");
        p.src_big = o.optString("src_big");
        p.src_xbig = o.optString("src_xbig");
        p.src_xxbig = o.optString("src_xxbig");
        p.src_xxxbig = o.optString("src_xxxbig");
        p.phototext = Api.unescape(o.optString("text"));
        p.created = o.optLong("created");
        
        if (o.has("likes")){
            JSONObject jlikes = o.getJSONObject("likes");
            p.like_count = jlikes.optInt("count");
            p.user_likes = jlikes.optInt("user_likes")==1;
        }
        if (o.has("comments")){
            JSONObject jcomments = o.getJSONObject("comments");
            p.comments_count = jcomments.optInt("count");
        }
        return p;
    }

    public Photo(){
    }

    public Photo(Long id, String owner_id, String src, String src_big){
        this.pid=id;
        this.owner_id=owner_id;
        this.src=src;
        this.src_big=src_big;
    }
}
