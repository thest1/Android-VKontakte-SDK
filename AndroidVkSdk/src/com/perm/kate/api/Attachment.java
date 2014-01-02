package com.perm.kate.api;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Attachment implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;//used only for wall post attached to message
    public String type; //photo,posted_photo,video,audio,link,note,app,poll,doc,geo,message,page,album
    public Photo photo; 
    //public Photo posted_photo; 
    public Video video; 
    public Audio audio; 
    public Link link; 
    public Note note; 
    public Graffiti graffiti;
    public VkApp app; 
    public VkPoll poll;
    public Geo geo;
    public Document document;
    public Message message;
    public WallMessage wallMessage;
    public Page page;
    public Gift gift;
    public Album album;

    public static ArrayList<Attachment> parseAttachments(JSONArray attachments, long from_id, long copy_owner_id, JSONObject geo_json) throws JSONException {
        ArrayList<Attachment> attachments_arr=new ArrayList<Attachment>();
        if(attachments!=null){
            int size=attachments.length();
            for(int j=0;j<size;++j){
                Object att=attachments.get(j);
                if(att instanceof JSONObject==false)
                    continue;
                JSONObject json_attachment=(JSONObject)att;
                Attachment attachment=new Attachment();
                attachment.type=json_attachment.getString("type");
                if(attachment.type.equals("photo") || attachment.type.equals("posted_photo")){
                    JSONObject x=json_attachment.optJSONObject("photo");
                    if(x!=null)
                        attachment.photo=Photo.parse(x);
                }
                else if(attachment.type.equals("graffiti"))
                    attachment.graffiti=Graffiti.parse(json_attachment.getJSONObject("graffiti"));
                else if(attachment.type.equals("link"))
                    attachment.link=Link.parse(json_attachment.getJSONObject("link"));
                else if(attachment.type.equals("audio"))
                    attachment.audio=Audio.parse(json_attachment.getJSONObject("audio"));
                else if(attachment.type.equals("note"))
                    attachment.note=Note.parse(json_attachment.getJSONObject("note"));
                else if(attachment.type.equals("video"))
                    attachment.video=Video.parseForAttachments(json_attachment.getJSONObject("video"));
                else if(attachment.type.equals("poll")){
                    attachment.poll=VkPoll.parse(json_attachment.getJSONObject("poll"));
                    if(attachment.poll.owner_id==0){
                        //это устарело потому что поля copy_owner_id больше нет при парсинге 
                        //if(copy_owner_id!=0)
                        //    attachment.poll.owner_id=copy_owner_id;
                        //else
                        attachment.poll.owner_id=from_id;
                    }
                }
                else if(attachment.type.equals("doc"))
                    attachment.document=Document.parse(json_attachment.getJSONObject("doc"));
                else if(attachment.type.equals("wall"))
                    attachment.wallMessage=WallMessage.parse(json_attachment.getJSONObject("wall"));
                else if(attachment.type.equals("page"))
                    attachment.page=Page.parseFromAttachment(json_attachment.getJSONObject("page"));
                else if(attachment.type.equals("gift"))
                    attachment.gift=Gift.parse(json_attachment.getJSONObject("gift"));
                else if(attachment.type.equals("album"))
                    attachment.album=Album.parseFromAttachment(json_attachment.getJSONObject("album"));
                attachments_arr.add(attachment);
            }
        }
        
        //Geo тоже добавляем в attacmnets если он есть
        if(geo_json!=null){
            Attachment a=new Attachment();
            a.type="geo";
            a.geo=Geo.parse(geo_json);
            attachments_arr.add(a);
        }
        return attachments_arr;
    }
}
