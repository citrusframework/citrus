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

package com.consol.citrus.adapter.handler.mapping;

/**
 * Extractor searches for SOAP action header name in request headers for mapping name identification.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 * @deprecated since Citrus 1.4, in favor of {@link com.consol.citrus.endpoint.adapter.mapping.SoapActionMappingKeyExtractor}
 */
@Deprecated
public class SoapActionMappingKeyExtractor extends com.consol.citrus.endpoint.adapter.mapping.SoapActionMappingKeyExtractor implements MappingKeyExtractor {
}
