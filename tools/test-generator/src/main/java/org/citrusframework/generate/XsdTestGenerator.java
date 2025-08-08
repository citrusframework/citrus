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

package org.citrusframework.generate;

import java.util.Map; /**
 * @since 2.7.4
 */
public interface XsdTestGenerator<T extends XsdTestGenerator<T>> extends TestGenerator<T> {

    XsdTestGenerator<T> withXsd(String xsd);

    XsdTestGenerator<T> withRequestMessage(String requestMessage);

    XsdTestGenerator<T> withResponseMessage(String responseMessage);

    XsdTestGenerator<T> withInboundMappings(Map<String, String> inbound);

    XsdTestGenerator<T> withOutboundMappings(Map<String, String> outbound);

    XsdTestGenerator<T> withInboundMappingFile(String inboundFile);

    XsdTestGenerator<T> withOutboundMappingFile(String outboundFile);

    XsdTestGenerator<T> withEndpoint(String endpoint);

    XsdTestGenerator<T> withNameSuffix(String suffix);

    String getResponseMessageSuggestion();

    String getXsd();

    String getRequestMessage();

    String getResponseMessage();
}
