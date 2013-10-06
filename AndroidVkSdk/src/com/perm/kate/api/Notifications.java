package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Notifications implements Serializable {
    
    private static final long serialVersionUID = 1L;
    public ArrayList<Notification> notifications=new ArrayList<Notification>();
    public ArrayList<User> profiles=new ArrayList<User>();
    public ArrayList<Group> groups=new ArrayList<Group>();
    
    public static Notifications parse(JSONObject response) throws JSONException{
        Notifications full_notifications = new Notifications();
        full_notifications.notifications = new ArrayList<Notification>();
        if (response==null)
            return full_notifications;
        JSONArray array = response.optJSONArray("items");
        JSONArray profiles_array = response.optJSONArray("profiles");
        JSONArray groups_array = response.optJSONArray("groups");
        if (array==null)
            return full_notifications;
        if (profiles_array != null)
            full_notifications.profiles = User.parseUsers(profiles_array, true);
        if (groups_array != null)
            full_notifications.groups = Group.parseGroups(groups_array);
        full_notifications.notifications = Notification.parseNotifications(array);
        return full_notifications;
    }
    
}
