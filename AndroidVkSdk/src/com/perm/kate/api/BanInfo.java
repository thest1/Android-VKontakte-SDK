package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class BanInfo {

    public long admin_id;
    public long date;
    public int reason;
    public String comment;
    public long end_date;

    public static BanInfo parse(JSONObject o) throws NumberFormatException, JSONException {
        BanInfo info = new BanInfo();
        info.admin_id = Long.parseLong(o.getString("admin_id"));
        info.date = Long.parseLong(o.getString("date"));
        info.reason = Integer.parseInt(o.getString("reason"));
        info.comment = o.optString("comment");
        info.end_date = Long.parseLong(o.getString("admin_id"));
        return info;
    }

}
