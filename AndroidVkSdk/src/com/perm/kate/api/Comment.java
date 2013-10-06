package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    public long cid;
    public long from_id;
    public long date;
    public String message;
    public long reply_to_uid;
    public long reply_to_cid;
    public WallMessage post;//parent post, used only for notifications type "reply_comment". Can be moved to Notification class.
    public ArrayList<Attachment> attachments=new ArrayList<Attachment>();

    //likes
    public int like_count;
    public boolean user_like;
    public boolean can_like;

    public static Comment parse(JSONObject o) throws NumberFormatException, JSONException{
        Comment comment=new Comment();
        comment.cid = o.optLong("id");
        //в newsfeed.getComments комментарии приходят по-старому - баг в API
        if(!o.has("id") && o.has("cid"))
            comment.cid = o.optLong("cid");
        
        comment.from_id = o.optLong("from_id");
        comment.date = o.optLong("date");
        comment.message = Api.unescape(o.optString("text"));
        String reply_to_uid = o.optString("reply_to_user");
        if (reply_to_uid != null && !reply_to_uid.equals(""))
            comment.reply_to_uid = Long.parseLong(reply_to_uid);
        String reply_to_cid = o.optString("reply_to_comment");
        if (reply_to_cid != null && !reply_to_cid.equals(""))
            comment.reply_to_cid = Long.parseLong(reply_to_cid);
        parseLikes(o, comment);
        
        JSONArray attachments=o.optJSONArray("attachments");
        comment.attachments=Attachment.parseAttachments(attachments, 0, 0, null);
        
        return comment;
    }

    private static void parseLikes(JSONObject o, Comment comment) throws JSONException {
        if (o.has("likes")){
            JSONObject jlikes = o.getJSONObject("likes");
            comment.like_count = jlikes.optInt("count");
            comment.user_like = jlikes.optInt("user_likes")==1;
            comment.can_like = jlikes.optInt("can_like")==1;
        }
    }

    //for group topic comments 
    public static Comment parseTopicComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = o.getLong("id");
        comment.from_id = o.optLong("from_id");
        comment.date = o.optLong("date");
        comment.message = Api.unescape(o.optString("text"));
        parseLikes(o, comment);
        
        JSONArray attachments=o.optJSONArray("attachments");
        comment.attachments=Attachment.parseAttachments(attachments, 0, 0, null);
        
        return comment;
    }
     
    public static Comment parseNoteComment(JSONObject o) throws NumberFormatException, JSONException {
        Comment comment = new Comment();
        comment.cid = Long.parseLong(o.getString("id"));
        comment.from_id = Long.parseLong(o.getString("uid"));
        comment.date = Long.parseLong(o.getString("date"));
        comment.message = Api.unescape(o.getString("message"));
        comment.reply_to_uid=o.optLong("reply_to");
        return comment;
    }
    
    public static Comment parseNotificationComment(JSONObject o, boolean parse_post) throws NumberFormatException, JSONException{
        Comment comment = new Comment();
        comment.cid = o.getLong("id");
        if(o.has("owner_id"))
            comment.from_id = o.getLong("owner_id");
        else
            comment.from_id = o.getLong("from_id");//or uid. fix for reply_comment и comment_post, поле feedback. Баг в API.
        comment.date = o.getLong("date");
        comment.message = Api.unescape(o.getString("text"));
        if (o.has("likes")){
            JSONObject jlikes = o.getJSONObject("likes");
            comment.like_count = jlikes.optInt("count");
            comment.user_like = jlikes.optInt("user_likes")==1;
        }
        if(parse_post){
            JSONObject post_json=o.getJSONObject("post");
            comment.post=WallMessage.parse(post_json);
        }
        return comment;
    }
}
