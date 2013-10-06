package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoTag {
    
    public Long owner_id;
    public long pid;
    
    public long uid;
    public long tag_id;
    public long placer_id;
    public String tagged_name;
    public long date;
    public double x;
    public double y;
    public double x2;
    public double y2;
    public int viewed;
    
    public static PhotoTag parse(JSONObject o) throws NumberFormatException, JSONException {
        PhotoTag t = new PhotoTag();
        //Баг в API - должно быть user_id - уже исправлен, оставил старое пока для подстраховки, можно удалить уже
        if(o.has("user_id"))
            t.uid = o.getLong("user_id");
        else
            t.uid = o.getLong("photo_id");
        t.tag_id = o.optLong("id");
        t.placer_id = o.optLong("placer_id");
        t.tagged_name = Api.unescape(o.optString("tagged_name"));
        t.date = o.optLong("date");
        t.x = o.optDouble("x");
        t.x2 = o.optDouble("x2");
        t.y = o.optDouble("y");
        t.y2 = o.optDouble("y2");
        t.viewed = o.optInt("viewed");
        return t;
    }
    
    public PhotoTag() { 
        
    }
    
    public PhotoTag(Long owner_id, long pid, long uid, double x, double y, double x2, double y2) { 
        this.owner_id = owner_id;
        this.pid = pid;
        this.uid = uid;
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
    }
}
