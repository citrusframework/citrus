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

import java.util.Map;

/**
 * @since 2.7.4
 */
public interface SwaggerTestGenerator<T extends SwaggerTestGenerator> extends TestGenerator<T> {

    SwaggerTestGenerator withSpec(String swagger);

    SwaggerTestGenerator withOperation(String operation);

    SwaggerTestGenerator withNamePrefix(String namePrefix);

    SwaggerTestGenerator withNameSuffix(String nameSuffix);

    SwaggerTestGenerator withInboundMappings(Map<String, String> inbound);

    SwaggerTestGenerator withOutboundMappings(Map<String, String> outbound);

    SwaggerTestGenerator withInboundMappingFile(String inboundFile);

    SwaggerTestGenerator withOutboundMappingFile(String outboundFile);

    SwaggerTestGenerator withEndpoint(String endpoint);

    String getNameSuffix();

    String getSwaggerResource();

    String getOperation();

}
