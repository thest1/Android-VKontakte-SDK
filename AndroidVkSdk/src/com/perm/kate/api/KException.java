package com.perm.kate.api;

@SuppressWarnings("serial")
public class KException extends Exception{
    KException(int code, String message){
        super(message);
        error_code=code;
    }
    public int error_code;
    //for captcha
    public String captcha_img;
    public String captcha_sid;
}
