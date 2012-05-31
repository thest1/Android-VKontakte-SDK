package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Graffiti implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id; 
    public long owner_id; 
    public String src; //200*100 http://cs10730.vkontakte.ru/u110317842/s_5a43e302.png
    public String src_big;//586*293 http://cs10730.vkontakte.ru/u110317842/l_f8bc298f.png
    
    public static Graffiti parse(JSONObject o) throws NumberFormatException, JSONException{
        Graffiti graffiti = new Graffiti();
        graffiti.id = o.optLong("gid");
        graffiti.owner_id = o.optLong("owner_id");
        graffiti.src = o.optString("src");
        graffiti.src_big = o.optString("src_big");
        return graffiti;
    }
}
