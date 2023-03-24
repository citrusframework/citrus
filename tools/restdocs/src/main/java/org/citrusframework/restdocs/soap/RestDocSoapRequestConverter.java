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

package org.citrusframework.restdocs.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.RequestConverter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

/**
 * Converts a Http request to RestDoc operation request instance.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocSoapRequestConverter implements RequestConverter<MessageContext> {

    @Override
    public OperationRequest convert(MessageContext messageContext) {
        try {
            TransportContext transportContext = TransportContextHolder.getTransportContext();
            URI uri;
            if (transportContext != null) {
                uri = transportContext.getConnection().getUri();
            } else {
                uri = URI.create("/");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            messageContext.getRequest().writeTo(bos);
            return new OperationRequestFactory().create(uri, HttpMethod.POST,
                    bos.toByteArray(), extractHeaders(messageContext), extractParts(messageContext));
        } catch (IOException | URISyntaxException e) {
            throw new CitrusRuntimeException("Failed to create Spring restdocs", e);
        }
    }

    protected HttpHeaders extractHeaders(MessageContext messageContext) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (messageContext.getRequest() instanceof SaajSoapMessage) {
            Map<String, String> mimeHeaders = new HashMap<String, String>();
            MimeHeaders messageMimeHeaders = ((SaajSoapMessage)messageContext.getRequest()).getSaajMessage().getMimeHeaders();

            if (messageMimeHeaders != null) {
                Iterator<?> mimeHeaderIterator = messageMimeHeaders.getAllHeaders();
                while (mimeHeaderIterator.hasNext()) {
                    MimeHeader mimeHeader = (MimeHeader)mimeHeaderIterator.next();
                    // http headers can have multipile values so headers might occur several times in map
                    if (mimeHeaders.containsKey(mimeHeader.getName())) {
                        // header is already present, so concat values to a single comma delimited string
                        String value = mimeHeaders.get(mimeHeader.getName());
                        value += ", " + mimeHeader.getValue();
                        mimeHeaders.put(mimeHeader.getName(), value);
                    } else {
                        mimeHeaders.put(mimeHeader.getName(), mimeHeader.getValue());
                    }
                }

                for (Map.Entry<String, String> httpHeaderEntry : mimeHeaders.entrySet()) {
                    httpHeaders.add(httpHeaderEntry.getKey(), httpHeaderEntry.getValue());
                }
            }
        }

        return httpHeaders;
    }

    protected Collection<OperationRequestPart> extractParts(MessageContext messageContext) throws IOException {
        List<OperationRequestPart> parts = new ArrayList<>();
        return parts;
    }
}
