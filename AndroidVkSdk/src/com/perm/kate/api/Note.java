package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    public long nid; 
    public long owner_id; 
    public String title; 
    public String text;
    public long date=0;
    public long ncom=-1;
    //public long read_ncom=-1;

    public static Note parse(JSONObject o, boolean is_get) throws NumberFormatException, JSONException{
        Note note = new Note();
        if(is_get)
            note.nid = o.getLong("nid");
        else
            note.nid = Long.parseLong(o.optString("nid"));
        if(is_get)
            note.owner_id = Long.parseLong(o.getString("uid"));
        else
            note.owner_id = o.getLong("owner_id");
        note.title = Api.unescape(o.getString("title"));
        if(is_get)
            note.ncom = Long.parseLong(o.optString("ncom"));
        else
            note.ncom = o.optLong("ncom");
        //note.read_ncom = o.optLong("read_ncom");
        note.text=o.optString("text");
        note.date = o.optLong("date");
        return note;
    }
}
