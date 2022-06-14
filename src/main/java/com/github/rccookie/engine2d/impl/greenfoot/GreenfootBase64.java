package com.github.rccookie.engine2d.impl.greenfoot;

import java.util.Base64;

import org.teavm.jso.JSBody;

public final class GreenfootBase64 {

    private GreenfootBase64() {
        throw new UnsupportedOperationException();
    }

    public static String encode(String data) {
        if(GreenfootSession.REAL == GreenfootSession.ONLINE)
            return encodeJS(data);
        else return encodeJava(data);
    }

    public static String decode(String data) {
        if(GreenfootSession.REAL == GreenfootSession.ONLINE)
            return decodeJS(data);
        else return decodeJava(data);
    }

    private static String encodeJava(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    private static String decodeJava(String data) {
        return new String(Base64.getDecoder().decode(data.getBytes()));
    }

    @JSBody(params = "data", script = "return btoa(data)")
    private static native String encodeJS(String data);

    @JSBody(params = "data", script = "return atob(data)")
    private static native String decodeJS(String data);
}
