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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.operation.*;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Converts a Http response to RestDoc operation response instance.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocSoapResponseConverter implements ResponseConverter<WebServiceMessage> {

    @Override
    public OperationResponse convert(WebServiceMessage response) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            response.writeTo(bos);

            return new OperationResponseFactory().create(
                    extractStatus(response), extractHeaders(response),
                    bos.toByteArray());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create Spring restdocs", e);
        }
    }

    protected HttpHeaders extractHeaders(WebServiceMessage response) {
        return new HttpHeaders();
    }

    protected HttpStatus extractStatus(WebServiceMessage response) {
        if (response instanceof SoapMessage && ((SoapMessage) response).hasFault()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.OK;
    }
}
