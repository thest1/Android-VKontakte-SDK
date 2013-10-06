package com.perm.kate.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupBanItem {
    
    public long id; // TODO for v5.0 - id
    public String first_name;
    public String last_name;
    public String photo_100; //not documented
    public BanInfo banInfo;
    
    public static GroupBanItem parse(JSONObject o) throws NumberFormatException, JSONException {
        GroupBanItem item = new GroupBanItem();
        if (o.isNull("id"))
            return null;  //TODO skip group, so as no full info
        item.id = Long.parseLong(o.getString("id"));
        if (!o.isNull("first_name"))
            item.first_name = o.optString("first_name");
        if (!o.isNull("last_name"))
            item.last_name = o.optString("last_name");
        if (!o.isNull("photo_100"))
            item.photo_100 = o.optString("photo_100");
        if (!o.isNull("ban_info")) {
            JSONObject object = o.optJSONObject("ban_info");
            if (object != null)
                item.banInfo = BanInfo.parse(object);
        }
        return item;
    }
    
    public static ArrayList<GroupBanItem> parseAll(JSONArray array) throws NumberFormatException, JSONException {
        ArrayList<GroupBanItem> items = new ArrayList<GroupBanItem>();
        if (array == null)
            return items;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            if (!(array.get(i) instanceof JSONObject))
                continue;
            JSONObject o = (JSONObject)array.get(i);
            GroupBanItem item = parse(o);
            if (item == null || item.id == 0)
                continue; //TODO skip group, so as no full info 
            items.add(item);
        }
        return items;
    }
}
