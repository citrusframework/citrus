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

package org.citrusframework.generate;

import java.util.Map; /**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public interface WsdlTestGenerator<T extends WsdlTestGenerator> extends TestGenerator<T> {

    WsdlTestGenerator withWsdl(String wsdl);

    WsdlTestGenerator withOperation(String operation);

    WsdlTestGenerator withNamePrefix(String namePrefix);

    WsdlTestGenerator withNameSuffix(String nameSuffix);

    WsdlTestGenerator withInboundMappings(Map<String, String> inbound);

    WsdlTestGenerator withOutboundMappings(Map<String, String> outbound);

    WsdlTestGenerator withInboundMappingFile(String inboundFile);

    WsdlTestGenerator withOutboundMappingFile(String outboundFile);

    WsdlTestGenerator withEndpoint(String endpoint);

    String getNameSuffix();

    String getWsdl();

    Object getOperation();

}
