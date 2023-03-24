/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message;

import org.citrusframework.CitrusSettings;

public class MessagePayloadUtils {

    private static final boolean prettyPrint = CitrusSettings.isPrettyPrintEnabled();

    /**
     * Prevent instantiation of utility class.
     */
    private MessagePayloadUtils() {
    }

    /**
     * Pretty print given message payload. Supports XML payloads.
     * @param payload
     * @return
     */
    public static String prettyPrint(String payload) {
        if (!prettyPrint) {
            return payload;
        }

        if (isXml(payload)) {
            return prettyPrintXml(payload);
        }

        if (isJson(payload)) {
            return prettyPrintJson(payload);
        }

        return payload;
    }

    /**
     * Checks if given message payload is of XML nature.
     * @param payload
     * @return
     */
    public static boolean isXml(String payload) {
        return payload.trim().startsWith("<");
    }

    /**
     * Check if given message payload is of Json nature.
     * @param payload
     * @return
     */
    public static boolean isJson(String payload) {
        return payload.trim().startsWith("{") || payload.trim().startsWith("[");
    }

    /**
     * Pretty print given XML payload.
     * @param payload
     * @return
     */
    public static String prettyPrintXml(String payload) {
        boolean singleLine = true;
        int indentNum = 2;
        int indent = 0;

        String s = payload.trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char currentChar = s.charAt(i);

            if (currentChar == '<') {
                char nextChar = s.charAt(i + 1);
                if (nextChar == '/') {
                    indent -= indentNum;
                }
                if (!singleLine) {
                    sb.append(" ".repeat(Math.max(0, indent)));
                }
                if (nextChar != '?' && nextChar != '!' && nextChar != '/') {
                    indent += indentNum;
                }
                singleLine = false;
            }
            sb.append(currentChar);
            if (currentChar == '>') {
                if (s.charAt(i - 1) == '/') {
                    indent -= indentNum;
                    sb.append(System.lineSeparator());
                } else {
                    int nextStartElementPos = s.indexOf('<', i);
                    if (nextStartElementPos > i + 1) {
                        String textBetweenElements = s.substring(i + 1, nextStartElementPos);

                        if (textBetweenElements.replaceAll("\\s", "").length() == 0) {
                            sb.append(System.lineSeparator());
                        } else {
                            sb.append(textBetweenElements.trim());
                            singleLine = true;
                        }
                        i = nextStartElementPos - 1;
                    } else {
                        sb.append(System.lineSeparator());
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Pretty print given Json payload.
     * @param payload
     * @return
     */
    public static String prettyPrintJson(String payload) {
        if ("{}".equals(payload) || "[]".equals(payload)) {
            return payload;
        }

        int indentNum = 2;
        int indent = 0;
        boolean inQuote = false;
        boolean isKey = true;

        String s = payload.trim();
        StringBuilder sb = new StringBuilder();
        char previousChar = 0;
        for (char currentChar : s.toCharArray()) {
            switch (currentChar) {
                case '"':
                    if (!inQuote && isKey) {
                        sb.append(" ".repeat(Math.max(0, indent)));
                    }
                    inQuote = !inQuote;
                    sb.append(currentChar);
                    break;
                case ':':
                    if (inQuote) {
                        sb.append(currentChar);
                    } else {
                        isKey = false;
                        sb.append(currentChar).append(" ");
                    }
                    break;
                case ' ':
                    if (inQuote) {
                        sb.append(currentChar);
                    }
                    break;
                case '{':
                case '[':
                    if (inQuote) {
                        sb.append(currentChar);
                    } else {
                        if (isKey) {
                            sb.append(" ".repeat(Math.max(0, indent)));
                        } else {
                            isKey = true;
                        }
                        sb.append(currentChar);
                        sb.append(System.lineSeparator());
                        indent += indentNum;
                    }
                    break;
                case '}':
                case ']':
                    if (!inQuote) {
                        if (previousChar == '"' || Character.isDigit(previousChar)) {
                            isKey = true;
                            sb.append(System.lineSeparator());
                        } else if (previousChar == '}' || previousChar == ']') {
                            sb.append(System.lineSeparator());
                        }

                        indent -= indentNum;
                        sb.append(" ".repeat(Math.max(0, indent)));
                    }
                    sb.append(currentChar);
                    break;
                case ',':
                    sb.append(currentChar);
                    if (!inQuote) {
                        isKey = true;
                        sb.append(System.lineSeparator());
                    }
                    break;
                case '\r':
                case '\n':
                    break;
                default:
                    if (inQuote || !System.lineSeparator().equals(String.valueOf(currentChar))) {
                        sb.append(currentChar);
                    }
            }
            if (inQuote || (!System.lineSeparator().equals(String.valueOf(currentChar)) && !(currentChar == ' '))) {
                previousChar = currentChar;
            }
        }
        return sb.toString();
    }
}
