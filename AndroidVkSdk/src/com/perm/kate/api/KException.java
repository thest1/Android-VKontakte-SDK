package com.perm.kate.api;

@SuppressWarnings("serial")
public class KException extends Exception{
    KException(int code, String message, String url){
        super(message);
        error_code=code;
        this.url=url;
    }
    public int error_code;
    public String url;
    
    //for captcha
    public String captcha_img;
    public String captcha_sid;
    
    //for "Validation required" error
    public String redirect_uri;
}
