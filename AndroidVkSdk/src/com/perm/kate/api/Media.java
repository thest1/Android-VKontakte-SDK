package com.perm.kate.api;

public class Media {
    public String type; //app, graffiti, video, audio, photo, posted_photo
    public String item_id;
    public String owner_id; //except graffiti 
    public String thumb_src; //except audio and video
    public String app_id; //only app
}
