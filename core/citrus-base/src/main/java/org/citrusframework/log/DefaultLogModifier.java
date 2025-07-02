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

package org.citrusframework.log;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.IsYamlPredicate;

/**
 * Default modifier implementation uses regular expressions to mask logger output.
 * Regular expressions match on default keywords.
 */
public class DefaultLogModifier implements LogMessageModifier {

    private final Set<String> keywords = CitrusLogSettings.getLogMaskKeywords();
    private final String logMaskValue = CitrusLogSettings.getLogMaskValue();

    private boolean maskXml = CitrusLogSettings.isMaskXmlEnabled();
    private boolean maskJson = CitrusLogSettings.isMaskJsonEnabled();
    private boolean maskYaml = CitrusLogSettings.isMaskYamlEnabled();
    private boolean maskKeyValue = CitrusLogSettings.isMaskKeyValueEnabled();
    private boolean maskFormUrlEncoded = CitrusLogSettings.isMaskFormUrlEncodedEnabled();

    private Pattern keyValuePattern;
    private Pattern xmlPattern;
    private Pattern jsonPattern;
    private Pattern yamlPattern;
    private Pattern formUrlEncodedPattern;

    @Override
    public String mask(String source) {
        if (!CitrusLogSettings.isLogModifierEnabled() || source == null || source.isEmpty()) {
            return source;
        }

        boolean xml = maskXml && IsXmlPredicate.getInstance().test(source);
        boolean json = maskJson && !xml && IsJsonPredicate.getInstance().test(source);
        boolean yaml = maskYaml && !xml && !json && IsYamlPredicate.getInstance().test(source);
        boolean formUrlEncoded = maskFormUrlEncoded && !yaml && source.contains("&") && source.contains("=");

        String masked = source;
        if (xml) {
            masked = createXmlPattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue + "$2");
            if (maskKeyValue) {
                // used for the attributes in the XML tags
                masked = createKeyValuePattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue);
            }
        } else if (json) {
            masked = createJsonPattern(keywords).matcher(masked).replaceAll("$1\"" + logMaskValue + "\"");
        } else if (yaml) {
            masked = createYamlPattern(keywords).matcher(masked).replaceAll("$1" + logMaskValue);
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

    protected Pattern createYamlPattern(Set<String> keywords) {
        if (yamlPattern == null) {
            String keywordExpression = createKeywordsExpression(keywords);
            if (keywordExpression.isEmpty()) {
                return null;
            }

            String regex = "((?>" + keywordExpression + "):\\s*['\"]?)([^'\"]+)";
            yamlPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        return yamlPattern;
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

    public void setMaskYaml(boolean maskYaml) {
        this.maskYaml = maskYaml;
    }

    public void setMaskXml(boolean maskXml) {
        this.maskXml = maskXml;
    }

    public void setMaskKeyValue(boolean maskKeyValue) {
        this.maskKeyValue = maskKeyValue;
    }

    public void setMaskFormUrlEncoded(boolean maskFormUrlEncoded) {
        this.maskFormUrlEncoded = maskFormUrlEncoded;
    }
}
