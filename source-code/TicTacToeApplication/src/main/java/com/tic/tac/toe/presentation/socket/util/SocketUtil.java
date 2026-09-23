package com.tic.tac.toe.presentation.socket.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class SocketUtil {
    public static String extractToken(String resourceDescriptor)
            throws UnsupportedEncodingException {
        int queryIndex = resourceDescriptor.indexOf('?');
        if (queryIndex == -1) {
            return null;
        }
        String query = resourceDescriptor.substring(queryIndex + 1);
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals("accessToken")) {
                return URLDecoder.decode(pair[1], String.valueOf(StandardCharsets.UTF_8));
            }
        }
        return null;
    }
}
