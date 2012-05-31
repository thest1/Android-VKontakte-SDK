package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    public static final String FOLLOW = "follow";
    public static final String FRIEND_ACCEPTED = "friend_accepted";
    public static final String MENTION = "mention";
    public static final String MENTION_COMMENTS = "mention_comments";
    public static final String WALL = "wall";
    public static final String COMMENT_POST = "comment_post";
    public static final String COMMENT_PHOTO = "comment_photo";
    public static final String COMMENT_VIDEO = "comment_video";
    public static final String REPLY_COMMENT = "reply_comment";
    public static final String REPLY_TOPIC = "reply_topic";
    public static final String LIKE_POST = "like_post";
    public static final String LIKE_COMMENT = "like_comment";
    public static final String LIKE_PHOTO = "like_photo";
    public static final String LIKE_VIDEO = "like_video";
    public static final String COPY_POST = "copy_post";
    public static final String COPY_PHOTO = "copy_photo";
    public static final String COPY_VIDEO = "copy_video";
    
    public String type;
    public Long date;
    public Object parent;
    public Object feedback;
    public Object reply;
    
    public static Notification parse(JSONObject o) {
        Notification n = null;
        try {
            n = new Notification();
            n.type = o.getString("type");
            n.date = o.optLong("date");
            if (n.type.equals(FOLLOW)) {
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                n.parent = null;//empty
                if (jfeedback != null)
                    n.feedback = getProfiles(jfeedback);
            } else if (n.type.equals(FRIEND_ACCEPTED)) {
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                n.parent = null;//empty
                if (jfeedback != null)
                    n.feedback = getProfiles(jfeedback);
            } else if (n.type.equals(MENTION)) {
                JSONObject jfeedback = o.optJSONObject("feedback");//post
                n.parent = null;//empty
                if (jfeedback != null)
                    n.feedback = WallMessage.parse(jfeedback);
            } else if (n.type.equals(MENTION_COMMENTS)) {
                JSONObject jparent = o.optJSONObject("parent"); //post
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = WallMessage.parse(jparent);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false); 
                }
            } else if (n.type.equals(WALL)) {
                JSONObject jfeedback = o.optJSONObject("feedback");//post
                if (jfeedback != null)
                    n.feedback = WallMessage.parseForNotifications(jfeedback);
            } else if (n.type.equals(COMMENT_POST)) {
                JSONObject jparent = o.optJSONObject("parent"); //post
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = WallMessage.parse(jparent);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false); 
                }
            } else if (n.type.equals(COMMENT_PHOTO)) {
                JSONObject jparent = o.optJSONObject("parent"); //photo
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = Photo.parse(jparent);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false);
                }
            } else if (n.type.equals(COMMENT_VIDEO)) {
                JSONObject jparent = o.optJSONObject("parent"); //video
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = Video.parse(jparent);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false);
                }
            } else if (n.type.equals(REPLY_COMMENT)) {
                JSONObject jparent = o.optJSONObject("parent"); //comment
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = Comment.parseNotificationComment(jparent, true);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false);
                }
            } else if (n.type.equals(REPLY_TOPIC)) {
                JSONObject jparent = o.optJSONObject("parent"); //topic
                JSONObject jfeedback = o.optJSONObject("feedback");//comment
                if (jparent != null && jfeedback != null) {
                    n.parent = GroupTopic.parseForNotifications(jparent);
                    n.feedback = Comment.parseNotificationComment(jfeedback, false);
                }
            } else if (n.type.equals(LIKE_POST)) {
                JSONObject jparent = o.optJSONObject("parent"); //post
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                if (jparent != null && jfeedback != null) {
                    n.parent = WallMessage.parse(jparent);
                    n.feedback = getProfiles(jfeedback);
                }
            } else if (n.type.equals(LIKE_COMMENT)) {
                JSONObject jparent = o.optJSONObject("parent"); //comment
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                if (jparent != null && jfeedback != null) {
                    n.parent = Comment.parseNotificationComment(jparent, false);
                    n.feedback = getProfiles(jfeedback);
                }
            } else if (n.type.equals(LIKE_PHOTO)) {
                JSONObject jparent = o.optJSONObject("parent"); //photo
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                if (jparent != null && jfeedback != null) {
                    n.parent = Photo.parse(jparent);
                    n.feedback = getProfiles(jfeedback);
                }
            } else if (n.type.equals(LIKE_VIDEO)) {
                JSONObject jparent = o.optJSONObject("parent"); //video
                JSONArray jfeedback = o.optJSONArray("feedback");//profiles
                if (jparent != null && jfeedback != null) {
                    n.parent = Video.parse(jparent);
                    n.feedback = getProfiles(jfeedback);
                }
            } else if (n.type.equals(COPY_POST)) {
                JSONObject jparent = o.optJSONObject("parent"); //wall
                JSONArray jfeedback = o.optJSONArray("feedback");//copy
                if (jparent != null && jfeedback != null) {
                    n.parent = WallMessage.parse(jparent);
                    n.feedback = getCopies(jfeedback);
                }
            } else if (n.type.equals(COPY_PHOTO)) {
                JSONObject jparent = o.optJSONObject("parent"); //photo
                JSONArray jfeedback = o.optJSONArray("feedback");//copy
                if (jparent != null && jfeedback != null) {
                    n.parent = Photo.parse(jparent);
                    n.feedback = getCopies(jfeedback);
                }
            } else if (n.type.equals(COPY_VIDEO)) {
                JSONObject jparent = o.optJSONObject("parent"); //video
                JSONArray jfeedback = o.optJSONArray("feedback");//copy
                if (jparent != null && jfeedback != null) {
                    n.parent = Video.parse(jparent);
                    n.feedback = getCopies(jfeedback);
                }
            }        
            JSONObject jreply = o.optJSONObject("reply");
            if (jreply != null)
                n.reply = Reply.parse(jreply);
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
        return n;
    }

    public static ArrayList<Notification> parseNotifications(JSONArray jnotifications) throws JSONException {
        ArrayList<Notification> notifications = new ArrayList<Notification>();
        for(int i = 0; i < jnotifications.length(); i++) {
            if(!(jnotifications.get(i) instanceof JSONObject))
                continue;
            JSONObject jgroup = (JSONObject)jnotifications.get(i);
            Notification n = Notification.parse(jgroup);
            if (n != null)
                notifications.add(n);
        }
        return notifications;
    }
    
    public static ArrayList<Object> getProfiles(JSONArray jfeedback) throws JSONException {
        ArrayList<Object> ids = new ArrayList<Object>();
        for (int i = 0; i < jfeedback.length(); i++) {
            if(!(jfeedback.get(i) instanceof JSONObject))
                continue;
            JSONObject j_id = (JSONObject)jfeedback.get(i);
            Long id = j_id.optLong("owner_id");
            if (id != null)
                ids.add(id);
        }
        return ids;
    }
    
    public static ArrayList<Object> getCopies(JSONArray jfeedback) throws JSONException {
        ArrayList<Object> ids = new ArrayList<Object>();
        for (int i = 0; i < jfeedback.length(); i++) {
            if(!(jfeedback.get(i) instanceof JSONObject))
                continue;
            JSONObject j_id = (JSONObject)jfeedback.get(i);
            Long id = j_id.optLong("id");
            Long owner_id = j_id.optLong("owner_id");
            if (id != null && owner_id != null) {
                IdsPair c = new IdsPair();
                c.id = id;
                c.owner_id = owner_id;
                ids.add(c);
            }
        }
        return ids;
    }
    
}
