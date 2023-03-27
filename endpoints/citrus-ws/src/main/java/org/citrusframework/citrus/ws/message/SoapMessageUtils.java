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

package org.citrusframework.citrus.ws.message;

import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public final class SoapMessageUtils {

    /**
     * Prevent instantiation.
     */
    private SoapMessageUtils() {
        super();
    }

    /**
     * Apply message settings to target http message.
     * @param from
     * @param to
     */
    public static void copy(Message from, SoapMessage to) {
        SoapMessage source;
        if (from instanceof SoapMessage) {
            source = (SoapMessage) from;
        } else {
            source = new SoapMessage(from);
        }

        copy(source, to);
    }

    /**
     * Apply message settings to target http message.
     * @param from
     * @param to
     */
    public static void copy(SoapMessage from, SoapMessage to) {
        to.setName(from.getName());
        to.setType(from.getType());
        to.setPayload(from.getPayload());

        if (from.getSoapAction() != null) {
            to.soapAction(from.getSoapAction());
        }

        from.getHeaders().entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals(MessageHeaders.ID) && !entry.getKey().equals(MessageHeaders.TIMESTAMP))
                .forEach(entry -> to.header(entry.getKey(), entry.getValue()));

        from.getHeaderData().forEach(to::addHeaderData);
        from.getAttachments().forEach(to::addAttachment);
    }
}
