package com.perm.kate.api;

import java.net.URLEncoder;
import android.util.Log;
import com.perm.utils.Utils;

public class Auth {
    
    private static final String TAG = "Kate.Auth";
    public static String redirect_url="http://oauth.vk.com/blank.html";
    
    public static String getUrl(String api_id, String settings){
        String url="http://oauth.vk.com/authorize?client_id="+api_id+"&display=touch&scope="+settings+"&redirect_uri="+URLEncoder.encode(redirect_url)+"&response_type=token";
        return url;
    }
    
    public static String getSettings(){
        //http://vk.com/developers.php?oid=-1&p=%D0%9F%D1%80%D0%B0%D0%B2%D0%B0_%D0%B4%D0%BE%D1%81%D1%82%D1%83%D0%BF%D0%B0_%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9
        //+1      Пользователь разрешил отправлять ему уведомления.
        //+2      Доступ к друзьям.
        //+4      Доступ к фотографиям.
        //+8      Доступ к аудиозаписям.
        //+16     Доступ к видеозаписям.
        //+32     Доступ к предложениям.
        //+64     Доступ к вопросам.
        //+128    Доступ к wiki-страницам.
        //+256    Добавление ссылки на приложение в меню слева.
        //+512    Добавление ссылки на приложение для быстрой публикации на стенах пользователей.
        //+1024   Доступ к статусам пользователя.
        //+2048   Доступ заметкам пользователя.
        //+4096   (для Desktop-приложений) Доступ к расширенным методам работы с сообщениями.
        //+8192   Доступ к обычным и расширенным методам работы со стеной.
        //+65536  offline
        //+131072 Доступ к документам пользователя.
        //+262144 Доступ к группам пользователя.
        //+524288 Доступ к оповещениям об ответах пользователю.
        int settings=1+2+4+8+16+32+64+128+1024+2048+4096+8192+65536+131072+262144+524288;
        return Integer.toString(settings);
        //return "friends,photos,audio,video,docs,notes,pages,wall,groups,messages,offline,notifications";
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