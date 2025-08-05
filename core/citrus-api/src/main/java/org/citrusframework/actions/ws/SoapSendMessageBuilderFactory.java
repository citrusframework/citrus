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

package org.citrusframework.actions.ws;

import java.nio.charset.Charset;

import org.citrusframework.TestAction;
import org.citrusframework.actions.SendMessageBuilderFactory;
import org.citrusframework.spi.Resource;

public interface SoapSendMessageBuilderFactory<T extends TestAction, M extends SoapSendMessageBuilderFactory<T, M>>
        extends SendMessageBuilderFactory<T, M> {

    /**
     * Sets special SOAP action message header.
     */
    M soapAction(String soapAction);

    /**
     * Sets the attachment with string content.
     */
    M attachment(String contentId, String contentType, String content);

    /**
     * Sets the attachment with content resource.
     */
    M attachment(String contentId, String contentType, Resource contentResource);

    /**
     * Sets the attachment with content resource.
     */
    M attachment(String contentId, String contentType, Resource contentResource, Charset charset);

    /**
     * Sets the charset name for this send action builder's most recent attachment.
     */
    M charset(String charsetName);

    /**
     * Sets the attachment from Java object instance.
     */
    M attachment(Object attachment);

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    M uri(String uri);

    /**
     * Sets the request content type header.
     */
    M contentType(String contentType);

    /**
     * Sets the request accept header.
     */
    M accept(String accept);

    M mtomEnabled(boolean mtomEnabled);
}
