package com.github.rccookie.engine2d.impl.greenfoot.online;

import java.util.HashMap;
import java.util.Map;

import com.github.rccookie.engine2d.impl.HTTPResponseData;
import com.github.rccookie.engine2d.impl.OnlineManager;
import com.github.rccookie.engine2d.online.HTTPRequest;
import com.github.rccookie.engine2d.util.Pool;
import com.github.rccookie.engine2d.util.greenfoot.JS;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.DualComputationFutureImpl;
import com.github.rccookie.util.Future;
import com.github.rccookie.util.FutureImpl;

import org.jetbrains.annotations.NotNull;
import org.teavm.jso.JSBody;

/**
 * JavaScript based greenfoot.Greenfoot online manager.
 */
public class OnlineGreenfootOnlineManager implements OnlineManager {

    /**
     * The online manager instance.
     */
    public static final OnlineGreenfootOnlineManager INSTANCE = new OnlineGreenfootOnlineManager();

    /**
     * Next free id to use for an HTTP request.
     */
    private static int nextID = 0;
    /**
     * Free ids to use for HTTP requests.
     */
    private static final Pool<Integer> resultIDs = new Pool<>(() -> nextID++);
    /**
     * Maps each pending request id to the respective result future.
     */
    private static final Map<Integer, FutureImpl<HTTPResponseData>> runningResults = new HashMap<>();

    static {
        // Keep methods in class file
        if(System.currentTimeMillis() < 0) {
            acceptResult(-1, -1, null, null);
            acceptError(-1);
        }
    }





    @SuppressWarnings("unchecked")
    @Override
    public Future<HTTPResponseData> sendHTTPRequest(@NotNull String url, @NotNull HTTPRequest.Method method, Map<String, String> header, String data) {
        int id = resultIDs.get();

        Console.debug("Sending request...");

        String[] headerArray = new String[header.size() * 2];
        Object[] headerEntries = header.entrySet().toArray();
        for(int i=0; i<headerEntries.length; i++) {
            headerArray[2*i] = ((Map.Entry<String,String>) headerEntries[i]).getKey();
            headerArray[2*i+1] = ((Map.Entry<String,String>) headerEntries[i]).getValue();
        }

        FutureImpl<HTTPResponseData> result = new DualComputationFutureImpl<>() {
            @Override
            public void cancelNonBlocking() {
                Console.debug("waitFor() called, cancelling async request...");
                //noinspection JSUnresolvedVariable
                JS.eval("cgreigo_OnlineGreenfootOnlineManager_runningResults.$remove6 = cgreigo_OnlineGreenfootOnlineManager_runningResults.$remove1");
                Console.log("Fixed incorrect method reference");
                runningResults.remove(id, this);
                resultIDs.returnObject(id);
                // TODO
                Console.debug("Cancelled");
            }

            @Override
            public HTTPResponseData computeBlocking() {
                String[] resp = sendHTTPRequestBlocking(url, method.toString(), headerArray, data);

                String code = resp[0], data = resp[1], header = resp[2];

                return new HTTPResponseData(Integer.parseInt(code), data, parseHeader(header));
            }
        };

        runningResults.put(id, result);

        sendHTTPRequestAsync(id, url, method.toString(), headerArray, data);
        return result;
    }

