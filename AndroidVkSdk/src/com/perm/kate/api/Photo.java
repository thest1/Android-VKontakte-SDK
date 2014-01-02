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
    public String src;//photo_130
    public String src_small;//photo_75
    public String src_big;//photo_604
    public String src_xbig;//photo_807
    public String src_xxbig;//photo_1280
    public String src_xxxbig;//photo_2560
    public String phototext;
    public long created;
    public Integer like_count;
    public Boolean user_likes;
    public Integer comments_count;
    public Integer tags_count;
    public Boolean can_comment;
    public int width;//0 means value is unknown
    public int height;//0 means value is unknown
    public String access_key;
    public String user_id; //for group

    public static Photo parse(JSONObject o) throws NumberFormatException, JSONException{
        Photo p = new Photo();
        p.pid = o.getLong("id");
        p.aid = o.optLong("album_id");
        p.owner_id = o.getString("owner_id");
        p.src = o.optString("photo_130");
        p.src_small = o.optString("photo_75");
        p.src_big = o.optString("photo_604");
        p.src_xbig = o.optString("photo_807");
        p.src_xxbig = o.optString("photo_1280");
        p.src_xxxbig = o.optString("photo_2560");
        p.phototext = Api.unescape(o.optString("text"));
        p.created = o.optLong("date"); //date instead created for api v 5.0 and higher
        p.user_id = o.optString("user_id");
        
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
        p.access_key=o.optString("access_key");
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
        JSONArray user_id_array = o.optJSONArray("user_id");
        if (user_id_array != null && user_id_array.length() > 0) {
            p.user_id = user_id_array.getString(0);
        }
        return p;
    }
}
