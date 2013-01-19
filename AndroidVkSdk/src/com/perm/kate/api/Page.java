package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Wiki page
 *
 */
public class Page {
    public long id;
    public long group_id;
    public String title;
    
    public static Page parseFromAttachment(JSONObject o) throws NumberFormatException, JSONException{
        Page page = new Page();
        page.title = Api.unescape(o.optString("title"));
        page.id = o.optLong("pid");
        page.group_id = o.optLong("gid");
        Log.i("sfaf", "parsed attachment");
        Log.i("sfaf", "title="+page.title);
        return page;
    }
}