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
    public String thumb_src;

    public static Album parse(JSONObject o) throws JSONException {
        Album a = new Album();
        a.title = Api.unescape(o.optString("title"));
        a.aid = Long.parseLong(o.getString("aid"));
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
        String privacy = o.optString("privacy");
        if (privacy != null && !privacy.equals("") && !privacy.equals("null"))
            a.privacy = Long.parseLong(privacy);
        String comment_privacy = o.optString("comment_privacy");
        if (comment_privacy != null && !comment_privacy.equals("") && !comment_privacy.equals("null"))
            a.comment_privacy = Long.parseLong(comment_privacy);
        a.size = o.optLong("size");
        String updated = o.optString("updated");
        if (updated != null && !updated.equals("") && !updated.equals("null"))
            a.updated = Long.parseLong(updated);
        a.thumb_src = o.optString("thumb_src");
        return a;
    }
}
