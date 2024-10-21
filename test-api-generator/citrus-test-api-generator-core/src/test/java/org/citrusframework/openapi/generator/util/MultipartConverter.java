package org.citrusframework.openapi.generator.util;

import org.apache.commons.fileupload.MultipartStream;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utility method to convert a multipart http message to a map for simplified assertion.
 */
public class MultipartConverter {

    private static final Pattern NAME_PATTERN = Pattern.compile("name=\"([^\"]+)\"");

    private static final Pattern CONTENT_PATTERN = Pattern.compile("Content-Type:\\s*([^\\s;]+)");

    public static Map<String, Object> multipartMessageToMap(HttpMessage message) {
        String contentType = message.getContentType();
        String boundary = contentType.substring(contentType.indexOf("=") + 1);

        Map<String, Object> partMap = new HashMap<>();
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(message.getPayload(String.class).getBytes());
            MultipartStream multipartStream = new MultipartStream(inputStream, boundary.getBytes(),
                    4096, null);

            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                String headers = multipartStream.readHeaders();
                String partName = getHeaderGroup(headers, NAME_PATTERN);
                String partContentType = getHeaderGroup(headers, CONTENT_PATTERN);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                multipartStream.readBodyData(outputStream);
                String rawBodyContent = outputStream.toString();

                partMap.put(partName, convertContent(rawBodyContent, partContentType));
                nextPart = multipartStream.readBoundary();
            }

            return partMap;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Unable to parse multipart data");
        }

    }

    private static String getHeaderGroup(String headers, Pattern groupPattern) {

        Matcher m = groupPattern.matcher(headers);

        if (m.find()) {
            return m.group(1);
        } else {
            throw new CitrusRuntimeException(
                    "unable to determine header group name: " + groupPattern);
        }
    }

    private static Object convertContent(String rawContent, String contentType) {
        if (contentType != null) {
            if (contentType.contains("application/octet-stream")) {
                return rawContent.getBytes(StandardCharsets.ISO_8859_1);
            }
        }
        return rawContent;
    }
}
