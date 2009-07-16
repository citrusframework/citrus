package com.consol.citrus.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.consol.citrus.message.Message;

public class HttpUtils {

    /**
     *
     * @param request
     * @return
     */
    public static String generateRequest(Message request) {
        StringBuffer sBuf = new StringBuffer();
        Map<String, String> requestHeaders = request.getHeader();

        // output status line
        sBuf.append(request.getHeader().get("HTTPMethod"));
        sBuf.append(" ").append(request.getHeader().get("HTTPUri"));
        sBuf.append(" ").append(request.getHeader().get("HTTPVersion")).append(HttpConstants.LINE_BREAK);

        if (!requestHeaders.containsKey("host")) {
            requestHeaders.put("host", request.getHeader().get("HTTPHost") + ":" + request.getHeader().get("HTTPPort"));
        }

        if (!requestHeaders.containsKey("connection")) {
            requestHeaders.put("connection", "close");
        }

        if (request.getMessagePayload() != null && request.getMessagePayload().length() > 0 && !requestHeaders.containsKey("content-length")) {
            requestHeaders.put("content-length", Integer.toString(request.getMessagePayload().length()));
        }

        // output headers
        Set<Map.Entry<String, String>> entrySet = requestHeaders.entrySet();
        for (Iterator<Map.Entry<String, String>> iter = entrySet.iterator(); iter.hasNext();) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            if (entry.getKey().startsWith("HTTP") == false) {
                sBuf.append(entry.getKey()).append(": ").append(entry.getValue()).append(HttpConstants.LINE_BREAK);
            }
        }

        // output post data
        if (request.getMessagePayload() != null && request.getMessagePayload().length() > 0) {
            sBuf.append(HttpConstants.LINE_BREAK);
            sBuf.append(request.getMessagePayload());
        }

        // signal end
        sBuf.append(HttpConstants.LINE_BREAK);

        return sBuf.toString();
    }

    public static String generateResponse(Message response) {
        if (response.getHeader().get("HTTPVersion") == null || response.getHeader().get("HTTPVersion").length() == 0) {
            response.addHeaderElement("HTTPVersion", HttpConstants.HTTP_VERSION);
        }
        if (response.getHeader().get("HTTPStatusCode") == null || response.getHeader().get("HTTPStatusCode").length() == 0) {
            response.addHeaderElement("HTTPStatusCode", HttpConstants.HTTP_CODE_200);
        }
        if (response.getHeader().get("HTTPReasonPhrase") == null || response.getHeader().get("HTTPReasonPhrase").length() == 0) {
            response.addHeaderElement("HTTPReasonPhrase", HttpConstants.HTTP_STATUS_OK);
        }

        StringBuffer sBuf = new StringBuffer();

        // output status line
        sBuf.append(response.getHeader().get("HTTPVersion"));
        sBuf.append(" ").append(response.getHeader().get("HTTPStatusCode"));
        sBuf.append(" ").append(response.getHeader().get("HTTPReasonPhrase")).append(HttpConstants.LINE_BREAK);

        // output headers
        if (response.getHeader() != null) {
            Set entrySet = response.getHeader().entrySet();
            for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                if (entry.getKey().startsWith("HTTP") == false) {
                    sBuf.append(entry.getKey()).append(": ").append(entry.getValue()).append(HttpConstants.LINE_BREAK);
                }
            }
        }

        // output content
        String content = response.getMessagePayload();
        if (content != null && content.length() > 0) {
            sBuf.append(HttpConstants.LINE_BREAK);
            sBuf.append(content);
        }

        // signal end
        sBuf.append(HttpConstants.LINE_BREAK);

        return sBuf.toString();
    }
}
