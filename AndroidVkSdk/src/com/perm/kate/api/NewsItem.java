package com.perm.kate.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsItem {
    public String type;
    public long source_id;
    public long from_id;//когда пост приходит в комментариях, то в source_id там стена, а в from_id автор сообщения.
    public long date;
    public long post_id;
    public long copy_owner_id;
    public long copy_post_id;
    public String copy_text;
    public String text;

    //likes
    public int like_count;
    public boolean user_like;

    //comments
    public int comment_count;
    public boolean comment_can_post;

    public ArrayList<Attachment> attachments=new ArrayList<Attachment>();
    public Geo geo;
    public ArrayList<Photo> photos=new ArrayList<Photo>(); //except wall
    public ArrayList<Photo> photo_tags=new ArrayList<Photo>();//except wall
    public ArrayList<Note> notes;//except wall
    public ArrayList<String> friends;//uid //except wall

    public String comments_json;

    public static NewsItem parse(JSONObject jitem, boolean is_comments) throws JSONException {
        NewsItem newsitem = new NewsItem();
        newsitem.type = jitem.getString("type");
        newsitem.source_id = Long.parseLong(jitem.getString("source_id"));
        String from_id=jitem.optString("from_id");
        if(from_id!=null && !from_id.equals(""))
            newsitem.from_id = Long.parseLong(from_id);
        newsitem.date = jitem.optLong("date");
        newsitem.post_id = jitem.optLong("post_id");
        newsitem.text = Api.unescape(jitem.optString("text"));
        newsitem.copy_owner_id = jitem.optLong("copy_owner_id");
        newsitem.copy_text = jitem.optString("copy_text");
        JSONArray attachments=jitem.optJSONArray("attachments");
        JSONObject geo_json=jitem.optJSONObject("geo");
        newsitem.attachments=Attachment.parseAttachments(attachments, newsitem.source_id, newsitem.copy_owner_id, geo_json);
        if (jitem.has(NewsJTags.COMMENTS)){
            JSONObject jcomments = jitem.getJSONObject(NewsJTags.COMMENTS);
            newsitem.comment_count = jcomments.optInt("count");//однажды была строка null
            newsitem.comment_can_post = jcomments.optInt("can_post")==1;
            JSONArray x=jcomments.optJSONArray("list");
            if(x!=null)
                newsitem.comments_json=x.toString();
        }
        if (jitem.has(NewsJTags.LIKES)){
            JSONObject jlikes = jitem.getJSONObject(NewsJTags.LIKES);
            newsitem.like_count = jlikes.optInt("count");
            newsitem.user_like = jlikes.optInt("user_likes")==1;
        }
        if (jitem.has(NewsJTags.PHOTO_TAGS)){
            JSONArray jphoto_tags = jitem.getJSONArray(NewsJTags.PHOTO_TAGS);
            newsitem.photo_tags = new ArrayList<Photo>();
            for(int j = 1; j < jphoto_tags.length(); j++) {
                JSONObject jphoto_tag = (JSONObject)jphoto_tags.get(j);
                Photo photo = Photo.parse(jphoto_tag);
                newsitem.photo_tags.add(photo);
            }
        }
        if (jitem.has(NewsJTags.PHOTOS)){
            JSONArray jphotos = jitem.getJSONArray(NewsJTags.PHOTOS);
            newsitem.photos = new ArrayList<Photo>();
            for(int j = 1; j < jphotos.length(); j++) {
                JSONObject jphoto = (JSONObject)jphotos.get(j);
                Photo photo = Photo.parse(jphoto);
                newsitem.photos.add(photo);
            }
        }
        //в newsfeed.getComments фотка прямо в новости
        if (newsitem.type.equals("photo") && is_comments){
            newsitem.photos = new ArrayList<Photo>();
            Photo photo = Photo.parse(jitem);
            newsitem.photos.add(photo);
        }
        if (jitem.has(NewsJTags.FRIENDS))
        {
            JSONArray jfriends = jitem.getJSONArray(NewsJTags.FRIENDS);
            newsitem.friends = new ArrayList<String>();
            for(int j = 1; j < jfriends.length(); ++j) {
                JSONObject jfriend = (JSONObject)jfriends.get(j);
                newsitem.friends.add(jfriend.getString("uid"));
            }
        }
        if (jitem.has(NewsJTags.NOTES))
        {
            JSONArray jnotes = jitem.getJSONArray(NewsJTags.NOTES);
            newsitem.notes = new ArrayList<Note>();
            for(int j = 1; j < jnotes.length(); ++j) {
                JSONObject jnote = (JSONObject)jnotes.get(j);
                Note note = Note.parse(jnote, false);
                newsitem.notes.add(note);
            }
        }
        return newsitem;
    }
}
