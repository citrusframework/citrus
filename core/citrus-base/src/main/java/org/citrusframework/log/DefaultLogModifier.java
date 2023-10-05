/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.log;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;

/**
 * Default modifier implementation uses regular expressions to mask logger output.
 * Regular expressions match on default keywords.
 *
 * @author Christoph Deppisch
 */
public class DefaultLogModifier implements LogMessageModifier {

    private final Set<String> keywords =  CitrusSettings.getLogMaskKeywords();
    private final String logMaskValue = CitrusSettings.getLogMaskValue();

    private boolean maskXml = true;
    private boolean maskJson = true;
    private boolean maskKeyValue = true;
    private boolean maskFormUrlEncoded = true;

    private Pattern keyValuePattern;
    private Pattern xmlPattern;
    private Pattern jsonPattern;
    private Pattern formUrlEncodedPattern;

    @Override
    public String mask(String source) {
        if (!CitrusSettings.isLogModifierEnabled() || source == null || source.length() == 0) {
            return source;
        }

        boolean xml = maskXml && source.startsWith("<");
        boolean json = maskJson && !xml && (source.startsWith("{") || source.startsWith("["));
        boolean formUrlEncoded = maskFormUrlEncoded && !json && source.contains("&") && source.contains("=");

        String masked = source;
        if (xml) {
            masked = createXmlPattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue + "$2");
            if (maskKeyValue) {
                // used for the attributes in the XML tags
                masked = createKeyValuePattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue + "");
            }
        } else if (json) {
            masked = createJsonPattern(keywords).matcher(masked).replaceAll("$1\"" + logMaskValue + "\"");
        } else if (formUrlEncoded) {
            masked = createFormUrlEncodedPattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue);
        } else if (maskKeyValue) {
            masked = createKeyValuePattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue);
        }

        return masked;
    }

    protected Pattern createKeyValuePattern(Set<String> keywords) {
        if (keyValuePattern == null) {
            String keywordExpression = createKeywordsExpression(keywords);
            if (keywordExpression.isEmpty()) {
                return null;
            }

            String regex = "((?>" + keywordExpression + ")\\s*=\\s*['\"]?)([^,'\"]+)";
            keyValuePattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        return keyValuePattern;
    }

    protected Pattern createFormUrlEncodedPattern(Set<String> keywords) {
        if (formUrlEncodedPattern == null) {
            String keywordExpression = createKeywordsExpression(keywords);
            if (keywordExpression.isEmpty()) {
                return null;
            }

            String regex = "((?>" + keywordExpression + ")\\s*=\\s*)([^&]*)";
            formUrlEncodedPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        return formUrlEncodedPattern;
    }

    protected Pattern createXmlPattern(Set<String> keywords) {
        if (xmlPattern == null) {
            String keywordExpression = createKeywordsExpression(keywords);
            if (keywordExpression.isEmpty()) {
                return null;
            }

            String regex = "(<(?>" + keywordExpression + ")>)[^<]*(</(?>" + keywordExpression + ")>)";
            xmlPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        return xmlPattern;
    }

    protected Pattern createJsonPattern(Set<String> keywords) {
        if (jsonPattern == null) {
            String keywordExpression = createKeywordsExpression(keywords);
            if (keywordExpression.isEmpty()) {
                return null;
            }

            String regex = "(\"(?>" + keywordExpression + ")\"\\s*:\\s*)(\"?[^\",]*[\",])";
            jsonPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        return jsonPattern;
    }

    protected String createKeywordsExpression(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return "";
        }

        return keywords.stream().map(Pattern::quote).collect(Collectors.joining("|"));
    }

    public void setMaskJson(boolean maskJson) {
        this.maskJson = maskJson;
    }

    public void setMaskXml(boolean maskXml) {
        this.maskXml = maskXml;
    }

    public void setMaskKeyValue(boolean maskKeyValue) {
        this.maskKeyValue = maskKeyValue;
    }
}