    /**
     * Native JavaScript implementation of sending an HTTP request asynchronously.
     * When done {@link #acceptResult(int, int, String, String)} or {@link #acceptError(int)}
     * will be invoked.
     *
     * @param id The id of the request
     * @param url The url for the HTTP request
     * @param method The request method
     * @param header Request headers, alternating between key and value
     * @param data Data to send
     */
    @JSBody(params = { "id", "url", "method", "header", "data" }, script =
            "var request = new XMLHttpRequest();" +
            "request.open(method, url, true);" +
            "var oldCookies = null;" +
            "var cookies = null;" +
            "for(let i=0; i<header.length / 2; i++) {" +
            "    if('Cookie' !== header[2*i])" +
            "        request.setRequestHeader(header[2*i], header[2*i+1]);" +
            "    else {" +
            "        oldCookies = document.cookie;" +
            "        document.cookie = cookies = header[2*i+1];" +
            "    }" +
            "}" +
            "request.onload = () => {" +
            "        if(cookies != null) {" +
            "            document.cookie = cookies.replaceAll('=.*;', '=').replaceAll('=.*$');" +
            "            document.cookie = oldCookies;" +
            "        }" +
            "        cgreigo_OnlineGreenfootOnlineManager_acceptResult(id, request.status, $rt_str(request.responseText), $rt_str(request.getAllResponseHeaders()));" +
            "};" +
            "request.onerror = () => {" +
            "        if(cookies != null) {" +
            "            document.cookie = cookies.replaceAll('=.*;', '=').replaceAll('=.*$');" +
            "            document.cookie = oldCookies;" +
            "        }" +
            "        cgreigo_OnlineGreenfootOnlineManager_acceptError(id);" +
            "};" +
            "request.send(data);"
    )
    private static native void sendHTTPRequestAsync(int id, String url, String method, String[] header, String data);


    /**
     * Accepts the specified result of an HTTP request.
     *
     * @param id The request id
     * @param code The response code
     * @param data The data received
     * @param header Headers received, in the format {@code key: value} seperated
     *               with \r\n
     */
    private static void acceptResult(int id, int code, String data, String header) {
        FutureImpl<HTTPResponseData> result = runningResults.get(id);
        runningResults.remove(id);
        resultIDs.returnObject(id);

        if(result == null || result.isDone()) return;

        result.complete(new HTTPResponseData(code, data, parseHeader(header)));
    }

    /**
     * Returns the given http header as a map. The string has to be formatted in standard
     * http header format, meaning each header field is in a separate line, followed by a
     * colon and a space, and then it's value.
     *
     * @param header The header string
     * @return The header as map
     */
    private static Map<String,String> parseHeader(String header) {
        Map<String,String> headerMap = new HashMap<>();
        if(header != null)
            for(String headerEntry : split(header, "\r\n"))
                if(headerEntry.contains(": "))
                    headerMap.put(headerEntry.substring(0, headerEntry.indexOf(": ")), headerEntry.substring(headerEntry.indexOf(": ") + 2));
        return headerMap;
    }

    /**
     * Accepts that the specified HTTP request had an error.
     *
     * @param id The request id
     */
    private static void acceptError(int id) {
        FutureImpl<HTTPResponseData> result = runningResults.get(id);
        runningResults.remove(id);
        resultIDs.returnObject(id);
        result.cancel();
    }

    /**
     * Native implementation of {@link String#split(String)}
     *
     * @param str The string
     * @param regex The splitting regular expression
     * @return The string split into parts around the regex
     */
    @JSBody(params = { "str", "regex" }, script = "return str.split(regex)")
    private static native String[] split(String str, String regex);

    /**
     * Native JavaScript implementation of sending an http request blocking.
     * As result will be a string array with exactly 3 entries returned, which
     * are: The response code (as string), the response text, and the response
     * headers.
     *
     * @param url The url for the http request
     * @param method The request method
     * @param header Request headers
     * @param data Data to send over the http request
     * @return An array with { responseCode, response, headers }
     */
    @JSBody(params = { "url", "method", "header", "data" }, script =
            "var request = new XMLHttpRequest();" +
            "request.open(method, url, false);" +
            "var oldCookies = null;" +
            "var cookies = null;" +
            "for(let i=0; i<header.length / 2; i++) {" +
            "    if('Cookie' !== header[2*i])" +
            "        request.setRequestHeader(header[2*i], header[2*i+1]);" +
            "    else {" +
            "        oldCookies = document.cookie;" +
            "        document.cookie = cookies = header[2*i+1];" +
            "    }" +
            "}" +
            "request.send(data);" +
            "return [ request.status + '', request.responseText, request.getAllResponseHeaders() ];"
    )
    private static native String[] sendHTTPRequestBlocking(String url, String method, String[] header, String data);
}
