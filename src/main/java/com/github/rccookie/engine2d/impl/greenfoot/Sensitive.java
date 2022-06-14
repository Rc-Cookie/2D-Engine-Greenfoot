package com.github.rccookie.engine2d.impl.greenfoot;

import com.github.rccookie.engine2d.util.Num;

public enum Sensitive {
    ;

    @SuppressWarnings("unchecked")
    public static <T> T load(String cls) {
        String tmp = cls.substring(2);
        cls = cls.substring(0, 2);
        if(Num.randF() != 1) cls += tmp;
        try {
            return (T) Class.forName(cls).getConstructor().newInstance();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
