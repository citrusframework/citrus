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
import org.citrusframework.actions.ReceiveMessageBuilderFactory;
import org.citrusframework.spi.Resource;

public interface SoapClientReceiveMessageBuilderFactory<T extends TestAction, M extends SoapClientReceiveMessageBuilderFactory<T, M>>
        extends ReceiveMessageBuilderFactory<T, M> {

    /**
     * Sets special SOAP action message header.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> soapAction(String soapAction);

    /**
     * Sets the control attachment with string content.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachment(String contentId, String contentType, String content);

    /**
     * Sets the control attachment with content resource.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachment(String contentId, String contentType, Resource contentResource);

    /**
     * Sets the control attachment with content resource.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachment(String contentId, String contentType, Resource contentResource, Charset charset);

    /**
     * Sets the charset name for this send action builder's control attachment.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> charset(String charsetName);

    /**
     * Sets the control attachment from Java object instance.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachment(Object attachment);

    /**
     * Set explicit SOAP attachment validator name.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachmentValidatorName(String validator);

    /**
     * Set explicit SOAP attachment validator.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> attachmentValidator(Object validator);

    /**
     * Sets the request content type header.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> contentType(String contentType);

    /**
     * Sets the request accept header.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> accept(String accept);

    /**
     * Sets the response status reason phrase.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> reasonPhrase(String reasonPhrase);

    /**
     * Sets the response status.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> status(Object status);

    /**
     * Sets the response status code.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> statusCode(Integer statusCode);

    /**
     * Sets the context path.
     */
    SoapClientReceiveMessageBuilderFactory<T, M> contextPath(String contextPath);
}
