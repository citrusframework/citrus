/*
 *    Copyright 2018 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.consol.citrus.validation.json;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class JsonMappingValidationCallbackTest {

    @Test
    public void validateByteArrayJsonPayload() {

        //GIVEN
        String id = "123";
        String json = String.format("{\"id\": \"%s\"}", id);
        Message message = new DefaultMessage();
        message.setPayload(json.getBytes());


        //THEN
        final JsonMappingValidationCallback<Product> validationCallback =
                new JsonMappingValidationCallback<Product>(Product.class, new ObjectMapper()) {
                    @Override
                    public void validate(Product payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(payload.getId(), id);
                    }
                };

        //WHEN
        validationCallback.validate(message, null);
    }


    //Helper class
    static private class Product {
        private String id;

        public String getId() { return id; }

        public void setId(String id) {this.id = id;}
    }
}