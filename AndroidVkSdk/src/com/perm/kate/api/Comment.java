package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
    public long cid;
    public long from_id;
    public long date;
    public String message;
    public long reply_to_uid;
    public long reply_to_cid;

    //likes
    public int like_count;
    public boolean user_like;
    public boolean can_like;

    public static Comment parse(JSONObject o) throws NumberFormatException, JSONException{
        Comment comment=new Comment();
        comment.cid = Long.parseLong(o.getString("cid"));
        comment.from_id = Long.parseLong(o.getString("uid"));
        comment.date = Long.parseLong(o.getString("date"));
        comment.message = Api.unescape(o.getString("text"));
        String reply_to_uid = o.optString("reply_to_uid");
        if (reply_to_uid != null && !reply_to_uid.equals(""))
            comment.reply_to_uid = Long.parseLong(reply_to_uid);
        String reply_to_cid = o.optString("reply_to_cid");
        if (reply_to_cid != null && !reply_to_cid.equals(""))
            comment.reply_to_cid = Long.parseLong(reply_to_cid);
        if (o.has("likes")){
            JSONObject jlikes = o.getJSONObject("likes");
            comment.like_count = jlikes.getInt("count");
            comment.user_like = jlikes.getInt("user_likes")==1;
            comment.can_like = jlikes.getInt("can_like")==1;
        }
        return comment;
    }

    //for group topic comments 
    public static Comment parseTopicComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = Long.parseLong(o.getString("id"));
        comment.from_id = Long.parseLong(o.getString("from_id"));
        comment.date = Long.parseLong(o.getString("date"));
        comment.message = Api.unescape(o.getString("text"));
        return comment;
    }
     
    public static Comment parseNoteComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = Long.parseLong(o.getString("id"));
        comment.from_id = Long.parseLong(o.getString("uid"));
        comment.date = Long.parseLong(o.getString("date"));
        comment.message = Api.unescape(o.getString("message"));
        return comment;
    }
    
    public static Comment parseVideoComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = o.getLong("id");
        comment.from_id = o.getLong("from_id");
        comment.date = o.getLong("date");
        comment.message = Api.unescape(o.getString("message"));
        return comment;
    }
    
    public static Comment parsePhotoComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = o.getLong("cid");
        comment.from_id = o.getLong("from_id");
        comment.date = o.getLong("date");
        comment.message = Api.unescape(o.getString("message"));
        return comment;
    }
}
