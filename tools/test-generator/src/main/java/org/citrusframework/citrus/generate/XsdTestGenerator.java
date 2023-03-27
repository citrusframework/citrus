/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.citrus.generate;

import java.util.Map; /**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public interface XsdTestGenerator<T extends XsdTestGenerator> extends TestGenerator<T> {

    XsdTestGenerator withXsd(String xsd);

    XsdTestGenerator withRequestMessage(String requestMessage);

    XsdTestGenerator withResponseMessage(String responseMessage);

    XsdTestGenerator withInboundMappings(Map<String, String> inbound);

    XsdTestGenerator withOutboundMappings(Map<String, String> outbound);

    XsdTestGenerator withInboundMappingFile(String inboundFile);

    XsdTestGenerator withOutboundMappingFile(String outboundFile);

    XsdTestGenerator withEndpoint(String endpoint);

    XsdTestGenerator withNameSuffix(String suffix);

    String getResponseMessageSuggestion();

    String getXsd();

    String getRequestMessage();

    String getResponseMessage();
}
