package com.perm.kate.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Params {
    TreeMap<String, String> args = new TreeMap<String, String>();
    String method_name;
    
    public Params(String method_name){
        this.method_name=method_name;
    }

    public void put(String param_name, String param_value) {
        if(param_value==null || param_value.length()==0)
            return;
        args.put(param_name, param_value);
    }
    
    public void put(String param_name, Long param_value) {
        if(param_value==null)
            return;
        args.put(param_name, Long.toString(param_value));
    }
    
    public void put(String param_name, Integer param_value) {
        if(param_value==null)
            return;
        args.put(param_name, Integer.toString(param_value));
    }
    
    public void putDouble(String param_name, double param_value) {
        args.put(param_name, Double.toString(param_value));
    }
    
    public String getParamsString(boolean is_post) {
        String params="";
        try {
            for(Entry<String, String> entry:args.entrySet()){
                if(params.length()!=0)
                    params+="&";
                params+=(entry.getKey()+"="+URLEncoder.encode(entry.getValue(), "utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return params;
    }

}
