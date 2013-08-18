package com.perm.kate.api;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BannArg {

    public ArrayList<Long> members;
    public ArrayList<Long> groups;
    public ArrayList<Group> full_groups;
    public ArrayList<User> profiles;
    public boolean is_extended;
    
    public BannArg(boolean extended) {
        is_extended = extended;
    }

    public static BannArg parse(JSONObject object, boolean is_extended) throws JSONException {
        BannArg arg = null;
        JSONArray jmembers = object.optJSONArray("members");
        JSONArray jgroups = object.optJSONArray("groups");
        JSONArray jprofiles = object.optJSONArray("profiles");
        if (is_extended) {
            arg = new BannArg(true);
            if (jgroups != null)
                arg.full_groups = Group.parseGroups(jgroups);
            if (jprofiles != null) {
                arg.profiles = new ArrayList<User>();
                for(int i = 0; i < jprofiles.length(); i++) {
                    JSONObject jprofile = (JSONObject)jprofiles.get(i);
                    User m = User.parseFromNews(jprofile);
                    arg.profiles.add(m);
                }
            }
        } else {
            arg = new BannArg(false);
            if (jgroups != null) {
                arg.groups = new ArrayList<Long>();
                int category_count = jgroups.length();
                for (int i=0; i<category_count; ++i) {
                    Long id = jgroups.optLong(i, -1);
                    if (id != -1)
                        arg.groups.add(id);
                }
            }
            if (jmembers != null) {
                arg.members = new ArrayList<Long>();
                int category_count = jmembers.length();
                for (int i=0; i<category_count; ++i) {
                    Long id = jmembers.optLong(i, -1);
                    if (id != -1)
                        arg.members.add(id);
                }
            }
        }
        return arg;
    }
}
