package org.citrusframework.message;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.annotation.Nullable;

final class MediaTypeMapper {

    private static final Map<String, MessageType> MEDIA_TYPE_TO_CITRUS;
    static {
        Map<String, MessageType> m = new LinkedHashMap<>();
        // XML
        m.put("application/xml",          MessageType.XML);
        m.put("text/xml",                 MessageType.XML);
        // XHTML
        m.put("application/xhtml+xml",    MessageType.XHTML);
        // CSV
        m.put("text/csv",                 MessageType.CSV);
        // JSON
        m.put("application/json",         MessageType.JSON);
        // YAML
        m.put("application/x-yaml",       MessageType.YAML);
        m.put("application/yaml",         MessageType.YAML);
        // Plain text
        m.put("text/plain",               MessageType.PLAINTEXT);
        // Binary
        m.put("application/octet-stream", MessageType.BINARY);
        // GZIP
        m.put("application/gzip",         MessageType.GZIP);
        m.put("application/x-gzip",       MessageType.GZIP);
        // MSCONS / EDIFACT
        m.put("application/edifact",              MessageType.MSCONS);
        m.put("application/edifact+mscons",       MessageType.MSCONS);
        m.put("application/edifact;profile=mscons", MessageType.MSCONS);
        // Common binary file types
        m.put("application/pdf",                     MessageType.BINARY);
        m.put("application/zip",                     MessageType.BINARY);
        m.put("application/x-7z-compressed",         MessageType.BINARY);
        m.put("image/png",                           MessageType.BINARY);
        m.put("image/jpeg",                          MessageType.BINARY);
        m.put("image/gif",                           MessageType.BINARY);

        MEDIA_TYPE_TO_CITRUS = Collections.unmodifiableMap(m);
    }

    private MediaTypeMapper() {
        // no instances
    }

    /**
     * Map a Content-Type + optional Content-Transfer-Encoding
     * to {@link MessageType}.
     *
     * @param contentTypeHeader the raw header value, e.g. "application/json; charset=UTF-8"
     * @param encodingHeader    the raw encoding header, e.g. "base64"
     * @return the best-match MessageType, or null if unknown
     */
    static MessageType mapToMessageType(@Nullable String contentTypeHeader, @Nullable String encodingHeader) {
        if (contentTypeHeader == null) {
            return null;
        }

        String ct = contentTypeHeader.split(";", 2)[0].trim().toLowerCase();

        String enc = encodingHeader != null ? encodingHeader.trim().toLowerCase() : "";
        if ("base64".equals(enc)) {
            if ("application/octet-stream".equals(ct)) {
                return MessageType.BINARY_BASE64;
            }
            if ("application/gzip".equals(ct) || "application/x-gzip".equals(ct)) {
                return MessageType.GZIP_BASE64;
            }
        }

        return MEDIA_TYPE_TO_CITRUS.get(ct);
    }
}
