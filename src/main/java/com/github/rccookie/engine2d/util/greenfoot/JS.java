package com.github.rccookie.engine2d.util.greenfoot;

import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;

import org.teavm.jso.JSBody;

@SuppressWarnings("NewClassNamingConvention")
public final class JS {

    private JS() {
        throw new UnsupportedOperationException();
    }

    public static final boolean available = GreenfootSession.REAL == GreenfootSession.ONLINE;



    public static String eval(String code) throws JSSyntaxException {
        checkAvailable();
        try {
            return eval0(code);
        } catch(RuntimeException e) {
            String msg = e.toString();
            if(!msg.startsWith("java.lang.RuntimeException: (JavaScript) SyntaxError: ")) throw e;
            throw new JSSyntaxException(msg.substring(54));
        }
    }

    public static String evalBlock(String code) throws JSSyntaxException {
        return eval("(() => {" + code + "})()");
    }

    @JSBody(params = "code", script =
            "var r = eval(code);" +
            "if(r === undefined || r === null) return null;" +
            "var s = JSON.stringify(r);" +
            "return s === undefined ? String(r) : s;")
    private static native String eval0(String code);

    private static void checkAvailable() {
        if(!available)
            throw new UnsupportedOperationException("JS evaluation is not available. Check JS.available first");
    }
}
