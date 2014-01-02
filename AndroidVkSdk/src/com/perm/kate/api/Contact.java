package com.perm.kate.api;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Contact  implements Serializable {

    private static final long serialVersionUID = 1L;

    public Long user_id;
    public String desc;
    public String email;
    public String phone;
    
    public static Contact parse(JSONObject o) throws JSONException {
        Contact c = new Contact();
        if(o.has("user_id"))
            c.user_id = o.optLong("user_id");
        c.desc = o.optString("desc");
        c.email = o.optString("email");
        c.phone = o.optString("phone");
        return c;
    }
}
