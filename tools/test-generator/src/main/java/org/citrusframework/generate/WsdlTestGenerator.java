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
public interface WsdlTestGenerator<T extends WsdlTestGenerator<T>> extends TestGenerator<T> {

    WsdlTestGenerator<T> withWsdl(String wsdl);

    WsdlTestGenerator<T> withOperation(String operation);

    WsdlTestGenerator<T> withNamePrefix(String namePrefix);

    WsdlTestGenerator<T> withNameSuffix(String nameSuffix);

    WsdlTestGenerator<T> withInboundMappings(Map<String, String> inbound);

    WsdlTestGenerator<T> withOutboundMappings(Map<String, String> outbound);

    WsdlTestGenerator<T> withInboundMappingFile(String inboundFile);

    WsdlTestGenerator<T> withOutboundMappingFile(String outboundFile);

    WsdlTestGenerator<T> withEndpoint(String endpoint);

    String getNameSuffix();

    String getWsdl();

    Object getOperation();

}
