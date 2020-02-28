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

package com.consol.citrus.validation.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.variable.dictionary.DataDictionary;

/**
 * List of global message construction interceptors that modify message payload and message headers. User just has to add
 * interceptor implementation as bean to the Spring application context.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageConstructionInterceptors implements MessageConstructionInterceptorAware {

    private List<MessageConstructionInterceptor> messageConstructionInterceptors = new ArrayList<>();

    /**
     * Sets the messageConstructionInterceptors property.
     *
     * @param messageConstructionInterceptors
     */
    public void setMessageConstructionInterceptors(List<MessageConstructionInterceptor> messageConstructionInterceptors) {
        this.messageConstructionInterceptors = messageConstructionInterceptors;
    }

    /**
     * Gets the messageConstructionInterceptors.
     * @return
     */
    public List<MessageConstructionInterceptor> getMessageConstructionInterceptors() {
        return Collections.unmodifiableList(messageConstructionInterceptors);
    }

    @Override
    public void addMessageConstructionInterceptor(MessageConstructionInterceptor interceptor) {
        if (interceptor instanceof DataDictionary && !((DataDictionary<?>)interceptor).isGlobalScope()) {
            throw new CitrusRuntimeException("Unable to add non global scoped data dictionary to global message construction interceptors - " +
                    "either declare dictionary as global scope or explicitly add dictionary to test actions instead");
        }
        this.messageConstructionInterceptors.add(interceptor);
    }
}
