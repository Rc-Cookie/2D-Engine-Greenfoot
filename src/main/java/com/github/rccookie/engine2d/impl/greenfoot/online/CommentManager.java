package com.github.rccookie.engine2d.impl.greenfoot.online;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.rccookie.engine2d.impl.greenfoot.GreenfootSession;
import com.github.rccookie.util.Console;

import org.teavm.jso.JSBody;

public final class CommentManager {

    private CommentManager() {
        throw new UnsupportedOperationException();
    }



    @SuppressWarnings("StringConcatenationInLoop")
    public static void manageComments() {
        try {
            debug("Starting comment manager");
            if(GreenfootSession.REAL != GreenfootSession.ONLINE) {
                debug("Not online");
                return;
            }
            int userID = getUserID();
            if(userID <= 0) {
                debug("Not logged in");
                return;
            }
            debug("Logged in, user id:", userID);
            String publisher = getPublisher();
            debug("Publisher:", publisher);
            String extendedUserID = "" + userID;
            while(extendedUserID.length() < 5)
                extendedUserID = "0" + extendedUserID;
            debug("Extended user id:", extendedUserID);
            blockIfNeeded(publisher, extendedUserID);
            debug("Blocking code end");
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private static void debug(Object... args) {
        String message = Arrays.stream(args).map(Object::toString).collect(Collectors.joining(" "));
        if(GreenfootSession.REAL == GreenfootSession.ONLINE) debug0(message);
        else Console.debug(message);
    }

    @JSBody(params = "message", script = "console.debug(message)")
    private static native void debug0(String message);

    @JSBody(script = "return document.getElementById('user_username') !== null ? -1 : parseInt(document.getElementsByClassName('userdata')[0].children[1].href.replace('https://www.greenfoot.org/users/', ''))")
    private static native int getUserID();

    @JSBody(script = "return document.getElementsByClassName('avatar_heading')[0].children[0].innerHTML")
    private static native String getPublisher();

    @JSBody(params = { "publisher", "userID" }, script =
            "var r1 = new XMLHttpRequest();" +
            "r1.open('GET', 'https://www.greenfoot.org/scenarios/29271');" +
            "r1.onload = () => {" +
            "    var index = r1.responseText.indexOf('storage.passcode') + 22;" +
            "    var passcode = r1.responseText.substring(index, index + 16);" +
            "    console.debug('Passcode:', passcode);" +
            "    var r = new XMLHttpRequest();" +
            "    r.open('GET', 'https://www.greenfoot.org/scenarios/29271/userinfo/all_user_data.json?passcode=' + passcode);" +
            "    r.onload = () => {" +
            "        var data = JSON.parse(r.responseText);" +
            "        for(var userNr in data) {" +
            "            var user = data[userNr];" +
            "            console.debug('User with block configured:', user.username);" +
            "            if(user.username !== publisher) continue;" +
            "            var blockData = user.str[0] + user.str[1] + user.str[2] + user.str[3] + user.str[4];" +
            "            console.debug('Block data:', blockData);" +
            "            for(let i=0; i<blockData.length; i+=6) {" +
            "                if(blockData.substring(i, i+5) === userID) {" +
            "                    console.debug('Blocked');" +
            "                    if(document.getElementsByClassName('leave_comment')[0].children.length === 3) {" +
            "                        console.debug('Few comments');" +
            "                        document.getElementsByClassName('leave_comment')[0].children[2].remove();" +
            "                        document.getElementsByClassName('leave_comment')[0].children[1].innerHTML = 'You were blocked from commenting by the scenario owner.';" +
            "                    }" +
            "                    else {" +
            "                        console.debug('Many comments');" +
            "                        document.getElementsByClassName('leave_comment')[0].children[3].remove();" +
            "                        document.getElementsByClassName('leave_comment')[0].children[2].innerHTML = 'You were blocked from commenting by the scenario owner.';" +
            "                    }" +
            "                    return;" +
            "                }" +
            "            }" +
            "            /* Not blocked */" +
            "            console.debug('Not blocked');" +
            "            return;" +
            "        }" +
            "        /* Publisher has not used the dashboard */" +
            "        console.debug('Blocking not configured. The users to block can be configured in the dashboard (https://www.greenfoot.org/scenarios/29271)');" +
            "    };" +
            "    r.send();" +
            "    console.debug('Request sent');" +
            "};" +
            "r1.send();" +
            "console.debug('Pre-request sent');"
    )
    private static native void blockIfNeeded(String publisher, String userID);
}
