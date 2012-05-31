package com.perm.kate.api;

import java.io.Serializable;
import org.json.JSONObject;

public class Geo implements Serializable {
    private static final long serialVersionUID = 1L;
    public String type;
    public String lat;
    public String lon;
    public Place place;
    
    public static Geo parse(JSONObject geo_json){
        Geo geo=new Geo();
        geo.type=geo_json.optString("type");
        String coordinates=geo_json.optString("coordinates");
        if(coordinates!=null){
            String[] coordinates_array=coordinates.split(" ");
            if(coordinates_array.length==2){
                geo.lat=coordinates_array[0];
                geo.lon=coordinates_array[1];
            }
        }
        return geo;
    }
}
