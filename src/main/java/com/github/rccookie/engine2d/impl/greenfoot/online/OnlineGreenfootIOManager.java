package com.github.rccookie.engine2d.impl.greenfoot.online;

import com.github.rccookie.engine2d.impl.IOManager;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootFileLookup;
import com.github.rccookie.engine2d.impl.greenfoot.sessionstorage.GreenfootFileLoader;
import com.github.rccookie.engine2d.util.RuntimeIOException;
import com.github.rccookie.engine2d.util.greenfoot.StringCallback;
import com.github.rccookie.util.Future;
import com.github.rccookie.util.FutureImpl;
import com.github.rccookie.util.NoWaitFutureImpl;

import org.jetbrains.annotations.NotNull;
import org.teavm.jso.JSBody;

public class OnlineGreenfootIOManager implements IOManager {

    static {
        GreenfootFileLoader.load();
    }

    @Override
    public String read(String file) throws RuntimeIOException {
        return GreenfootFileLookup.get(file);
    }

    @Override
    @NotNull
    public String @NotNull [] listFiles() {
        return GreenfootFileLookup.list();
    }

    @Override
    public Future<String> getClipboard() {
        FutureImpl<String> result = new NoWaitFutureImpl<>();
        getClipboard0(result::complete);
        return result;
    }

    @Override
    public void setClipboard(@NotNull String content) {
        setClipboard0(content);
    }

    @SuppressWarnings("JSUnresolvedVariable")
    @JSBody(params = "callback", script =
            "if(navigator.clipboard === null || navigator.clipboard === undefined ||" +
            "   navigator.clipboard.readText === null || navigator.clipboard.readText === undefined) {" +
            "    console.error('Clipboard not available');" +
            "    callback.accept('');" +
            "    return;" +
            "}" +
            "navigator.clipboard.readText()" +
            "    .then(txt => callback.accept(txt))" +
            "    .catch(e => {" +
            "        console.error(e);" +
            "        callback.accept('');" +
            "    });")
    private static native void getClipboard0(StringCallback callback);

    @JSBody(params = "content", script =
            "if(navigator.clipboard === null || navigator.clipboard === undefined ||" +
            "   navigator.clipboard.writeText === null || navigator.clipboard.writeText === undefined) {" +
            "    console.error('Clipboard not available');" +
            "    return;" +
            "}" +
            "navigator.clipboard.writeText(content).catch(e=>console.error(e));")
    private static native void setClipboard0(String content);
}
