package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Album {
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

    public static Album parse(JSONObject o) throws JSONException {
        Album a = new Album();
        a.title = Api.unescape(o.optString("title"));
        a.aid = Long.parseLong(o.getString("aid"));
        a.owner_id = Long.parseLong(o.getString("owner_id"));
        a.description = Api.unescape(o.optString("description"));
        a.thumb_id = Long.parseLong(o.optString("thumb_id"));
        a.created = Long.parseLong(o.optString("created"));
        String privacy = o.optString("privacy");
        if(privacy != null && !privacy.equals("") && !privacy.equals("null"))
            a.privacy = Long.parseLong(privacy);
        String comment_privacy = o.optString("comment_privacy");
        if(comment_privacy != null && !comment_privacy.equals("") && !comment_privacy.equals("null"))
            a.comment_privacy = Long.parseLong(comment_privacy);
        a.size = o.optLong("size");
        a.updated = Long.parseLong(o.optString("updated"));
        return a;
    }
}
