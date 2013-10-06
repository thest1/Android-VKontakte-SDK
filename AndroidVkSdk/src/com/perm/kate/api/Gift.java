package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Gift implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;
    public String thumb_256;
    //public String thumb_96;
    //public String thumb_48;

    public static Gift parse(JSONObject o) throws NumberFormatException, JSONException{
        Gift gift = new Gift();
        gift.id = o.getLong("id");
        gift.thumb_256 = o.getString("thumb_256");
        //audio.thumb_96 = o.getString("thumb_96");
        //audio.thumb_48 = o.getString("thumb_48");
        return gift;
    }
}