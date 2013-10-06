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

    public static Note parse(JSONObject o) throws NumberFormatException, JSONException{
        Note note = new Note();
        note.nid = o.optLong("id");
        
        //в новости "добавил заметку" заметка приходит по-старому - баг в API
        if(!o.has("id") && o.has("nid"))
            note.nid = o.optLong("nid");
        
        note.owner_id = o.getLong("owner_id");
        note.title = Api.unescape(o.getString("title"));
        note.ncom = o.optLong("comments");
        
        //в новости "добавил заметку" заметка приходит по-старому - баг в API
        if(!o.has("comments") && o.has("ncom"))
            note.ncom = o.optLong("ncom");
        
        //note.read_ncom = o.optLong("read_comments");
        note.text=o.optString("text");
        note.date = o.optLong("date");
        return note;
    }
}
