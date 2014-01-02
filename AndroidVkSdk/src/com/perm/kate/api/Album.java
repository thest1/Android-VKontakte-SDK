package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

public class Album implements Serializable {
    public long aid;
    public long thumb_id;
    public long owner_id;
    public String title;
    public String description;
    public long created;
    public long updated;
    public long size;
    public long privacy;
    public long comment_privacy;
    public String thumb_src;

    public static Album parse(JSONObject o) throws JSONException {
        Album a = new Album();
        a.title = Api.unescape(o.optString("title"));
        a.aid = Long.parseLong(o.getString("id"));
        a.owner_id = Long.parseLong(o.getString("owner_id"));
        String description = o.optString("description");
        if (description != null && !description.equals("") && !description.equals("null"))
            a.description = Api.unescape(description);
        String thumb_id = o.optString("thumb_id");
        if (thumb_id != null && !thumb_id.equals("") && !thumb_id.equals("null"))
            a.thumb_id = Long.parseLong(thumb_id);
        String created = o.optString("created");
        if (created != null && !created.equals("") && !created.equals("null"))
            a.created = Long.parseLong(created);
        
        JSONObject privacy=o.optJSONObject("privacy_view");
        if(privacy!=null){
            String type = privacy.optString("type");
            if("all".equals(type))
                a.privacy=0;
            else if("friends".equals(type))
                a.privacy=1;
            else if("friends_of_friends".equals(type))
                a.privacy=2;
            else if("nobody".equals(type))
                a.privacy=3;
            else if("users".equals(type))
                a.privacy=4;
        }
        
        JSONObject privacy_comment=o.optJSONObject("privacy_comment");
        if(privacy_comment!=null){
            String type = privacy_comment.optString("type");
            if("all".equals(type))
                a.comment_privacy=0;
            else if("friends".equals(type))
                a.comment_privacy=1;
            else if("friends_of_friends".equals(type))
                a.comment_privacy=2;
            else if("nobody".equals(type))
                a.comment_privacy=3;
            else if("users".equals(type))
                a.comment_privacy=4;
        }
        
        a.size = o.optLong("size");
        String updated = o.optString("updated");
        if (updated != null && !updated.equals("") && !updated.equals("null"))
            a.updated = Long.parseLong(updated);
        a.thumb_src = o.optString("thumb_src");
        return a;
    }
    
    public static Album parseFromAttachment(JSONObject o) throws JSONException {
        Album a = new Album();
        a.title = Api.unescape(o.optString("title"));
        a.aid = Long.parseLong(o.getString("id"));
        a.owner_id = Long.parseLong(o.getString("owner_id"));
        return a;
    }
}