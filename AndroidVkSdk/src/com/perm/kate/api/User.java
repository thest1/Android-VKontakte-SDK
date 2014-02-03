package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Fields are optional. Should be null if not populated
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public long uid;
    public String first_name;
    public String last_name;
    public String nickname;
    public Integer sex=null;
    public Boolean online=null;
    public Boolean online_mobile=null;
    public String birthdate; //bdate
    /**
     * В запрос надо добваить photo_50
     * URL квадратной фотографии 50х50
     */
    public String photo;			
    /**
     * В запрос надо добавить photo_200 
     * URL необрезанной фотографии 200х200
     */
    public String photo_big;		//photo_200_orig
    /**
     * В запрос надо добавить photo_200 
     * URL квадратной фотографии 200х200, <b>у некоторых пользователей, которые загружали фотографию давно её нет</b>
     */
    public String photo_200;	
    /**
     * <b>В запрос надо добавить photo_100</b>
     * URL квадратной фотографии 100х100
     */
    public String photo_medium_rec;	//photo_100 квадратная
    /** 
     * В запрос надо добавить photo_max
     * URL квадратной фотографии максимального формата, <b>у некоторых пользователей, которые загружали фотографию давно её нет</b>
     */
    public String photo_max;
    /**
     * В запрос надо добавить photo_max_orig
     * URL необрезанной фотографии максимального формата
     */
    public String photo_max_orig; 	//photo_max_orig обычно квадратная, может быть не у всех
    /**
     * В запрос надо добавить photo_400_orig
     * URL необрезанной фотографии формата 400х400
     */
    public String photo_400_orig;	
    public Integer city=null;
    public Integer country=null;
    public Integer timezone=null;
    public String lists;
    public String domain;
    public Integer rate=null;
    public Integer university=null; //if education 
    public String university_name; //if education
    public Integer faculty=null; //if education
    public String faculty_name; //if education
    public Integer graduation=null; //if education
    public Boolean has_mobile=null;
    public String home_phone;
    public String mobile_phone;
    public String status;
    public Integer relation;
    public String friends_list_ids = null;
    public long last_seen;
    public int albums_count;
    public int videos_count;
    public int audios_count;
    public int notes_count;
    public int friends_count;
    public int user_photos_count;
    public int user_videos_count;
    public int followers_count;
    //public int subscriptions_count;
    //public int online_friends_count;
    public String phone;//for getByPhones
    public int groups_count;
    //relation_partner
    public Long relation_partner_id;
    public String relation_partner_first_name;
    public String relation_partner_last_name;
    
    //new connections fields
    public String twitter;
    public String facebook;
    public String facebook_name;
    public String skype;
    public String livejounal;

    //new additional fields
    public String interests;
    public String movies;
    public String tv;
    public String books;
    public String games;
    public String about;
    
    
    public static User parse(JSONObject o) throws JSONException {
        User u = new User();
        u.uid = Long.parseLong(o.getString("id"));
        if(!o.isNull("first_name"))
            u.first_name = Api.unescape(o.getString("first_name"));
        if(!o.isNull("last_name"))
            u.last_name = Api.unescape(o.getString("last_name"));
        if(!o.isNull("nickname"))
            u.nickname = Api.unescape(o.optString("nickname"));
        if(!o.isNull("screen_name"))
            u.domain = o.optString("screen_name");
        if(!o.isNull("online"))
            u.online = o.optInt("online")==1;
        if(!o.isNull("online_mobile"))
            u.online_mobile = o.optInt("online_mobile")==1;
        else
            //if it's not there it means false
            u.online_mobile=false;
        if(!o.isNull("sex"))
            u.sex = o.optInt("sex");
        if(!o.isNull("bdate"))
            u.birthdate = o.optString("bdate");
        if(o.has("city"))
            u.city = o.optInt("city");
        if(o.has("country"))
            u.country = o.optInt("country");
        if(!o.isNull("timezone"))
            u.timezone = o.optInt("timezone");
        if(!o.isNull("photo_50"))
            u.photo = o.optString("photo_50");
        if(!o.isNull("photo_100"))
            u.photo_medium_rec = o.optString("photo_100");
        if(!o.isNull("photo_200_orig"))
            u.photo_big = o.optString("photo_200_orig");
        if(!o.isNull("photo_200"))
            u.photo_200 = o.optString("photo_200");
        if(!o.isNull("photo_max"))
            u.photo_max = o.optString("photo_max");
        if(!o.isNull("photo_max_orig"))
            u.photo_max_orig = o.optString("photo_max_orig");
        if(!o.isNull("photo_400_orig"))
            u.photo_400_orig = o.optString("photo_400_orig");
        if(!o.isNull("has_mobile"))
            u.has_mobile = o.optInt("has_mobile")==1;
        if(!o.isNull("home_phone"))
            u.home_phone = o.optString("home_phone");
        if(!o.isNull("mobile_phone"))
            u.mobile_phone = o.optString("mobile_phone");
        if(!o.isNull("rate"))
            u.rate = o.optInt("rate");
        if(o.has("faculty"))
            u.faculty = o.optInt("faculty");
        if(!o.isNull("faculty_name"))
            u.faculty_name = o.optString("faculty_name");
        if(o.has("university"))
            u.university = o.optInt("university");
        if(!o.isNull("university_name"))
            u.university_name = o.optString("university_name");
        if(o.has("graduation"))
            u.graduation = o.optInt("graduation");
        if(!o.isNull("activity"))
            u.status = Api.unescape(o.optString("activity"));
        if(!o.isNull("relation"))
            u.relation = o.optInt("relation");
        if (!o.isNull("lists")) {
            JSONArray array = o.optJSONArray("lists");
            if (array != null) {
                String ids = "";
                for (int i=0; i<array.length()-1;++i)
                    ids += array.getString(i) + ",";
                ids += array.getString(array.length()-1);
                u.friends_list_ids = ids;
            }
        }
        if(!o.isNull("last_seen")) {
            JSONObject object = o.optJSONObject("last_seen");
            if (object != null)
                u.last_seen = object.optLong("time");
        }
        if(!o.isNull("counters")) {
            JSONObject object = o.optJSONObject("counters");
            if (object != null) {
                u.albums_count = object.optInt("albums");
                u.videos_count = object.optInt("videos");
                u.audios_count = object.optInt("audios");
                u.notes_count = object.optInt("notes");
                u.friends_count = object.optInt("friends");
                u.user_photos_count = object.optInt("user_photos");
                u.user_videos_count = object.optInt("user_videos");
                //u.online_friends_count = object.optInt("online_friends");
                u.followers_count = object.optInt("followers");
                //u.subscriptions_count = object.optInt("subscriptions");
                u.groups_count = object.optInt("groups");
            }
        }
        if(!o.isNull("relation_partner")) {
            JSONObject object = o.optJSONObject("relation_partner");
            if (object != null) {
                u.relation_partner_id = object.optLong("id");
                u.relation_partner_first_name = object.optString("first_name");
                u.relation_partner_last_name = object.optString("last_name");
            }
        }

        if(!o.isNull("twitter"))
            u.twitter = o.optString("twitter");
        if(!o.isNull("facebook"))
            u.facebook = o.optString("facebook");
        if(!o.isNull("facebook_name"))
            u.facebook_name = o.optString("facebook_name");
        if(!o.isNull("skype"))
            u.skype = o.optString("skype");
        if(!o.isNull("livejounal"))
            u.livejounal = o.optString("livejounal");

        if(!o.isNull("interests"))
            u.interests = o.optString("interests");
        if(!o.isNull("movies"))
            u.movies = o.optString("movies");
        if(!o.isNull("tv"))
            u.tv = o.optString("tv");
        if(!o.isNull("books"))
            u.books = o.optString("books");
        if(!o.isNull("games"))
            u.games = o.optString("games");
        if(!o.isNull("about"))
            u.about = o.optString("about");

        return u;
    }

    public static User parseFromNews(JSONObject jprofile) throws JSONException {
        User m = new User();
        m.uid = jprofile.getLong("id");
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo = jprofile.optString("photo_50");
        m.photo_medium_rec = jprofile.optString("photo_100");
        if(jprofile.has("sex"))
            m.sex = jprofile.optInt("sex");
        if(!jprofile.isNull("online"))
            m.online = jprofile.optInt("online")==1;
        return m;
    }
    
    public static User parseFromGetByPhones(JSONObject o) throws JSONException {
        User u = new User();
        u.uid = o.getLong("id");
        u.first_name = Api.unescape(o.optString("first_name"));
        u.last_name = Api.unescape(o.optString("last_name"));
        u.phone = o.optString("phone");
        return u;
    }
    
    public static ArrayList<User> parseUsers(JSONArray array) throws JSONException {
        return parseUsers(array, false);
    }
    
    public static ArrayList<User> parseUsers(JSONArray array, boolean from_notifications) throws JSONException {
        ArrayList<User> users=new ArrayList<User>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            User u;
            if(from_notifications)
                u = User.parseFromNotifications(o);
            else
                u = User.parse(o);
            users.add(u);
        }
        return users;
    }
    
    public static ArrayList<User> parseUsersForGetByPhones(JSONArray array) throws JSONException {
        ArrayList<User> users=new ArrayList<User>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            if(array.get(i)==null || ((array.get(i) instanceof JSONObject)==false))
                continue;
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parseFromGetByPhones(o);
            users.add(u);
        }
        return users;
    }
    
    public static User parseFromFave(JSONObject jprofile) throws JSONException {
        User m = new User();
        m.uid = Long.parseLong(jprofile.getString("id"));
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo_medium_rec = jprofile.optString("photo_100");
        if(!jprofile.isNull("online"))
            m.online = jprofile.optInt("online")==1;
        if(!jprofile.isNull("online_mobile"))
            m.online_mobile = jprofile.optInt("online_mobile")==1;
        else
            //if it's not there it means false
            m.online_mobile=false;
        return m;
    }
    
    public static User parseFromNotifications(JSONObject jprofile) throws JSONException {
        User m = new User();
        m.uid = jprofile.getLong("id");
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo_medium_rec = jprofile.optString("photo_100");
        m.photo = jprofile.optString("photo_50");
        return m;
    }
}
