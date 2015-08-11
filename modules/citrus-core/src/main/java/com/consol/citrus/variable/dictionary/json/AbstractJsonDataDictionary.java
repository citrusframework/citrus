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

package com.consol.citrus.variable.dictionary.json;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.variable.dictionary.AbstractDataDictionary;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract json data dictionary works on json message data. Parses message payload to json object tree. Traverses
 * through json data supporting nested json objects, arrays and values. Each value is translated with dictionary.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractJsonDataDictionary extends AbstractDataDictionary<String> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractJsonDataDictionary.class);

    @Override
    protected Message interceptMessage(Message message, String messageType, TestContext context) {
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
            log.warn("Data dictionary unable to parse JSON object", e);
        }

        return message;
    }

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
                        jsonArray.set(i, translate(String.format((StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()) + "[%s]", i), jsonArray.get(i).toString(), context));
                    }
                }
            } else {
                jsonEntry.setValue(translate((StringUtils.hasText(jsonPath) ? jsonPath + "." + jsonEntry.getKey() : jsonEntry.getKey().toString()), jsonEntry.getValue().toString(), context));
            }
        }
    }

    /**
     * Checks if this message interceptor is capable of this message type. XML message interceptors may only apply to this message
     * type while JSON message interceptor implementations do not and vice versa.
     *
     * @param messageType the message type representation as String (e.g. xml, json, csv, plaintext).
     * @return true if this message interceptor supports the message type.
     */
    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.JSON.toString().equalsIgnoreCase(messageType);
    }
}
