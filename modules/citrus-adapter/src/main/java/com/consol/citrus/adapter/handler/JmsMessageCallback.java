/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.adapter.handler;

import org.springframework.integration.Message;

import javax.jms.JMSException;

/**
 * Message callback interface for manipulating JMS request messages before sending.
 * @deprecated since Citrus 1.4
 */
@Deprecated
public interface JmsMessageCallback {

    /** Opportunity to decorate generated jms message before forwarding */
    void doWithMessage(javax.jms.Message message, Message<?> request) throws JMSException;
}
