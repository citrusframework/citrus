/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.jackson;

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import org.codehaus.jackson.map.ObjectMapper;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class JsonHelper {

    @Autowired
    private ObjectMapper jsonMapper;

    /**
     * Extracts model type information from json object and constructs proper model object from json data.
     * @param jsonObject
     * @return
     */
    public Object readModel(JSONObject jsonObject) {
        if (!jsonObject.containsKey("modelType")) {
            throw new CitrusAdminRuntimeException("Missing model type information in json object");
        }

        // remove type information as it is not present in jaxb objects
        jsonObject.remove("type");

        String modelType = jsonObject.remove("modelType").toString();
        try {
            return jsonMapper.readValue(jsonObject.toJSONString(), Class.forName(modelType));
        } catch (ClassNotFoundException e) {
            throw new CitrusAdminRuntimeException(String.format("Unknown model type '%s'", modelType), e);
        } catch (Exception e) {
            throw new CitrusAdminRuntimeException("Failed to read json object", e);
        }
    }
}
