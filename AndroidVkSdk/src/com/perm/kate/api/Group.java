package com.perm.kate.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Group {
    public long gid;
    public String name;
    public String photo;//50*50
    public Boolean is_closed;
    public Boolean is_member;
    
    //это новые поля, которых у нас пока нет в базе
    //public String screen_name;
    //public Boolean is_admin;
    public String photo_medium;//100*100
    public String photo_big;//200*200

    public static Group parse(JSONObject o) throws JSONException{
        Group g=new Group();
        g.gid = o.getLong("gid");
        g.name = Api.unescape(o.getString("name"));
        g.photo = o.getString("photo");
        g.photo_medium = o.optString("photo_medium");
        g.photo_big = o.optString("photo_big");
        String is_closed = o.optString("is_closed");
        if(is_closed != null)
            g.is_closed = is_closed.equals("1");
        String is_member = o.optString("is_member");
        if(is_member != null)
            g.is_member = is_member.equals("1");
        
        //это новые поля, которых у нас пока нет в базе
        //g.screen_name=o.optString("screen_name");
        //String is_admin=o.optString("is_admin");
        //if(is_admin!=null)
        //    g.is_admin=is_admin.equals("1");
        //g.photo_medium = o.getString("photo_medium");
        //g.photo_big = o.getString("photo_big");
        return g;
    }
    
    public static ArrayList<Group> parseGroups(JSONArray jgroups) throws JSONException {
        ArrayList<Group> groups=new ArrayList<Group>();
        for(int i = 0; i < jgroups.length(); i++) {
            //для метода groups.get первый элемент - количество
            if(!(jgroups.get(i) instanceof JSONObject))
                continue;
            JSONObject jgroup = (JSONObject)jgroups.get(i);
            Group group = Group.parse(jgroup);
            groups.add(group);
        }
        return groups;
    }
}
