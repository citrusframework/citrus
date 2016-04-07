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

package com.consol.citrus.restdocs.soap;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.*;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Converts a Http request to RestDoc operation request instance.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocSoapRequestConverter implements RequestConverter<WebServiceMessage> {

    @Override
    public OperationRequest convert(WebServiceMessage request) {
        try {
            TransportContext transportContext = TransportContextHolder.getTransportContext();
            URI uri;
            if (transportContext != null) {
                uri = transportContext.getConnection().getUri();
            } else {
                uri = URI.create("/");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            request.writeTo(bos);
            return new OperationRequestFactory().create(uri, HttpMethod.POST,
                    bos.toByteArray(), new HttpHeaders(),
                    extractParameters(request), extractParts(request));
        } catch (IOException | URISyntaxException e) {
            throw new CitrusRuntimeException("Failed to create Spring restdocs", e);
        }
    }

    private Parameters extractParameters(WebServiceMessage request) {
        Parameters parameters = new Parameters();
        return parameters;
    }

    private Collection<OperationRequestPart> extractParts(WebServiceMessage request) {
        List<OperationRequestPart> parts = new ArrayList<>();
        return parts;
    }
}
