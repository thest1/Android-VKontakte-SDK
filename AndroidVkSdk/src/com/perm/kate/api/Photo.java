package com.perm.kate.api;

import java.io.Serializable;

import org.json.JSONArray;
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
    public Integer tags_count;
    public Boolean can_comment;
    public int width;//0 means value is unknown
    public int height;//0 means value is unknown

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
        
        if (o.has("likes")) {
            JSONObject jlikes = o.getJSONObject("likes");
            p.like_count = jlikes.optInt("count");
            p.user_likes = jlikes.optInt("user_likes")==1;
        }
        if (o.has("comments")) {
            JSONObject jcomments = o.getJSONObject("comments");
            p.comments_count = jcomments.optInt("count");
        }
        if (o.has("tags")) {
            JSONObject jtags = o.getJSONObject("tags");
            p.tags_count = jtags.optInt("count");
        }
        if (o.has("can_comment"))
            p.can_comment = o.optInt("can_comment")==1;
        p.width = o.optInt("width");
        p.height = o.optInt("height");
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

    public static Photo parseCounts(JSONObject o) throws NumberFormatException, JSONException{
        Photo p = new Photo();
        JSONArray pid_array = o.optJSONArray("pid");
        if (pid_array != null && pid_array.length() > 0) {
            p.pid = pid_array.getLong(0);
        }
        JSONArray likes_array = o.optJSONArray("likes");
        if (likes_array != null && likes_array.length() > 0) {
            JSONObject jlikes = likes_array.getJSONObject(0);
            p.like_count = jlikes.optInt("count");
            p.user_likes = jlikes.optInt("user_likes")==1;
        }
        JSONArray comments_array = o.optJSONArray("comments");
        if (comments_array != null && comments_array.length() > 0) {
            JSONObject jcomments = comments_array.getJSONObject(0);
            p.comments_count = jcomments.optInt("count");
        }
        JSONArray tags_array = o.optJSONArray("tags");
        if (tags_array != null && tags_array.length() > 0) {
            JSONObject jtags = tags_array.getJSONObject(0);
            p.tags_count = jtags.optInt("count");
        }
        JSONArray can_comment_array = o.optJSONArray("can_comment");
        if (can_comment_array != null && can_comment_array.length() > 0) {
            p.can_comment = can_comment_array.getInt(0)==1;
        }
        return p;
    }
}
