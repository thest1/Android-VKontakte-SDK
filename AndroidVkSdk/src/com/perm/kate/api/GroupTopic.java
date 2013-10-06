package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupTopic implements Serializable {
    private static final long serialVersionUID = 1L;
    public long tid;
    public long gid;
    public String title;
    public long created;
    public long created_by;
    public long updated;
    public long updated_by;
    public int is_closed;
    public int is_fixed;
    public int comments;
    
    public static GroupTopic parse(JSONObject o) throws NumberFormatException, JSONException {
        GroupTopic topic = new GroupTopic();
        topic.tid = o.getLong("id");
        topic.title = Api.unescape(o.getString("title"));
        topic.created = o.optLong("created");
        topic.created_by = o.optLong("created_by");
        topic.updated = o.optLong("updated");
        topic.updated_by = o.optLong("updated_by");
        topic.is_closed = o.optInt("is_closed");
        topic.is_fixed = o.optInt("is_fixed");
        topic.comments = o.optInt("comments");
        return topic;
    }
    
    public static GroupTopic parseForNotifications(JSONObject o) throws NumberFormatException, JSONException {
        GroupTopic topic = new GroupTopic();
        topic.tid = o.getLong("id");
        topic.title = Api.unescape(o.getString("title"));
        topic.gid = -o.getLong("owner_id");
        //topic.created = Long.parseLong(o.getString("created"));
        //topic.created_by = Long.parseLong(o.getString("created_by"));
        //topic.updated = Long.parseLong(o.getString("updated"));
        //topic.updated_by = Long.parseLong(o.getString("updated_by"));
        //topic.is_closed = Integer.parseInt(o.getString("is_closed"));
        //topic.is_fixed = Integer.parseInt(o.getString("is_fixed"));
        //topic.comments = Integer.parseInt(o.getString("comments"));
        return topic;
    }
}
