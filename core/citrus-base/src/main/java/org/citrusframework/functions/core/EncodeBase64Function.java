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

package org.citrusframework.functions.core;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Encodes a character sequence to base64 binary using given charset.
 */
public class EncodeBase64Function implements ParameterizedFunction<EncodeBase64Function.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        try {
            return Base64.encodeBase64String(params.getValue().getBytes(params.getCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException("Unsupported character encoding", e);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements ParameterizedFunction.FunctionParameters {

        private String value;
        private String charset = "UTF-8";


        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setValue(parameterList.get(0));

            if (parameterList.size() > 1) {
                setCharset(parameterList.get(1));
            }
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The value to perform substring.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getCharset() {
            return charset;
        }

        @SchemaProperty(description = "Optional charset used to decode.", defaultValue = "UTF-8")
        public void setCharset(String charset) {
            this.charset = charset;
        }
    }
}
