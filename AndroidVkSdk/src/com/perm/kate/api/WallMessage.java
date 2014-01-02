package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WallMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public long from_id;
    public long to_id;
    public long date; 
    public int post_type; //where -1 - undefined 0 - post, 1 - copy, 2 - postpone, 3 - suggests
    public String text;
    public long id;
    public ArrayList<Attachment> attachments;
    public long comment_count;
    public boolean comment_can_post;

    //likes
    public int like_count;
    public boolean user_like;
    public boolean can_like;
    public boolean like_can_publish;
    
    //reposts
    public int reposts_count;
    public boolean user_reposted;
    
    //deprecated fields
    public long copy_owner_id=0;
    public long copy_post_id=0;
    public String copy_text;
    
    public ArrayList<WallMessage> copy_history;
    
    public long signer_id=0;
    
    public static WallMessage parse(JSONObject o) throws JSONException {
        WallMessage wm = new WallMessage();
        wm.id = o.getLong("id");
        wm.from_id = o.getLong("from_id");
        if(o.has("to_id"))
            wm.to_id = o.getLong("to_id");
        else
            //in copy_history owner_id is used
            wm.to_id = o.getLong("owner_id");
        wm.date = o.optLong("date");
        wm.post_type = getPostType(o);
        wm.text = Api.unescape(o.optString("text"));
        if (o.has("likes")){
            JSONObject jlikes = o.getJSONObject(NewsJTags.LIKES);
            wm.like_count = jlikes.optInt("count");
            wm.user_like = jlikes.optInt("user_likes")==1;
            wm.can_like = jlikes.optInt("can_like")==1;
            wm.like_can_publish = jlikes.optInt("can_publish")==1;
        }
        JSONArray copy_history_json=o.optJSONArray("copy_history");
        if(copy_history_json!=null){
            wm.copy_history=new ArrayList<WallMessage>();
            for(int i=0;i<copy_history_json.length();++i){
                JSONObject history_item=copy_history_json.getJSONObject(i);
                
                //empty items happen sometimes, seems to be bug in API
                if(history_item.isNull("id"))
                    continue;
                
                wm.copy_history.add(parse(history_item));
            }
        }
        JSONArray attachments=o.optJSONArray("attachments");
        JSONObject geo_json=o.optJSONObject("geo");
        //владельцем опроса является to_id. Даже если добавить опрос в группу от своего имени, то from_id буду я, но опрос всё-равно будет принадлежать группе.
        wm.attachments=Attachment.parseAttachments(attachments, wm.to_id, wm.copy_owner_id, geo_json);
        if (o.has("comments")){
            JSONObject jcomments = o.getJSONObject("comments");
            wm.comment_count = jcomments.optInt("count");
            wm.comment_can_post = jcomments.optInt("can_post")==1;
        }
        wm.signer_id = o.optLong("signer_id");
        if (o.has(NewsJTags.REPOSTS)){
            JSONObject jlikes = o.getJSONObject(NewsJTags.REPOSTS);
            wm.reposts_count = jlikes.optInt("count");
            wm.user_reposted = jlikes.optInt("user_reposted")==1;
        }
        return wm;
    }
    
    public static WallMessage parseForNotifications(JSONObject o) throws JSONException {
        WallMessage wm = new WallMessage();
        wm.id = o.getLong("id");
        wm.from_id = o.getLong("from_id");
        wm.to_id = o.optLong("to_id");
        wm.post_type = getPostType(o);
        wm.text = Api.unescape(o.getString("text"));
        //likes is there but I don't parse it because I don't need it
        //if (o.has("likes")){
        //    JSONObject jlikes = o.getJSONObject(NewsJTags.LIKES);
        //    wm.like_count = jlikes.getInt("count");
        //    wm.user_like = jlikes.getInt("user_likes")==1;
        //    wm.can_like = jlikes.getInt("can_like")==1;
        //    wm.like_can_publish = jlikes.getInt("can_publish")==1;
        //}
        JSONArray attachments=o.optJSONArray("attachments");
        JSONObject geo_json=o.optJSONObject("geo");
        wm.attachments=Attachment.parseAttachments(attachments, wm.to_id, wm.copy_owner_id, geo_json);
        return wm;
    }
    
    public static int getPostType(JSONObject o) {
        int post_type = -1;
        if (o.has("post_type")) {
            String _post_type = o.optString("post_type");
            if ("post".equals(_post_type))
                post_type = 0;
            else if ("copy".equals(_post_type))
                post_type = 1;
            else if ("postpone".equals(_post_type))
                post_type = 2;
            else if ("suggest".equals(_post_type))
                post_type = 3;
        }
        return post_type;
    }
}