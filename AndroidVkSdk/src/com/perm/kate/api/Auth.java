package com.perm.kate.api;

import java.net.URLEncoder;
import android.util.Log;
import com.perm.utils.Utils;

public class Auth {
    
    private static final String TAG = "Kate.Auth";
    public static String redirect_url="https://oauth.vk.com/blank.html";
    
    public static String getUrl(String api_id, String settings){
        String url="https://oauth.vk.com/authorize?client_id="+api_id+"&display=mobile&scope="+settings+"&redirect_uri="+URLEncoder.encode(redirect_url)+"&response_type=token"
                +"&v="+URLEncoder.encode(Api.API_VERSION);
        return url;
    }
    
    public static String getSettings(){
        //http://vk.com/dev/permission
        return "notify,friends,photos,audio,video,docs,status,notes,pages,wall,groups,messages,offline,notifications";
    }
    
    public static String[] parseRedirectUrl(String url) throws Exception {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token=Utils.extractPattern(url, "access_token=(.*?)&");
        Log.i(TAG, "access_token=" + access_token);
        String user_id=Utils.extractPattern(url, "user_id=(\\d*)");
        Log.i(TAG, "user_id=" + user_id);
        if(user_id==null || user_id.length()==0 || access_token==null || access_token.length()==0)
            throw new Exception("Failed to parse redirect url "+url);
        return new String[]{access_token, user_id};
    }
}