/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpRequest;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.RequestConverter;

/**
 * Converts a Http request to RestDoc operation request instance.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocRequestConverter implements RequestConverter<CachedBodyHttpRequest> {

    @Override
    public OperationRequest convert(CachedBodyHttpRequest request) {
        return new OperationRequestFactory().create(request.getURI(), request.getMethod(),
                request.getBody(), request.getHeaders(), extractParts(request));
    }

    protected Collection<OperationRequestPart> extractParts(HttpRequest request) {
        List<OperationRequestPart> parts = new ArrayList<>();
        return parts;
    }
}
