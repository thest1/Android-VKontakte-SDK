package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupTopic {
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
        topic.tid = Long.parseLong(o.getString("tid"));
        topic.title = Api.unescape(o.getString("title"));
        topic.created = Long.parseLong(o.getString("created"));
        topic.created_by = Long.parseLong(o.getString("created_by"));
        topic.updated = Long.parseLong(o.getString("updated"));
        topic.updated_by = Long.parseLong(o.getString("updated_by"));
        topic.is_closed = Integer.parseInt(o.getString("is_closed"));
        topic.is_fixed = Integer.parseInt(o.getString("is_fixed"));
        topic.comments = Integer.parseInt(o.getString("comments"));
        return topic;
    }
}
