/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary.json;

import java.util.Iterator;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple json data dictionary implementation holds a set of mappings where keys are json path expressions to match
 * json object graph. Parses message payload to json object tree. Traverses
 * through json data supporting nested json objects, arrays and values.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JsonMappingDataDictionary extends AbstractJsonDataDictionary {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonMappingDataDictionary.class);

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return;
        }

        JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

        try {
            Object json = parser.parse(message.getPayload(String.class));

            if (json instanceof JSONObject) {
                traverseJsonData((JSONObject) json, "", context);
            } else if (json instanceof JSONArray) {
                JSONObject tempJson = new JSONObject();
                tempJson.put("root", json);
                traverseJsonData(tempJson, "", context);
            } else {
                throw new CitrusRuntimeException("Unsupported json type " + json.getClass());
            }

            message.setPayload(json.toString());
        } catch (ParseException e) {
            logger.warn("Data dictionary unable to parse JSON object", e);
        }
    }

    @Override
    public <T> T translate(String jsonPath, T value, TestContext context) {
        if (getPathMappingStrategy().equals(PathMappingStrategy.EXACT)) {
            if (mappings.containsKey(jsonPath)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, mappings.get(jsonPath)));
                }
                return convertIfNecessary(mappings.get(jsonPath), value, context);
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (jsonPath.endsWith(entry.getKey())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, entry.getValue()));
                    }
                    return convertIfNecessary(entry.getValue(), value, context);
                }
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (jsonPath.startsWith(entry.getKey())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, entry.getValue()));
                    }
                    return convertIfNecessary(entry.getValue(), value, context);
                }
            }
        }

        return value;
    }

    /**
     * Walks through the Json object structure and translates values based on element path if necessary.
     * @param jsonData
     * @param jsonPath
     * @param context
     */
    private void traverseJsonData(JSONObject jsonData, String jsonPath, TestContext context) {
        for (Iterator it = jsonData.entrySet().iterator(); it.hasNext();) {
            Map.Entry jsonEntry = (Map.Entry) it.next();

            if (jsonEntry.getValue() instanceof JSONObject) {
                traverseJsonData((JSONObject) jsonEntry.getValue(), (StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()), context);
            } else if (jsonEntry.getValue() instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonEntry.getValue();
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i) instanceof JSONObject) {
                        traverseJsonData((JSONObject) jsonArray.get(i), String.format((StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()) + "[%s]", i), context);
                    } else {
                        jsonArray.set(i, translate(String.format((StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()) + "[%s]", i), jsonArray.get(i), context));
                    }
                }
            } else {
                jsonEntry.setValue(translate((StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()),
                        jsonEntry.getValue() != null ? jsonEntry.getValue() : null, context));
            }
        }
    }

}
