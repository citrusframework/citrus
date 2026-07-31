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

import org.citrusframework.message.builder.json.JsonPayloadBuilders;
import org.citrusframework.message.builder.xml.XmlPayloadBuilders;

/**
 * Interface combines default implementations with domain specific language methods for all message payload builders
 * available in Citrus.
 */
public interface PayloadBuilderSupport extends PayloadBuilders, PayloadBuilderLookupSupport {

    @Override
    default JsonPayloadBuilders json() {
        return new JsonPayloadBuilders() {
            @Override
            public MessagePayloadBuilder marshal(Object model) {
                return lookupPayloadBuilder("jsonMarshal", model);
            }

            @Override
            public MessagePayloadBuilder marshal(Object model, Object mapper) {
                return lookupPayloadBuilder("jsonMarshal", model, mapper);
            }
        };
    }

    @Override
    default XmlPayloadBuilders xml() {
        return new XmlPayloadBuilders() {
            @Override
            public MessagePayloadBuilder marshal(Object model) {
                return lookupPayloadBuilder("xmlMarshal", model);
            }

            @Override
            public MessagePayloadBuilder marshal(Object model, Object marshaller) {
                return lookupPayloadBuilder("xmlMarshal", model, marshaller);
            }
        };
    }
}
