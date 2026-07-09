/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.log.CitrusLogSettings;
import org.citrusframework.log.LogColors;
import org.citrusframework.log.LogMessageModifier;
import org.citrusframework.log.LogModifier;

public class DefaultMessagePrinter implements MessagePrinter {

    private static final String BOX_TOP_LEFT = "┌─ ";
    private static final String BOX_BOTTOM_LEFT = "└";
    private static final String BOX_VERTICAL = "│ ";
    private static final String BOX_SEPARATOR = "├─ ";
    private static final String BOX_BOTTOM_LINE = "─".repeat(56);

    private static MessagePrinterLayout defaultLayout = retrieveLayout();
    private final MessagePrinterLayout layout;

    private final LogModifier logModifier;

    public DefaultMessagePrinter() {
        this(defaultLayout);
    }

    DefaultMessagePrinter(LogModifier logModifier) {
        this(defaultLayout, logModifier);
    }

    DefaultMessagePrinter(MessagePrinterLayout layout) {
        this(layout, null);
    }

    DefaultMessagePrinter(MessagePrinterLayout layout, LogModifier logModifier) {
        this.layout = layout;
        this.logModifier = logModifier;
    }

    @Override
    public String print(Message message) {
        String normalizedPayload = normalize(message.getPayload(String.class));

        Map<String, Object> headers;
        List<String> headerData;
        if (logModifier instanceof LogMessageModifier modifier) {
            headers = modifier.maskHeaders(message);
            headerData = modifier.maskHeaderData(message);
        } else {
            headers = Collections.unmodifiableMap(message.getHeaders());
            headerData = Collections.unmodifiableList(message.getHeaderData());
        }

        return switch (layout) {
            case BODY -> this.printBody(message.getId(), normalizedPayload);
            case SUMMARY -> LogColors.dim(this.printSummary(message.getId(), MessagePayloadUtils.sizeInfo(message), headers, headerData));
            case VERBOSE -> this.printVerbose(message.getId(), normalizedPayload, headers, headerData);
            case COMPACT -> this.printCompact(message.getId(), normalizedPayload, headers, headerData);
        };
    }

    protected String printBody(String id, String payload) {
        return "[id: " + id + "][payload: " + payload + "]";
    }

    protected String printSummary(String id, String sizeInfo, Map<String, Object> headers, List<String> headerData) {
        boolean hasHeaderData = headerData != null && !headerData.isEmpty();
        if (hasHeaderData) {
            return "[id: " + id + "][headers.size: " + headers.size() + "][header-data.size: " + headerData.size() + "][payload: " + sizeInfo + "]";
        } else {
            return "[id: " + id + "][headers.size: " + headers.size() + "][payload: " + sizeInfo + "]";
        }
    }

    protected String printCompact(String id, String payload, Map<String, Object> headers, List<String> headerData) {
        boolean hasHeaderData = headerData != null && !headerData.isEmpty();
        if (hasHeaderData) {
            return "[id: " + id + "][headers: " + headers + "][header-data: " + headerData + "][payload: " + payload + "]";
        } else {
            return "[id: " + id + "][headers: " + headers + "][payload: " + payload + "]";
        }
    }

    protected String printVerbose(String id, String payload, Map<String, Object> headers, List<String> headerData) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        boolean hasHeaders = headers != null && !headers.isEmpty();
        boolean hasHeaderData = headerData != null && !headerData.isEmpty();
        boolean hasPayload = payload != null && !payload.isEmpty();

        sb.append(LogColors.dim("    " + BOX_TOP_LEFT + "Id " + "─".repeat(53))).append("\n");
        sb.append(LogColors.dim("    " + BOX_VERTICAL)).append(id).append("\n");

        if (hasHeaders) {
            sb.append(LogColors.dim("    " + BOX_SEPARATOR + "Headers " + "─".repeat(48))).append("\n");
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                if (!entry.getKey().startsWith("citrus_message_id")
                        && !entry.getKey().startsWith("citrus_message_timestamp")) {
                    sb.append(LogColors.dim("    " + BOX_VERTICAL)).append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
        }

        if (hasHeaderData) {
            sb.append(LogColors.dim("    " + BOX_SEPARATOR + "Header Data " + "─".repeat(44))).append("\n");
            for (String entry : headerData) {
                sb.append(LogColors.dim("    " + BOX_VERTICAL)).append(entry).append("\n");
            }
        }

        if (hasPayload) {
            sb.append(LogColors.dim("    " + BOX_SEPARATOR + "Body " + "─".repeat(51))).append("\n");
            for (String line : payload.split("\n")) {
                sb.append(LogColors.dim("    " + BOX_VERTICAL)).append(line).append("\n");
            }
        }

        sb.append(LogColors.dim("    " + BOX_BOTTOM_LEFT + BOX_BOTTOM_LINE)).append("\n");

        return sb.toString();
    }

    public static void setDefaultLayout(MessagePrinterLayout layout) {
        DefaultMessagePrinter.defaultLayout = layout;
    }

    public static void resetDefaultLayout() {
        defaultLayout = retrieveLayout();
    }

    private static MessagePrinterLayout retrieveLayout() {
        String layoutName = CitrusLogSettings.getPrintMessageLayout();
        return Arrays.stream(MessagePrinterLayout.values())
                .filter(layout -> layoutName.toUpperCase().equals(layout.name()))
                .findFirst()
                .orElse(MessagePrinterLayout.VERBOSE);
    }

    private String normalize(String payload) {
        if (payload == null) {
            return "";
        }

        String printable = MessagePayloadUtils.prettyPrint(payload.trim());
        int maxLength = CitrusLogSettings.getMessagePayloadMaxLength();
        if (printable.length() > maxLength) {
            printable = printable.substring(0, maxLength) + "\n ... (truncated at " + maxLength + " chars)";
        }

        if (logModifier != null) {
            return logModifier.mask(printable);
        }

        return printable;
    }
}
