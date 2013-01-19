package com.perm.kate.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.perm.utils.Utils;
import com.perm.utils.WrongResponseCodeException;
import android.util.Log;

public class Api {
    static final String TAG="Kate.Api";
    
    public static final String BASE_URL="https://api.vk.com/method/";
    
    public Api(String access_token, String api_id){
        this.access_token=access_token;
        this.api_id=api_id;
    }
    
    String access_token;
    String api_id;
    
    //TODO: it's not faster, even slower on slow devices. Maybe we should add an option to disable it. It's only good for paid internet connection.
    static boolean enable_compression=true;
    
    /*** utils methods***/
    private void checkError(JSONObject root, String url) throws JSONException,KException {
        if(!root.isNull("error")){
            JSONObject error=root.getJSONObject("error");
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url); 
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            throw e;
        }
    }
    
    private JSONObject sendRequest(Params params) throws IOException, MalformedURLException, JSONException, KException {
        return sendRequest(params, false);
    }
    
    private final static int MAX_TRIES=3;
    private JSONObject sendRequest(Params params, boolean is_post) throws IOException, MalformedURLException, JSONException, KException {
        String url = getSignedUrl(params, is_post);
        String body="";
        if(is_post)
            body=params.getParamsString(is_post);
        Log.i(TAG, "url="+url);
        if(body.length()!=0)
            Log.i(TAG, "body="+body);
        String response="";
        for(int i=1;i<=MAX_TRIES;++i){
            try{
                if(i!=1)
                    Log.i(TAG, "try "+i);
                response = sendRequestInternal(url, body, is_post);
                break;
            }catch(javax.net.ssl.SSLException ex){
                processNetworkException(i, ex);
            }catch(java.net.SocketException ex){
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response="+response);
        JSONObject root=new JSONObject(response);
        checkError(root, url);
        return root;
    }

    private void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if(i==MAX_TRIES)
            throw ex;
    }

    private String sendRequestInternal(String url, String body, boolean is_post) throws IOException, MalformedURLException, WrongResponseCodeException {
        HttpURLConnection connection=null;
        try{
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(is_post);
            connection.setDoInput(true);
            connection.setRequestMethod(is_post?"POST":"GET");
            if(enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");
            if(is_post)
                connection.getOutputStream().write(body.getBytes("UTF-8"));
            int code=connection.getResponseCode();
            Log.i(TAG, "code="+code);
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code==-1)
                throw new WrongResponseCodeException("Network error");
            //может стоит проверить на код 200
            //on error can also read error stream from connection.
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if(enc!=null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            String response=Utils.convertStreamToString(is);
            return response;
        }
        finally{
            if(connection!=null)
                connection.disconnect();
        }
    }
    
    private long getDefaultStartTime() {
        long now = System.currentTimeMillis() / 1000L;//unixtime
        return now-31*24*60*60;//month ago
    }

    private String getSignedUrl(Params params, boolean is_post) {
        String args = "";
        if(!is_post)
            args=params.getParamsString(is_post);
        
        //add access_token
        if(args.length()!=0)
            args+="&";
        args+="access_token="+access_token;
        
        return BASE_URL+params.method_name+"?"+args;
    }

    public static String unescape(String text){
        if(text==null)
            return null;
        return text.replace("&amp;", "&").replace("&quot;", "\"").replace("<br>", "\n").replace("&gt;", ">").replace("&lt;", "<")
        .replace("&#39;", "'").replace("<br/>", "\n").replace("&ndash;","-").replace("&#33;", "!").trim();
        //возможно тут могут быть любые коды после &#, например были: 092 - backslash \
    }
    
    public static String unescapeWithSmiles(String text){
        return unescape(text)
                //May be useful to someone
                //.replace("\uD83D\uDE0A", ":-)")
                //.replace("\uD83D\uDE03", ":D")
                //.replace("\uD83D\uDE09", ";-)")
                //.replace("\uD83D\uDE06", "xD")
                //.replace("\uD83D\uDE1C", ";P")
                //.replace("\uD83D\uDE0B", ":p")
                //.replace("\uD83D\uDE0D", "8)")
                //.replace("\uD83D\uDE0E", "B)")
                //
                //.replace("\ud83d\ude12", ":(")  //F0 9F 98 92
                //.replace("\ud83d\ude0f", ":]")  //F0 9F 98 8F
                //.replace("\ud83d\ude14", "3(")  //F0 9F 98 94
                //.replace("\ud83d\ude22", ":'(")  //F0 9F 98 A2
                //.replace("\ud83d\ude2d", ":_(")  //F0 9F 98 AD
                //.replace("\ud83d\ude29", ":((")  //F0 9F 98 A9
                //.replace("\ud83d\ude28", ":o")  //F0 9F 98 A8
                //.replace("\ud83d\ude10", ":|")  //F0 9F 98 90
                //                           
                //.replace("\ud83d\ude0c", "3)")  //F0 9F 98 8C
                //.replace("\ud83d\ude20", ">(")  //F0 9F 98 A0
                //.replace("\ud83d\ude21", ">((")  //F0 9F 98 A1
                //.replace("\ud83d\ude07", "O:)")  //F0 9F 98 87
                //.replace("\ud83d\ude30", ";o")  //F0 9F 98 B0
                //.replace("\ud83d\ude32", "8o")  //F0 9F 98 B2
                //.replace("\ud83d\ude33", "8|")  //F0 9F 98 B3
                //.replace("\ud83d\ude37", ":X")  //F0 9F 98 B7
                //                           
                //.replace("\ud83d\ude1a", ":*")  //F0 9F 98 9A
                //.replace("\ud83d\ude08", "}:)")  //F0 9F 98 88
                //.replace("\u2764", "<3")  //E2 9D A4   
                //.replace("\ud83d\udc4d", ":like:")  //F0 9F 91 8D
                //.replace("\ud83d\udc4e", ":dislike:")  //F0 9F 91 8E
                //.replace("\u261d", ":up:")  //E2 98 9D   
                //.replace("\u270c", ":v:")  //E2 9C 8C   
                //.replace("\ud83d\udc4c", ":ok:")  //F0 9F 91 8C
                ;
    }

    /*** API methods ***/
    //http://vk.com/developers.php?oid=-1&p=places.getCityById
    public ArrayList<City> getCities(Collection<Long> cids) throws MalformedURLException, IOException, JSONException, KException {
        if (cids == null || cids.size() == 0)
            return null;
        Params params = new Params("places.getCityById");
        params.put("cids",arrayToString(cids));
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        ArrayList<City> cities=new ArrayList<City>(); 
        if(array!=null){
            for(int i=0; i<array.length(); i++){
                JSONObject o = (JSONObject)array.get(i);
                City c = City.parse(o);
                cities.add(c);
            }
        }
        return cities;
    }

    <T> String arrayToString(Collection<T> items) {
        if(items==null)
            return null;
        String str_cids = "";
        for (Object item:items){
            if(str_cids.length()!=0)
                str_cids+=',';
            str_cids+=item;
        }
        return str_cids;
    }
    
    //http://vk.com/developers.php?oid=-1&p=places.getCountryById
    public ArrayList<Country> getCountries(Collection<Long> cids) throws MalformedURLException, IOException, JSONException, KException {
        if (cids == null || cids.size() == 0)
            return null;
        Params params = new Params("places.getCountryById");
        String str_cids = arrayToString(cids);
        params.put("cids",str_cids);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Country> countries=new ArrayList<Country>(); 
        int category_count = array.length();
        for(int i=0; i<category_count; i++){
            JSONObject o = (JSONObject)array.get(i);
            Country c = Country.parse(o);
            countries.add(c);
        }
        return countries;
    }

    //*** methods for users ***//
    //http://vk.com/developers.php?oid=-1&p=users.get
    public ArrayList<User> getProfiles(Collection<Long> uids, Collection<String> domains, String fields, String name_case, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        if (uids == null && domains == null)
            return null;
        if ((uids != null && uids.size() == 0) || (domains != null && domains.size() == 0))
            return null;
        Params params = new Params("users.get");
        if (uids != null && uids.size() > 0)
            params.put("uids",arrayToString(uids));
        if (domains != null && domains.size() > 0)
            params.put("uids",arrayToString(domains));
        if (fields == null)
            params.put("fields","uid,first_name,last_name,nickname,domain,sex,bdate,city,country,timezone,photo,photo_medium_rec,photo_big,has_mobile,rate,contacts,education,online");
        else
            params.put("fields",fields);
        params.put("name_case",name_case);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsers(array);
    }

    /*** methods for friends ***/
    //http://vkontakte.ru/developers.php?o=-1&p=friends.get
    public ArrayList<User> getFriends(Long user_id, String fields, Integer lid, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.get");
        if(fields==null)
            fields="first_name,last_name,photo_medium,online";
        params.put("fields",fields);
        params.put("uid",user_id);
        params.put("lid", lid);
        
        //сортировка по популярности не даёт запросить друзей из списка
        if(lid==null)
            params.put("order","hints");
        
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        ArrayList<User> users=new ArrayList<User>();
        JSONArray array=root.optJSONArray("response");
        //if there are no friends "response" will not be array
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parse(o);
            users.add(u);
        }
        return users;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=friends.getOnline
    public ArrayList<Long> getOnlineFriends(Long uid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.getOnline");
        params.put("uid",uid);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=likes.getList
    public ArrayList<Long> getLikeUsers(String item_type, long item_id, long owner_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("likes.getList");
        params.put("type",item_type);
        params.put("owner_id",owner_id);
        params.put("item_id",item_id);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        JSONArray array=response.optJSONArray("users");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=friends.getMutual
    public ArrayList<Long> getMutual(Long target_uid, Long source_uid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.getMutual");
        params.put("target_uid",target_uid);
        params.put("source_uid",source_uid);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }
    
    /*** methods for photos ***/
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getAlbums
    public ArrayList<Album> getAlbums(Long owner_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getAlbums");
        if (owner_id > 0)
            //user
            params.put("uid",owner_id);
        else
            //group
            params.put("gid",-owner_id);
        JSONObject root = sendRequest(params);
        ArrayList<Album> albums=new ArrayList<Album>();
        JSONArray array=root.optJSONArray("response");
        if(array==null)
            return albums;
        int category_count=array.length(); 
        for(int i=0; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            Album a = Album.parse(o);
            if(a.title.equals("DELETED"))
                continue;
            albums.add(a);
        }
        return albums;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.get
    public ArrayList<Photo> getPhotos(Long uid, Long aid, Integer offset, Integer count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.get");
        if(uid>0)
            params.put("uid", uid);
        else
            params.put("gid", -uid);
        params.put("aid", aid);
        params.put("extended", "1");
        params.put("offset",offset);
        params.put("limit",count);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getUserPhotos
    public ArrayList<Photo> getUserPhotos(Long uid, Integer offset, Integer count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getUserPhotos");
        params.put("uid", uid);
        params.put("sort","0");
        params.put("count",count);
        params.put("offset",offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/developers.php?oid=-1&p=photos.getAll
    public ArrayList<Photo> getAllPhotos(Long owner_id, Integer offset, Integer count, boolean extended) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getAll");
        params.put("owner_id", owner_id);
        params.put("offset", offset);
        params.put("count",count);
        params.put("extended",extended?1:0);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getUserPhotos
    public ArrayList<Photo> getUserPhotos(Long owner_id, Long offset, Long count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getUserPhotos");
        params.put("owner_id", owner_id);
        params.put("offset", offset);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Photo> photos=new ArrayList<Photo>(); 
        int category_count = array.length(); 
        for(int i=1; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            Photo p = Photo.parse(o);
            photos.add(p);
        }
        return photos;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=photos.getComments
    public CommentList getPhotoComments(Long pid, Long owner_id, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getComments");
        params.put("pid", pid);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        params.put("sort", "asc");
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        CommentList commnets = new CommentList();
        commnets.count=array.getInt(0);
        int category_count = array.length();
        for(int i = 1; i<category_count; ++i) { //get(0) is integer, it is comments count
            JSONObject o = (JSONObject)array.get(i);
            Comment comment = Comment.parsePhotoComment(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=notes.getComments
    public CommentList getNoteComments(Long nid, Long owner_id, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("notes.getComments");
        params.put("nid", nid);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        CommentList commnets = new CommentList();
        commnets.count=array.getInt(0);
        int category_count = array.length();
        for(int i = 1; i<category_count; ++i) { //get(0) is integer, it is comments count
            JSONObject o = (JSONObject)array.get(i);
            Comment comment = Comment.parseNoteComment(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=video.getComments
    public CommentList getVideoComments(long video_id, Long owner_id, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("video.getComments");
        params.put("vid", video_id);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        CommentList commnets = new CommentList();
        commnets.count=array.getInt(0);
        int category_count = array.length();
        for(int i = 1; i<category_count; ++i) { //get(0) is integer, it is comments count
            JSONObject o = (JSONObject)array.get(i);
            Comment comment = Comment.parseVideoComment(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }
    
    //Not used for now
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getAllComments
    public ArrayList<Comment> getAllPhotoComments(Long owner_id, Long album_id, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getAllComments");
        params.put("owner_id", owner_id);
        params.put("album_id", album_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        ArrayList<Comment> commnets = new ArrayList<Comment>();
        @SuppressWarnings("unused")
        JSONObject root = sendRequest(params);
        //здесь ещё приходит pid - photo_id
        //вынести парсящий код чтобы не было дублирования
        //JSONArray array = root.getJSONArray("response");
        //int category_count = array.length();
        //for(int i = 0; i<category_count; ++i) {           
        //    JSONObject o = (JSONObject)array.get(i);
        //    Comment comment = new Comment();
        //    comment.cid = Long.parseLong(o.getString("cid"));
        //    comment.from_id = Long.parseLong(o.getString("from_id"));
        //    comment.date = Long.parseLong(o.getString("date"));
        //    comment.message = unescape(o.getString("message"));
        //    commnets.add(comment);
        //}
        return commnets;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.createComment
    public long createPhotoComment(Long pid, Long owner_id, String message, Long reply_to_cid, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.createComment");
        params.put("pid",pid);
        params.put("owner_id",owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message",message);
        params.put("reply_to_cid", reply_to_cid);
        JSONObject root = sendRequest(params);
        long message_id = root.optLong("response");
        return message_id;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=notes.createComment
    public long createNoteComment(Long nid, Long owner_id, String message, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("notes.createComment");
        params.put("nid",nid);
        params.put("owner_id",owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message",message);
        //if (reply_to != null && !reply_to.equals(""))
        //    params.put("reply_to", reply_to);
        JSONObject root = sendRequest(params);
        long message_id = root.optLong("response");
        return message_id;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=video.createComment
    public long createVideoComment(Long video_id, Long owner_id, String message, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("video.createComment");
        params.put("vid",video_id);
        params.put("owner_id",owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message",message);
        JSONObject root = sendRequest(params);
        long message_id = root.optLong("response");
        return message_id;
    }
    
    private void addCaptchaParams(String captcha_key, String captcha_sid, Params params) {
        params.put("captcha_sid",captcha_sid);
        params.put("captcha_key",captcha_key);
    }

    /*** methods for messages 
     * @throws KException ***/
    //http://vkontakte.ru/developers.php?o=-1&p=messages.get
    public ArrayList<Message> getMessages(long time_offset, boolean is_out, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.get");
        if (is_out)
            params.put("out","1");
        if (time_offset!=0)
            params.put("time_offset", time_offset);
        if (count != 0)
            params.put("count", count);
        params.put("preview_length","0");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Message> messages = parseMessages(array, false, 0, false, 0);
        return messages;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=messages.getHistory
    public ArrayList<Message> getMessagesHistory(long uid, long chat_id, long me, Long offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.getHistory");
        if(chat_id<=0)
            params.put("uid",uid);
        else
            params.put("chat_id",chat_id);
        params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Message> messages = parseMessages(array, chat_id<=0, uid, chat_id>0, me);
        return messages;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=messages.getDialogs
    public ArrayList<Message> getMessagesDialogs(long offset, int count, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.getDialogs");
        if (offset!=0)
            params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        params.put("preview_length","0");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Message> messages = parseMessages(array, false, 0, false ,0);
        return messages;
    }

    private ArrayList<Message> parseMessages(JSONArray array, boolean from_history, long history_uid, boolean from_chat, long me) throws JSONException {
        ArrayList<Message> messages = new ArrayList<Message>();
        if (array != null) {
            int category_count = array.length();
            for(int i = 1; i < category_count; ++i) {
                JSONObject o = (JSONObject)array.get(i);
                Message m = Message.parse(o, from_history, history_uid, from_chat, me);
                messages.add(m);
            }
        }
        return messages;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=messages.send
    public String sendMessage(Long uid, long chat_id, String message, String title, String type, Collection<String> attachments, ArrayList<Long> forward_messages, String lat, String lon, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.send");
        if(chat_id<=0)
            params.put("uid", uid);
        else
            params.put("chat_id", chat_id);
        params.put("message", message);
        params.put("title", title);
        params.put("type", type); 
        params.put("attachment", arrayToString(attachments));
        params.put("forward_messages", arrayToString(forward_messages));
        params.put("lat", lat);
        params.put("long", lon);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        Object message_id = root.opt("response");
        if (message_id != null)
            return String.valueOf(message_id);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=messages.markAsNew
    //http://vkontakte.ru/developers.php?o=-1&p=messages.markAsRead
    public String markAsNewOrAsRead(ArrayList<Long> mids, boolean as_read) throws MalformedURLException, IOException, JSONException, KException{
        if (mids == null || mids.size() == 0)
            return null;
        Params params;
        if (as_read)
            params = new Params("messages.markAsRead");
        else
            params = new Params("messages.markAsNew");
        params.put("mids", arrayToString(mids));
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=messages.delete
    public String deleteMessage(Long mid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.delete");
        params.put("mid", mid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    /*** for status***/
    //http://vk.com/developers.php?oid=-1&p=status.get
    public VkStatus getStatus(Long uid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("status.get");
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        JSONObject obj = root.optJSONObject("response");
        VkStatus status = new VkStatus();
        if (obj != null) {
            status.text = unescape(obj.getString("text"));
            JSONObject jaudio = obj.optJSONObject("audio");
            if (jaudio != null) 
                status.audio = Audio.parse(jaudio);
        }
        return status;
    }

    //http://vk.com/developers.php?o=-1&p=status.set
    public String setStatus(String status_text, String audio) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("status.set");
        params.put("text", status_text);
        params.put("audio", audio); //oid_aid
        JSONObject root = sendRequest(params);
        Object response_id = root.opt("response");
        if (response_id != null)
            return String.valueOf(response_id);
        return null;
    }

    /*** methods for wall 
     * @throws KException ***/
    //http://vkontakte.ru/developers.php?o=-1&p=wall.get
    public ArrayList<WallMessage> getWallMessages(Long owner_id, int count, int offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.get");
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<WallMessage> wmessages = new ArrayList<WallMessage>();
        int category_count = array.length();
        for(int i = 1; i < category_count; ++i) {
            JSONObject o = (JSONObject)array.get(i);
            WallMessage wm = WallMessage.parse(o);
            wmessages.add(wm);
        }
        return wmessages;
    }

    /*** methods for news 
     * @throws KException ***/
    //http://vkontakte.ru/developers.php?o=-1&p=newsfeed.get
    //always returns about 33-35 items
    public Newsfeed getNews(long start_time, long count, long end_time, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("newsfeed.get");
        params.put("filters","post,photo,photo_tag,friend,note");
        params.put("start_time",(start_time==-1)?getDefaultStartTime():start_time);
        if(end_time!=-1)
            params.put("end_time",end_time);
        if(count!=0)
            params.put("count",count);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return Newsfeed.parse(root, false);
    }

    //Новости-Комментарии. Описания нет.
    public Newsfeed getNewsComments() throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("newsfeed.getComments");
        params.put("last_comments","1");
        params.put("count","50");
        JSONObject root = sendRequest(params);
        return Newsfeed.parse(root, true);
    }

    /*** for audio ***/
    //http://vkontakte.ru/developers.php?o=-1&p=audio.get
    public ArrayList<Audio> getAudio(Long uid, Long gid, Long album_id, Collection<Long> aids) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.get");
        params.put("uid", uid);
        params.put("gid", gid);
        params.put("aids", arrayToString(aids));
        params.put("album_id", album_id);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }
    
    //http://vk.com/developers.php?oid=-1&p=audio.getById
    public ArrayList<Audio> getAudioById(String audios) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.getById");
        params.put("audios", audios);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }
    
    public String getLyrics(Long id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.getLyrics");
        params.put("lyrics_id", id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optString("text");
    }
    
    /*** for video ***/
    //http://vkontakte.ru/developers.php?o=-1&p=video.get //width = 130,160,320
    public ArrayList<Video> getVideo(String videos, Long owner_id, Long album_id, String width, Long count, Long offset, String access_key) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("video.get");
        params.put("videos", videos);
        if (owner_id != null){
            if(owner_id>0)
                params.put("uid", owner_id);
            else
                params.put("gid", -owner_id);
        }
        params.put("width", width);
        params.put("count", count);
        params.put("offset", offset);
        params.put("aid", album_id);
        params.put("access_key", access_key);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Video> videoss = new ArrayList<Video>();
        if (array != null) {
            for(int i = 1; i<array.length(); ++i) {
                JSONObject o = (JSONObject)array.get(i);
                Video video = Video.parse(o);
                videoss.add(video);
            }
        }
        return videoss;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=video.getUserVideos
    public ArrayList<Video> getUserVideo(Long user_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("video.getUserVideos");
        params.put("uid", user_id);
        params.put("count", "50");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Video> videos = new ArrayList<Video>();
        if (array != null) {
            for(int i = 1; i<array.length(); ++i) {
                JSONObject o = (JSONObject)array.get(i); 
                videos.add(Video.parse(o));
            }
        }
        return videos;
    }
    
    /*** for crate album ***/
    //http://vkontakte.ru/developers.php?o=-1&p=photos.createAlbum
    public Album createAlbum(String title, String privacy, String comment_privacy, String description) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.createAlbum");
        params.put("title", title);
        params.put("privacy", privacy);
        params.put("comment_privacy", comment_privacy);
        params.put("description", description);
        JSONObject root = sendRequest(params);
        JSONObject o = root.optJSONObject("response");
        if (o == null)
            return null; 
        return Album.parse(o);
    }
    
    //http://vk.com/developers.php?oid=-1&p=photos.editAlbum
    public String editAlbum(long aid, String title, String privacy, String comment_privacy, String description) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.editAlbum");
        params.put("aid", String.valueOf(aid));
        params.put("title", title);
        params.put("privacy", privacy);
        params.put("comment_privacy", comment_privacy);
        params.put("description", description);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    /*** for notes ***/
    //http://vkontakte.ru/developers.php?o=-1&p=notes.get
    public ArrayList<Note> getNotes(Long uid, Collection<Long> nids, String sort, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("notes.get");
        params.put("uid", uid);
        params.put("nids", arrayToString(nids));
        params.put("sort", sort);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Note> notes = new ArrayList<Note>();
        if (array != null) {
            for(int i = 1; i<array.length(); ++i) {
                JSONObject o = (JSONObject)array.get(i);
                Note note = Note.parse(o, true);
                notes.add(note);
            }
        }
        return notes;
    }

    //http://vk.com/developers.php?oid=-1&p=notes.delete
    public String deleteNote(Long nid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("notes.delete");
        params.put("nid", nid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=photos.getUploadServer
    public String photosGetUploadServer(long album_id, Long group_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.getUploadServer");
        params.put("aid",album_id);
        params.put("gid",group_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getWallUploadServer
    public String photosGetWallUploadServer(Long user_id, Long group_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.getWallUploadServer");
        params.put("uid",user_id);
        params.put("gid",group_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=getAudioUploadServer
    public String getAudioUploadServer() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("getAudioUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=photos.getMessagesUploadServer
    public String photosGetMessagesUploadServer() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.getMessagesUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getProfileUploadServer
    public String photosGetProfileUploadServer() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.getProfileUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.save
    public ArrayList<Photo> photosSave(String server, String photos_list, Long aid, Long group_id, String hash, String caption) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.save");
        params.put("server",server);
        params.put("photos_list",photos_list);
        params.put("aid",aid);
        params.put("gid",group_id);
        params.put("hash",hash);
        params.put("caption",caption);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.saveWallPhoto
    public ArrayList<Photo> saveWallPhoto(String server, String photo, String hash, Long user_id, Long group_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.saveWallPhoto");
        params.put("server",server);
        params.put("photo",photo);
        params.put("hash",hash);
        params.put("uid",user_id);
        params.put("gid",group_id);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vk.com/developers.php?oid=-1&p=audio.save
    public Audio saveAudio(String server, String audio, String hash, String artist, String title) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("audio.save");
        params.put("server",server);
        params.put("audio",audio);
        params.put("hash",hash);
        params.put("artist",artist);
        params.put("title",title);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        return Audio.parse(response);
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=photos.saveMessagesPhoto
    public ArrayList<Photo> saveMessagesPhoto(String server, String photo, String hash) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.saveMessagesPhoto");
        params.put("server",server);
        params.put("photo",photo);
        params.put("hash",hash);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.saveProfilePhoto
    public String[] saveProfilePhoto(String server, String photo, String hash) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.saveProfilePhoto");
        params.put("server",server);
        params.put("photo",photo);
        params.put("hash",hash);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        String src = response.optString("photo_src");
        String hash1 = response.optString("photo_hash");
        String[] res=new String[]{src, hash1};
        return res;
    }

    private ArrayList<Photo> parsePhotos(JSONArray array) throws JSONException {
        ArrayList<Photo> photos=new ArrayList<Photo>(); 
        int category_count=array.length(); 
        for(int i=0; i<category_count; ++i){
            //in getUserPhotos first element is integer
            if(array.get(i) instanceof JSONObject == false)
                continue;
            JSONObject o = (JSONObject)array.get(i);
            Photo p = Photo.parse(o);
            photos.add(p);
        }
        return photos;
    }
    
    /*public long createGraffitiComment(String gid, String owner_id, String message, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("graffiti.createComment");
        params.put("gid",gid);
        params.put("owner_id",owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message",message);
        JSONObject root = sendRequest(params);
        long message_id = root.optLong("response");
        return message_id;
    }*/
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.addComment
    public long createWallComment(Long owner_id, Long post_id, String text, Long reply_to_cid, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.addComment");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        params.put("text", text);
        params.put("reply_to_cid", reply_to_cid);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        long cid = response.optLong("cid");
        return cid;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.post
    public long createWallPost(long owner_id, String text, Collection<String> attachments, String export, boolean only_friends, boolean from_group, boolean signed, String lat, String lon, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.post");
        params.put("owner_id", owner_id);
        params.put("attachments", arrayToString(attachments));
        params.put("lat", lat);
        params.put("long", lon);
        params.put("message", text);
        if(export!=null && export.length()!=0)
            params.put("services",export);
        if (from_group)
            params.put("from_group","1");
        if (only_friends)
            params.put("friends_only","1");
        if (signed)
            params.put("signed","1");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        JSONObject response = root.getJSONObject("response");
        long post_id = response.optLong("post_id");
        return post_id;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.getComments
    public CommentList getWallComments(Long owner_id, Long post_id, int offset, int count, String v) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.getComments");
        params.put("post_id", post_id);
        params.put("owner_id", owner_id);
        /*
        if (sort != null)
            params.put("sort", sort);
            //asc - хронологический
            //desc - антихронологический
        */
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);
        params.put("preview_length", "0");
        params.put("need_likes", "1");
        params.put("v", v);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        CommentList commnets = new CommentList();
        commnets.count=array.getInt(0);
        int category_count = array.length();
        for(int i = 1; i<category_count; ++i) //get(0) is integer, it is comments count
            commnets.comments.add(Comment.parse((JSONObject)array.get(i)));
        return commnets;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=audio.search
    public ArrayList<Audio> searchAudio(String q, String sort, String lyrics, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.search");
        params.put("q", q);
        params.put("sort", sort);
        params.put("lyrics", lyrics);
        params.put("count", count);
        params.put("offset", offset);
        params.put("auto_complete", "1");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 1);
    }

    private ArrayList<Audio> parseAudioList(JSONArray array, int type_array) //type_array must be 0 or 1
            throws JSONException {
        ArrayList<Audio> audios = new ArrayList<Audio>();
        if (array != null) {
            for(int i = type_array; i<array.length(); ++i) { //get(0) is integer, it is audio count
                JSONObject o = (JSONObject)array.get(i);
                audios.add(Audio.parse(o));
            }
        }
        return audios;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=audio.delete
    public String deleteAudio(Long aid, Long oid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.delete");
        params.put("aid", aid);
        params.put("oid", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=audio.add
    public String addAudio(Long aid, Long oid, Long gid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.add");
        params.put("aid", aid);
        params.put("oid", oid);
        params.put("gid", gid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.addLike
    public Long wallAddLike(Long owner_id, Long post_id, boolean need_publish, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.addLike");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        params.put("need_publish", need_publish?"1":"0");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes=response.optLong("likes", -1);
        return likes;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.deleteLike
    public Long wallDeleteLike(Long owner_id, Long post_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.deleteLike");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes=response.optLong("likes", -1);
        return likes;
    }
    
    //http://vk.com/developers.php?oid=-1&p=likes.add
    public Long addLike(Long owner_id, Long item_id, String type, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("likes.add");
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("type", type);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes=response.optLong("likes", -1);
        return likes;
    }
    
    //http://vk.com/developers.php?oid=-1&p=likes.delete
    public Long deleteLike(Long owner_id, Long item_id, String type) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("likes.delete");
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("type", type);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes=response.optLong("likes", -1);
        return likes;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=likes.add
    public Long addLike(Long owner_id, String type, Long item_id, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("likes.add");
        params.put("owner_id", owner_id);
        params.put("type", type);
        params.put("item_id", item_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optLong("likes", -1);
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=likes.delete
    public Long deleteLike(Long owner_id, String type, Long item_id, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("likes.delete");
        params.put("owner_id", owner_id);
        params.put("type", type);
        params.put("item_id", item_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optLong("likes", -1);
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.getById
    public ArrayList<Photo> getPhotosById(String photos) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getById");
        params.put("photos", photos);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos1 = parsePhotos(array);
        return photos1;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=groups.get
    public ArrayList<Group> getUserGroups(Long user_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("groups.get");
        params.put("extended","1");
        params.put("uid",user_id);
        JSONObject root = sendRequest(params);
        ArrayList<Group> groups=new ArrayList<Group>();
        JSONArray array=root.optJSONArray("response");
        //if there are no groups "response" will not be array
        if(array==null)
            return groups;
        groups = Group.parseGroups(array);
        return groups;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=wall.delete
    public Boolean removeWallPost(Long post_id, long wall_owner_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("wall.delete");
        params.put("owner_id", wall_owner_id);
        params.put("post_id", post_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=wall.deleteComment
    public Boolean deleteWallComment(Long wall_owner_id, long comment_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("wall.deleteComment");
        params.put("owner_id", wall_owner_id);
        params.put("cid", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=notes.deleteComment
    public Boolean deleteNoteComment(Long note_owner_id, long comment_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("notes.deleteComment");
        params.put("owner_id", note_owner_id);
        params.put("cid", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=video.deleteComment
    public Boolean deleteVideoComment(Long video_owner_id, long comment_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("video.deleteComment");
        params.put("owner_id", video_owner_id);
        params.put("cid", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=photos.deleteComment
    public Boolean deletePhotoComment(long photo_id, Long photo_owner_id, long comment_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.deleteComment");
        params.put("owner_id", photo_owner_id);
        params.put("cid", comment_id);
        params.put("pid", photo_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=video.search
    public ArrayList<Video> searchVideo(String q, String sort, String hd, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("video.search");
        params.put("q", q);
        params.put("sort", sort);
        params.put("hd", hd);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Video> videoss = new ArrayList<Video>();
        if (array != null) {
            for(int i = 0; i<array.length(); ++i) {
                JSONObject o = (JSONObject)array.get(i);
                Video video = Video.parse(o);
                videoss.add(video);
            }
        }
        return videoss;
    }
    
    //no documentation
    public ArrayList<User> searchUser(String q, String fields, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("users.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsers(array);
    }

    //http://vkontakte.ru/developers.php?o=-1&p=video.delete
    public String deleteVideo(Long vid, Long oid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("video.delete");
        params.put("vid", vid);
        params.put("oid", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=video.add
    public String addVideo(Long vid, Long oid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("video.add");
        params.put("vid", vid);
        params.put("oid", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vkontakte.ru/developers.php?o=-1&p=notes.add
    public long createNote(String title, String text) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("notes.add");
        params.put("title", title);
        params.put("text", text);
        params.put("privacy", "0");
        params.put("comment_privacy", "0");
        JSONObject root = sendRequest(params, true);
        JSONObject response = root.getJSONObject("response");
        long note_id = response.optLong("nid");
        return note_id;
    }
    
    //http://vkontakte.ru/developers.php?o=-1&p=messages.getLongPollServer
    public Object[] getLongPollServer(String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.getLongPollServer");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        String key=response.getString("key");
        String server=response.getString("server");
        Long ts = response.getLong("ts");
        return new Object[]{key, server, ts};
    }
    
    //не документирован
    public void setOnline(String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("activity.online");
        addCaptchaParams(captcha_key, captcha_sid, params);
        sendRequest(params);
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=friends.add
    public long addFriend(Long uid, String text, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.add");
        params.put("uid", uid);
        params.put("text", text);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=friends.delete
    public long deleteFriend(Long uid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.delete");
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=friends.getRequests
    public ArrayList<Object[]> getRequestsFriends() throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("friends.getRequests");
        params.put("need_messages", "1");
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        ArrayList<Object[]> users=new ArrayList<Object[]>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    Long id = item.optLong("uid", -1);
                    if (id!=-1) {
                        Object[] u = new Object[2];
                        u[0] = id;
                        u[1] = item.optString("message");
                        users.add(u);
                    }
                }
            }
        }
        return users;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=subscriptions.follow
    public String followUser(Long uid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("subscriptions.follow");
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=subscriptions.unfollow
    public String unfollowUser(Long uid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("subscriptions.unfollow");
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=subscriptions.get
    public ArrayList<Long> getSubscriptions(Long uid, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("subscriptions.get");
        params.put("uid", uid);
        if (offset>0)
            params.put("offset", offset);
        if (count>0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        JSONArray array=response.optJSONArray("users");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }

    
    //http://vkontakte.ru/developers.php?oid=-1&p=subscriptions.getFollowers
    public ArrayList<Long> getFollowers(Long uid, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("subscriptions.getFollowers");
        params.put("uid", uid);
        if (offset>0)
            params.put("offset", offset);
        if (count>0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        JSONArray array=response.optJSONArray("users");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vkontakte.ru/pages?oid=-1&p=messages.deleteDialog
    public int deleteMessageThread(Long uid, Long chatId) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.deleteDialog");
        params.put("uid", uid);
        params.put("chat_id", chatId);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=execute
    public void execute(String code) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("execute");
        params.put("code", code);
        sendRequest(params);
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=photos.delete
    public boolean deletePhoto(Long owner_id, Long photo_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.delete");
        params.put("oid", owner_id);
        params.put("pid", photo_id);
        JSONObject root = sendRequest(params);
        long response = root.optLong("response", -1);
        return response==1;
    }

    //http://vkontakte.ru/developers.php?oid=-1&p=polls.getById
    public VkPoll getPoll(long poll_id, long owner_id) throws JSONException, MalformedURLException, IOException, KException {
        Params params = new Params("polls.getById");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return VkPoll.parse(response);
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=polls.addVote
    public int addPollVote(long poll_id, long answer_id, long owner_id, String captcha_key, String captcha_sid) throws JSONException, MalformedURLException, IOException, KException {
        Params params = new Params("polls.addVote");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        params.put("answer_id", answer_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }

    //http://vkontakte.ru/developers.php?oid=-1&p=polls.deleteVote
    public int deletePollVote(long poll_id, long answer_id, long owner_id) throws JSONException, MalformedURLException, IOException, KException {
        Params params = new Params("polls.deleteVote");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        params.put("answer_id", answer_id);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=friends.getLists
    public ArrayList<FriendsList> friendsLists() throws JSONException, MalformedURLException, IOException, KException {
        Params params = new Params("friends.getLists");
        JSONObject root = sendRequest(params);
        ArrayList<FriendsList> result = new ArrayList<FriendsList>();
        JSONArray list = root.optJSONArray("response");
        if (list != null) {
            for (int i=0; i<list.length(); ++i) {
                JSONObject o = list.getJSONObject(i);
                FriendsList fl = FriendsList.parse(o);
                result.add(fl);
            }
        }
        return result;
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=video.save
    public String saveVideo(String name, String description, Long gid, int privacy_view, int privacy_comment) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("video.save");
        params.put("name", name);
        params.put("description", description);
        params.put("gid", gid);
        if (privacy_view > 0)
            params.put("privacy_view", privacy_view);
        if (privacy_comment > 0)
            params.put("privacy_comment", privacy_comment);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vkontakte.ru/developers.php?oid=-1&p=photos.deleteAlbum
    public String deleteAlbum(Long aid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.deleteAlbum");
        params.put("aid", aid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //http://vk.com/developers.php?o=-1&p=photos.getTags
    public ArrayList<PhotoTag> getPhotoTagsById(Long pid, Long owner_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("photos.getTags");
        params.put("owner_id", owner_id);
        params.put("pid", pid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<PhotoTag>(); 
        ArrayList<PhotoTag> photo_tags = parsePhotoTags(array, pid, owner_id);
        return photo_tags;
    }
    
    private ArrayList<PhotoTag> parsePhotoTags(JSONArray array, Long pid, Long owner_id) throws JSONException {
        ArrayList<PhotoTag> photo_tags=new ArrayList<PhotoTag>(); 
        int category_count=array.length(); 
        for(int i=0; i<category_count; ++i){
            //in getUserPhotos first element is integer
            if(array.get(i) instanceof JSONObject == false)
                continue;
            JSONObject o = (JSONObject)array.get(i);
            PhotoTag p = PhotoTag.parse(o);
            photo_tags.add(p);
            if (pid != null)
                p.pid = pid;
            if (owner_id != null)
                p.owner_id = owner_id;
        }
        return photo_tags;
    }
    
    //http://vk.com/developers.php?oid=-1&p=photos.putTag
    public String putPhotoTag(PhotoTag ptag, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException {
        if (ptag == null)
            return null;
        Params params = new Params("photos.putTag");
        params.put("owner_id", ptag.owner_id);
        params.put("pid", ptag.pid);
        params.put("uid", ptag.uid);
        params.putDouble("x", ptag.x);
        params.putDouble("x2", ptag.x2);
        params.putDouble("y", ptag.y);
        params.putDouble("y2", ptag.y2);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    /*** topics region ***/
    //http://kate1.unfuddle.com/a#/projects/2/tickets/by_number/340?cycle=true
    public ArrayList<GroupTopic> getGroupTopics(long gid, int extended, int count, int offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("board.getTopics");
        params.put("gid", gid);
        if (extended == 1)
            params.put("extended", "1"); //for profiles
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<GroupTopic> result = new ArrayList<GroupTopic>();
        JSONObject response = root.optJSONObject("response");
        if (response != null) {
            JSONArray topics = response.optJSONArray("topics");
            if (topics != null) {
                for (int i=1; i<topics.length(); ++i) {
                    JSONObject o = topics.getJSONObject(i);
                    GroupTopic gt = GroupTopic.parse(o);
                    gt.gid = gid;
                    result.add(gt);
                }
            }
        }
        return result;
    }
    
    public CommentList getGroupTopicComments(long gid, long tid, int photo_sizes, int extended, int count, int offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("board.getComments");
        params.put("gid", gid);
        params.put("tid", tid);
        if (photo_sizes == 1)
            params.put("photo_sizes", "1");
        if (extended == 1)
            params.put("extended", "1");
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        CommentList result = new CommentList();
        if (response != null) {
            JSONArray comments = response.optJSONArray("comments");
            int category_count = comments.length();
            result.count=comments.getInt(0);
            for (int i=1; i<category_count; ++i) { //get(0) is integer, it is comments count
                JSONObject o = comments.getJSONObject(i);
                Comment c = Comment.parseTopicComment(o);
                result.comments.add(c);
            }
        }
        return result;
    }
    
    public long createGroupTopicComment(long gid, long tid, String text, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("board.addComment");
        params.put("gid", gid);
        params.put("tid", tid);
        params.put("text", text);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        long message_id = root.optLong("response");
        return message_id;
    }
    
    public Boolean deleteGroupTopicComment(long gid, long tid, long cid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("board.deleteComment");
        params.put("gid", gid);
        params.put("tid", tid);
        params.put("cid", cid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    public long createGroupTopic(long gid, String title, String text, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("board.addTopic");
        params.put("gid", gid);
        params.put("title", title);
        params.put("text", text);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        long topic_id = root.optLong("response");
        return topic_id;
    }
    
    public Boolean deleteGroupTopic(long gid, long tid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("board.deleteTopic");
        params.put("gid", gid);
        params.put("tid", tid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    /*** end topics region ***/
    
    //http://vk.com/developers.php?oid=-1&p=groups.getById
    public ArrayList<Group> getGroups(Collection<Long> uids, String domain, String fields) throws MalformedURLException, IOException, JSONException, KException{
        if (uids == null && domain == null)
            return null;
        if (uids.size() == 0 && domain == null)
            return null;
        Params params = new Params("groups.getById");
        String str_uids;
        if (uids != null && uids.size() > 0)
            str_uids=arrayToString(uids);
        else
            str_uids = domain;
        params.put("gids", str_uids);
        params.put("fields", fields); //Possible values: place,wiki_page,city,country,description,start_date,finish_date,site
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return Group.parseGroups(array);
    }

    //no documentation
    public String joinGroup(long gid, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("groups.join");
        params.put("gid", gid);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //no documentation
    public String leaveGroup(long gid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("groups.leave");
        params.put("gid", gid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    
    //no documentation
    public ArrayList<Group> searchGroup(String q, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("groups.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        ArrayList<Group> groups = new ArrayList<Group>();  
        //if there are no groups "response" will not be array
        if (array==null)
            return groups;
        groups = Group.parseGroups(array);
        return groups;
    }
    
    //http://vk.com/pages?oid=-1&p=account.registerDevice
    public String registerDevice(String token, String device_model, String system_version, Integer no_text, String subscribe)
            throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("account.registerDevice");
        params.put("token", token);
        params.put("device_model", device_model);
        params.put("system_version", system_version);
        params.put("no_text", no_text);
        params.put("subscribe", subscribe);
        JSONObject root = sendRequest(params);
        return root.getString("response");
    }
    
    //http://vk.com/pages?oid=-1&p=account.unregisterDevice
    public String unregisterDevice(String token) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("account.unregisterDevice");
        params.put("token", token);
        JSONObject root = sendRequest(params);
        return root.getString("response");
    }
    
    //http://vk.com/developers.php?oid=-1&p=notifications.get
    public Notifications getNotifications(String filters, Long start_time, Long end_time, Integer offset, Integer count) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("notifications.get");
        params.put("filters", filters);
        params.put("start_time", start_time);
        params.put("end_time", end_time);
        params.put("offset", offset);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return Notifications.parse(response);
    }
    
    //http://vk.com/developers.php?oid=-1&p=notifications.markAsViewed
    public String resetUnreadNotifications() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("notifications.markAsViewed");
        JSONObject root = sendRequest(params);
        return root.getString("response");
    }
    
    //http://vk.com/developers.php?oid=-1&p=messages.getById
    public ArrayList<Message> getMessagesById(ArrayList<Long> message_ids) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.getById");
        params.put("mids", arrayToString(message_ids));
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Message> messages = parseMessages(array, false, 0, false, 0);
        return messages;
    }
    
    public Counters getCounters(String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("getCounters");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response=root.optJSONObject("response");
        return Counters.parse(response);
    }
    
    /*** faves ***/
    //http://vk.com/developers.php?oid=-1&p=fave.getUsers
    public ArrayList<User> getFaveUsers(String fields, Integer count, Integer offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("fave.getUsers");
        if(fields==null)
            fields="photo_medium,online";
        params.put("fields",fields);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<User> users=new ArrayList<User>();
        JSONArray array=root.optJSONArray("response");
        //if there are no friends "response" will not be array
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i) {
            if(array.get(i)==null || ((array.get(i) instanceof JSONObject)==false))
                continue;
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parseFromFave(o);
            users.add(u);
        }
        return users;
    }
    
    //http://vk.com/developers.php?oid=-1&p=fave.getPhotos
    public ArrayList<Photo> getFavePhotos(Integer count, Integer offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("fave.getPhotos");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vk.com/developers.php?oid=-1&p=fave.getVideos
    public ArrayList<Video> getFaveVideos(Integer count, Integer offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("fave.getVideos");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Video> videos = new ArrayList<Video>();
        if (array != null) {
            for(int i = 1; i<array.length(); ++i) {
                JSONObject o = (JSONObject)array.get(i);
                Video video = Video.parse(o);
                videos.add(video);
            }
        }
        return videos;
    }
    
    //http://vk.com/developers.php?oid=-1&p=fave.getPosts
    public ArrayList<WallMessage> getFavePosts(Integer count, Integer offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("fave.getPosts");
        //params.put("extended", extended);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        //JSONArray array = response.optJSONArray("wall");
        //JSONArray profiles_array = response.optJSONArray("profiles");
        //JSONArray groups_array = response.optJSONArray("groups");
        ArrayList<WallMessage> wmessages = new ArrayList<WallMessage>();
        if (array == null)
            return wmessages;
        int category_count = array.length();
        for(int i = 1; i < category_count; ++i) {
            JSONObject o = (JSONObject)array.get(i);
            WallMessage wm = WallMessage.parse(o);
            wmessages.add(wm);
        }
        return wmessages;
    }
    
    //http://vk.com/developers.php?oid=-1&p=fave.getLinks
    public ArrayList<Link> getFaveLinks(Integer count, Integer offset) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("fave.getLinks");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<Link> groups=new ArrayList<Link>();
        JSONArray array=root.optJSONArray("response");
        //if there are no groups "response" will not be array
        if(array==null)
            return groups;
        for(int i = 0; i < array.length(); i++) {
            if(!(array.get(i) instanceof JSONObject))
                continue;
            JSONObject jlink = (JSONObject)array.get(i);
            Link link = Link.parse(jlink);
            groups.add(link);
        }
        return groups;
    }
    /*** end faves  ***/
    
    /*** chat methods ***/
    //http://vk.com/pages?oid=-1&p=messages.createChat
    public Long chatCreate(ArrayList<Long> uids, String title) throws MalformedURLException, IOException, JSONException, KException {
        if (uids == null || uids.size() == 0)
            return null;
        Params params = new Params("messages.createChat");
        String str_uids = String.valueOf(uids.get(0));
        for (int i=1; i<uids.size(); i++)
            str_uids += "," + String.valueOf(uids.get(i));
        params.put("uids", str_uids);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }
    
    //http://vk.com/pages?oid=-1&p=messages.editChat
    public Integer chatEdit(long chat_id, String title) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.editChat");
        params.put("chat_id", chat_id);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/pages?oid=-1&p=messages.getChatUsers
    public ArrayList<User> getChatUsers(long chat_id, String fields) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.getChatUsers");
        params.put("chat_id", chat_id);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsers(array);
    }
    
    //http://vk.com/pages?oid=-1&p=messages.addChatUser
    public Integer addUserToChat(long chat_id, long uid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.addChatUser");
        params.put("chat_id", chat_id);
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/pages?oid=-1&p=messages.removeChatUser
    public Integer removeUserFromChat(long chat_id, long uid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.removeChatUser");
        params.put("chat_id", chat_id);
        params.put("uid", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    /*** end chat methods ***/
    
    //http://vk.com/pages?oid=-1&p=friends.getSuggestions
    public ArrayList<User> getSuggestions(String filter, String fields) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("friends.getSuggestions");
        params.put("filter", filter);   //mutual, contacts, mutual_contacts
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsers(array);
    }
    
    //http://vk.com/pages?oid=-1&p=account.importContacts
    public Integer importContacts(Collection<String> contacts) throws MalformedURLException, IOException, JSONException, KException    {
        Params params = new Params("account.importContacts");
        params.put("contacts", arrayToString(contacts));
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/pages?oid=-1&p=friends.getByPhones
    public ArrayList<User> getFriendsByPhones(ArrayList<String> phones, String fields) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("friends.getByPhones");
        params.put("phones", arrayToString(phones));
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsersForGetByPhones(array);
    }
    
    /*** methods for messages search ***/      
    //http://vk.com/pages?oid=-1&p=messages.search
    public ArrayList<Message> searchMessages(String q, int offset, int count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Message> messages = parseMessages(array, false, 0, false, 0);
        return messages;
    }
    
    //http://vk.com/pages?oid=-1&p=messages.searchDialogs
    public ArrayList<SearchDialogItem> searchDialogs(String q, String fields) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.searchDialogs");
        params.put("q", q);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return Message.parseSearchedDialogs(array);
    }
    
    //http://vk.com/pages?oid=-1&p=messages.getLastActivity
    public LastActivity getLastActivity(long user_id) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("messages.getLastActivity");
        params.put("uid", user_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return LastActivity.parse(response);
    }
    
    //http://vk.com/developers.php?oid=-1&p=groups.getMembers
    public ArrayList<Long> getGroupsMembers(long gid, Integer count, Integer offset, String sort) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("groups.getMembers");
        params.put("gid", gid);
        params.put("count", count);
        params.put("offset", offset);
        params.put("sort", sort); //id_asc, id_desc, time_asc, time_desc
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        JSONArray array=response.optJSONArray("users");
        ArrayList<Long> users=new ArrayList<Long>();
        if (array != null) {
            int category_count=array.length();
            for(int i=0; i<category_count; ++i){
                Long id = array.optLong(i, -1);
                if(id!=-1)
                    users.add(id);
            }
        }
        return users;
    }
    
    //http://vk.com/developers.php?oid=-1&p=groups.getMembers
    public Long getGroupsMembersCount(long gid) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("groups.getMembers");
        params.put("gid", gid);
        params.put("count", 10);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        return response.optLong("count");
    }
    
    public ArrayList<User> getGroupsMembersWithExecute(long gid, Integer count, Integer offset, String sort, String fields) throws MalformedURLException, IOException, JSONException, KException {
        //String code = "return API.getProfiles({\"uids\":API.groups.getMembers({\"gid\":" + String.valueOf(gid) + ",\"count\":" + String.valueOf(count) + ",\"offset\":" + String.valueOf(offset) + ",\"sort\":\"id_asc\"}),\"fields\":\"" + fields + "\"});";
        String code = "var members=API.groups.getMembers({\"gid\":" + gid + "}); var u=members[1]; return API.getProfiles({\"uids\":u,\"fields\":\"" + fields + "\"});";
        Params params = new Params("execute");
        params.put("code", code);
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return User.parseUsers(array);
    }
    
    //http://vk.com/developers.php?oid=-1&p=getServerTime
    public long getServerTime() throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("getServerTime");
        JSONObject root = sendRequest(params);
        return root.getLong("response");
    }
    
    //http://vk.com/developers.php?oid=-1&p=audio.getAlbums
    public ArrayList<AudioAlbum> getAudioAlbums(Long uid, Long gid, Integer offset, Integer count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.getAlbums");
        params.put("uid", uid);
        params.put("gid", gid);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<AudioAlbum> albums = AudioAlbum.parseAlbums(array);
        return albums;
    }
    
    public ArrayList<Audio> getAudioRecommendations() throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("audio.getRecommendations");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }
    
    //http://vk.com/developers.php?oid=-1&p=video.getAlbums
    public ArrayList<AudioAlbum> getVideoAlbums(Long uid, Long gid, Integer offset, Integer count) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("video.getAlbums");
        params.put("uid", uid);
        params.put("gid", gid);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<AudioAlbum> albums = AudioAlbum.parseAlbums(array);
        return albums;
    }
    
    //http://vk.com/developers.php?oid=-1&p=messages.setActivity
    public Integer setMessageActivity(long uid, long chat_id, boolean typing) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("messages.setActivity");
        params.put("uid", uid);
        params.put("chat_id", chat_id);
        if (typing)
            params.put("type", "typing");
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/developers.php?oid=-1&p=wall.edit
    public int editWallPost(long owner_id, long post_id, String text, Collection<String> attachments, String lat, String lon, long place_id, String captcha_key, String captcha_sid) throws MalformedURLException, IOException, JSONException, KException{
        Params params = new Params("wall.edit");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        params.put("message", text);
        params.put("attachments", arrayToString(attachments));
        params.put("lat", lat);
        params.put("long", lon);
        params.put("place_id", place_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/developers.php?oid=-1&p=photos.edit
    public Integer photoEdit(Long owner_id, long pid, String caption) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("photos.edit");
        params.put("owner_id", owner_id);
        params.put("pid", pid);
        params.put("caption", caption);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/developers.php?oid=-1&p=docs.get
    public ArrayList<Document> getDocs(Long owner_id, Long count, Long offset) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("docs.get");
        params.put("oid", owner_id);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return Document.parseDocs(array);
    }
    
    //http://vk.com/developers.php?oid=-1&p=docs.getUploadServer
    public String docsGetUploadServer() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("docs.getUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }
    
    //http://vk.com/developers.php?oid=-1&p=docs.save
    public Document saveDoc(String file) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("docs.save");
        params.put("file", file);
        JSONObject root = sendRequest(params);
        JSONArray array=root.getJSONArray("response");
        ArrayList<Document> docs = Document.parseDocs(array);
        return docs.get(0);
    }
    
    //http://vk.com/developers.php?oid=-1&p=docs.delete
    public Boolean deleteDoc(Long doc_id, long owner_id) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("docs.delete");
        params.put("oid", owner_id);
        params.put("did", doc_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //Помечает уведомления о новых ответах как прочитанные, документации нет
    public Boolean markNotificationsAsViewed() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("notifications.markAsViewed");
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vk.com/developers.php?oid=-1&p=newsfeed.getBanned
    public BannArg getBanned(boolean is_extended, String fields) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("newsfeed.getBanned");
        if (is_extended)
            params.put("extended", "1");
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONObject object = root.optJSONObject("response");
        return BannArg.parse(object);
    }
    
    //http://vk.com/developers.php?oid=-1&p=newsfeed.addBan
    public Boolean addBan(Collection<Long> uids, Collection<Long> gids) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("newsfeed.addBan");
        if (uids != null && uids.size() > 0)
            params.put("uids",arrayToString(uids));
        if (gids != null && gids.size() > 0)
            params.put("gids",arrayToString(gids));
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //http://vk.com/developers.php?oid=-1&p=newsfeed.deleteBan
    public Boolean deleteBan(Collection<Long> uids, Collection<Long> gids) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("newsfeed.deleteBan");
        if (uids != null && uids.size() > 0)
            params.put("uids",arrayToString(uids));
        if (gids != null && gids.size() > 0)
            params.put("gids",arrayToString(gids));
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response==1;
    }
    
    //gets status of broadcasting user current audio to his page
    public boolean audioGetBroadcast() throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("audio.getBroadcast");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optInt("enabled")==1;
    }

    //enables broadcasting user current audio to his page
    public boolean audioSetBroadcast(boolean enabled) throws MalformedURLException, IOException, JSONException, KException {
        Params params = new Params("audio.setBroadcast");
        params.put("enabled",enabled?"1":"0");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optInt("enabled")==1;
    }
}