/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.http.config.handler;

import org.citrusframework.http.config.xml.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class CitrusHttpTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("send-request", new HttpSendRequestActionParser());
        registerBeanDefinitionParser("receive-response", new HttpReceiveResponseActionParser());
        registerBeanDefinitionParser("receive-request", new HttpReceiveRequestActionParser());
        registerBeanDefinitionParser("send-response", new HttpSendResponseActionParser());
    }
}
