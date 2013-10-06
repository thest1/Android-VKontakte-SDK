package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;//0 means no value
    public long owner_id;//0 means no value
    public String title;
    public String url;
    public long size;
    public String ext;
    public String thumb;//for photos. 130*65.
    public String thumb_s;//for photos. 100*50.
    public String access_key;
    
    public static Document parse(JSONObject o) throws JSONException {
        Document d = new Document();
        d.id = o.optLong("id");
        d.owner_id = o.optLong("owner_id");
        d.title = o.getString("title");
        d.url = o.optString("url");
        d.size = o.optLong("size");
        d.ext = o.optString("ext");
        d.thumb = o.optString("photo_130", null);
        d.thumb_s = o.optString("photo_100", null);
        d.access_key = o.optString("access_key", null);
        return d;
    }
    
    public static ArrayList<Document> parseDocs(JSONArray array) throws JSONException {
        ArrayList<Document> docs = new ArrayList<Document>();
        if (array != null) {
            for(int i = 0; i<array.length(); ++i) {
                Object item=array.get(i);
                if(!(item instanceof JSONObject))
                    continue;
                JSONObject o = (JSONObject)item;
                Document doc = Document.parse(o);
                docs.add(doc);
            }
        }
        return docs;
    }

}
