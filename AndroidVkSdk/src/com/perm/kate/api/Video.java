package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Video implements Serializable{
    private static final long serialVersionUID = 1L;
    public long vid;
    public long owner_id;
    public String title;
    public String description;
    public long duration;
    public String link1;
    public String image;//130*97
    public String image_big;//320*240
    //public String photo_640;
    public long date;
    public String player;
    //files
    public String external;
    public String mp4_240;
    public String mp4_360;
    public String mp4_480;
    public String mp4_720;
    public String flv_320;
    public String access_key;//used when private video attached to message
    public int views;
    
    
    public static Video parse(JSONObject o) throws NumberFormatException, JSONException{
        Video v = new Video();
        v.vid = o.getLong("id");
        v.owner_id = o.getLong("owner_id");
        v.title = Api.unescape(o.optString("title"));
        v.duration = o.optLong("duration");
        v.description = Api.unescape(o.optString("description"));
        v.image = o.optString("photo_130");
        //notifications.get возвращает видео по-старому в типе like_video - баг в API
        if(!o.has("photo_130") && o.has("image"))
            v.image = o.optString("image");
        v.image_big = o.optString("photo_320");
        //notifications.get возвращает видео по-старому в типе like_video - баг в API
        if(!o.has("photo_320") && o.has("image_medium"))
            v.image_big = o.optString("image_medium");
        v.date = o.optLong("date");
        v.player = o.optString("player");
        if (o.has("views"))
            v.views = o.getInt("views");
        
        JSONObject files=o.optJSONObject("files");
        if(files!=null){
            v.external = files.optString("external");
            v.mp4_240 = files.optString("mp4_240");
            v.mp4_360 = files.optString("mp4_360");
            v.mp4_480 = files.optString("mp4_480");
            v.mp4_720 = files.optString("mp4_720");
            v.flv_320 = files.optString("flv_320");
        }
        return v;
    }
    
    public static Video parseForAttachments(JSONObject o) throws NumberFormatException, JSONException{
        Video v = new Video();
        v.vid = o.getLong("id");
        v.owner_id = o.getLong("owner_id");
        v.title = Api.unescape(o.getString("title"));
        v.duration = o.getLong("duration");
        v.description = Api.unescape(o.optString("description"));
        v.image = o.optString("photo_130");
        v.image_big = o.optString("photo_320");
        v.date = o.optLong("date");
        v.player = o.optString("player");
        v.access_key = o.optString("access_key");
        return v;
    }
    
    public String getVideoUrl() {
        return getVideoUrl(owner_id, vid);
    }
    
    public static String getVideoUrl(long owner_id, long video_id) {
        String res = null;
        String base_url = "http://vk.com/";
        res = base_url + "video" + owner_id + "_" + video_id;
        //sample http://vkontakte.ru/video4491835_158963813
        //http://79.gt2.vkadre.ru/assets/videos/f6b1af1e4258-24411750.vk.flv
        return res;
    }
}